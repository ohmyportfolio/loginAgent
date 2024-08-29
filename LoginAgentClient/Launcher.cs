
using MetroFramework.Forms;
using Microsoft.Win32;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net;
using System.Text;
using System.Windows.Automation;
using System.Windows.Forms;
using System.Threading.Tasks;
using System.Net.Http;
using System.Threading;
using System.Linq;


namespace LoginAgent
{
    public partial class Launcher : MetroForm
    {
        private Main main;
        private CancellationTokenSource cancellationTokenSource;

        private string[] DistrictTitle;
        private string[] DistrictUrls;

        public Launcher()
        {
            // AgentUpdater 실행
            try
            {
                string updaterPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "AgentUpdater.exe");
                if (File.Exists(updaterPath))
                {
                    Process.Start(updaterPath);
                    // 업데이터가 완료될 때까지 잠시 대기 (예: 5초)
                    Thread.Sleep(5000);
                }
            }
            catch (Exception ex)
            {
                // 업데이터 실행 중 오류 발생 시 로그 기록 (선택사항)
                Console.WriteLine($"AgentUpdater 실행 중 오류 발생: {ex.Message}");
            }

            Process[] procs = Process.GetProcessesByName("LoginAgent");
            // 두번 이상 실행되었을 때 처리할 내용을 작성합니다.
            if (procs.Length > 1)
            {
                //MessageBox.Show("프로그램이 이미 실행되고 있습니다.\n다시 한번 확인해주시기 바랍니다.");
                Application.ExitThread();
                Environment.Exit(0);
            }
            InitializeComponent();
            Rectangle workingArea = Screen.GetWorkingArea(this);
            this.Location = new Point(workingArea.Right - Size.Width,
                                      workingArea.Bottom - Size.Height);
            this.ShowInTaskbar = false;
            this.versionLabel.Text = AppHelper.GetVersion();
            this.driverVer.Text = AppHelper.GetDriverVer();
            Task.Run(async () => await UpdateEdgeDriverAsync());
        }



        private async Task<string[]> DownloadDistrictListAsync(string url)
        {
            using (var client = new HttpClient())
            {
                try
                {
                    var response = await client.GetStringAsync(url);
                    // URL에서 데이터를 성공적으로 다운로드한 경우,
                    // 개행 문자를 기준으로 분리하여 문자열 배열로 반환합니다.
                    return response.Split(new[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries);
                }
                catch (HttpRequestException e)
                {
                    // HttpRequestException은 주로 네트워크 오류나 요청 실패 시 발생합니다.
                    Console.WriteLine($"HTTP Request Exception: {e.Message}");
                }
                catch (Exception e)
                {
                    // 기타 예외 상황에 대한 처리
                    Console.WriteLine($"An error occurred: {e.Message}");
                }
            }

            // 에러 발생 시 빈 배열 반환
            return new string[] { };
        }




        private void MainFormCloedEvent(object sender, FormClosedEventArgs e)
        {
            Console.WriteLine("MainForm Closed");
            Console.WriteLine("Thread Stop");
            
            if (cancellationTokenSource != null)
            {
                cancellationTokenSource.Cancel();
                cancellationTokenSource = null;
            }
            
            KillAllSite();
            ProcessUtils.KillProcessByName("msedgedriver.exe");
        }

        private async void OpenMainBtnClick(object sender, EventArgs e)
        {
            cancellationTokenSource?.Cancel();
            await Task.Delay(100); // 필요에 따라 대기 시간 조정
            cancellationTokenSource = new CancellationTokenSource();

            KillAllSite();
            ProcessUtils.KillProcessByName("msedgedriver.exe");

            cancellationTokenSource = new CancellationTokenSource();

             //Start the background task without awaiting it
            var backgroundTask = Task.Run(() => RunKillAccountPageAsync(cancellationTokenSource.Token));


            main = new Main();
            main.FormClosed += new FormClosedEventHandler(MainFormCloedEvent);
            main.KillBrowser = this.KillAllSite;

         

            await UpdateEdgeDriverAsync();
            main.ShowDialog();
        }

        private async Task RunKillAccountPageAsync(CancellationToken cancellationToken)
        {
            while (!cancellationToken.IsCancellationRequested)
            {
                KillAccountPage(); // Adapt this method as needed
                try
                {
                    await Task.Delay(3000, cancellationToken);
                }
                catch (TaskCanceledException)
                {
                    break;
                }
            }
        }

        private async void Launcher_Load(object sender, EventArgs e)
        {
            this.FormBorderStyle = FormBorderStyle.FixedDialog;

            DistrictUrls = await DownloadDistrictListAsync("http://" + AppHelper.GetServerUrl() + "/dist/urllist.txt");
            DistrictTitle = await DownloadDistrictListAsync("http://" + AppHelper.GetServerUrl() + "/dist/titlelist.txt");
        }

        public void KillAllSite()
        {

            ProcessUtils.KillProcessByName("msedge.exe");
            CheckAndSendUseInfo(null);

        }

        private async void KillAccountPage()
        {
            Console.WriteLine("Check Account Page and Kill");

            // Edge와 Chrome 프로세스를 모두 가져옵니다.
            var processes = Process.GetProcesses()
                                   .Where(p => p.ProcessName.ToLower().Contains("chrome") || p.ProcessName.ToLower().Contains("edge"))
                                   .ToList();

            foreach (var process in processes)
            {
                try
                {
                    // Edge 프로세스에 대해서만 URL을 확인합니다.
                    if (process.ProcessName.ToLower().Contains("edge"))
                    {
                        string url = await GetBrowserUrlAsync(process);
                        if (url != null && DistrictUrls.Any(districtUrl => url.Contains(districtUrl)))
                        {
                            Console.WriteLine($"Killing Edge process for URL match: {process.ProcessName}, URL: {url}");
                            process.Kill();
                            continue; // 다음 프로세스로 넘어갑니다.
                        }
                    }

                    // 프로세스의 창 제목이 DistrictTitle에 정의된 제목을 포함하는지 확인합니다.
                    var title = process.MainWindowTitle.ToLower();
                    if (!string.IsNullOrEmpty(title) && DistrictTitle.Any(districtTitle => title.Contains(districtTitle.ToLower())))
                    {
                        Console.WriteLine($"Killing process for Title match: {process.ProcessName}, Title: {title}");
                        process.Kill();
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Error while processing '{process.ProcessName}': {ex.Message}");
                }
            }
        }



        public async Task<string> GetBrowserUrlAsync(Process process)
        {
            if (process == null)
                throw new ArgumentNullException(nameof(process));

            if (process.MainWindowHandle == IntPtr.Zero)
                return null;

            // 비동기적으로 UI Automation 작업을 백그라운드 스레드에서 실행
            return await Task.Run(() =>
            {
                var element = AutomationElement.FromHandle(process.MainWindowHandle);
                if (element == null)
                    return null;

                // TreeScope를 Subtree에서 Children으로 변경할 수 있으나, 여기서는 주소 표시줄을 찾기 위해 Subtree를 유지
                var edits = element.FindAll(TreeScope.Subtree, new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.Edit));
                if (edits == null || edits.Count == 0)
                    return null;

                foreach (AutomationElement edit in edits)
                {
                    if (edit.TryGetCurrentPattern(ValuePattern.Pattern, out object valuePattern))
                    {
                        var url = ((ValuePattern)valuePattern).Current.Value as string;
                        if (!string.IsNullOrEmpty(url))
                        {
                            Console.WriteLine(url);
                            return url;
                        }
                    }
                }
                return null;
            });
        }



        private void CheckAndSendUseInfo(AutomationElementCollection tabs)
        {

            String ip = AppHelper.GetLocalIp();
            Boolean netflix = false;
            Boolean uflix = false;
            Boolean tving = false;
            Boolean wavve = false;
            if (tabs != null)
            {
                foreach (AutomationElement tabitem in tabs)
                {
                    string urlname = tabitem.Current.Name;
                    if (urlname.Contains("Netflix") == true)
                    {
                        netflix = true;
                    }
                    if (urlname.Contains("TVING") == true)
                    {
                        tving = true;
                    }
                    if (urlname.Contains("wavve") == true)
                    {
                        wavve = true;
                    }
                    Console.WriteLine(urlname);
                }

            }
            var siteObj = new JObject
            {
                { "ip", ip },
                { "netflix", netflix },
                { "uflix", uflix },
                { "tving", tving },
                { "wavve", wavve }
            };

            string data = JsonConvert.SerializeObject(siteObj);

            string url = "http://" + AppHelper.GetServerUrl() + "/api/occupieds/1/useSite";
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url);
            request.Method = "POST";
            request.ContentType = "application/json";
            request.Timeout = 30 * 1000;
            //request.Headers.Add("Authorization", "BASIC SGVsbG8=");

            // POST할 데이타를 Request Stream에 쓴다
            byte[] bytes = Encoding.ASCII.GetBytes(data);
            request.ContentLength = bytes.Length; // 바이트수 지정

            using (Stream reqStream = request.GetRequestStream())
            {
                reqStream.Write(bytes, 0, bytes.Length);
            }

            // Response 처리
            string responseText = string.Empty;
            using (WebResponse resp = request.GetResponse())
            {
                Stream respStream = resp.GetResponseStream();
                using (StreamReader sr = new StreamReader(respStream))
                {
                    responseText = sr.ReadToEnd();
                }
            }
            Console.WriteLine(responseText);
            if (responseText.Contains("false"))
            {
                MessageBox.Show("등록 되지 않은 IP");
                System.Windows.Forms.Application.Exit();
            }

        }

     
        private void ShowToolStripMenuItem_Click(object sender, EventArgs e)
        {
            BringToFront();
        }

   

        private void TrayIcon_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            this.Show(); // Shows the form
            this.WindowState = FormWindowState.Normal; // Changes the window state to normal
        }


        private async Task UpdateEdgeDriverAsync()
        {
            // 1. Windows에 설치된 MS Edge 브라우저 버전 확인
            string installedEdgeVersion = GetInstalledEdgeVersion();

            // 2. 현재 경로에 있는 msedgedriver.exe의 버전 확인
            string edgeDriverPath = Path.Combine(Application.StartupPath, "msedgedriver.exe");
            string currentDriverVersion = File.Exists(edgeDriverPath)
                ? FileVersionInfo.GetVersionInfo(edgeDriverPath).FileVersion
                : "";

                        
            if (string.IsNullOrEmpty(installedEdgeVersion))
            {
                return;
            }

            // 3. 버전 비교
            if (installedEdgeVersion != currentDriverVersion)
            {
                string downloadUrl = $"https://msedgedriver.azureedge.net/{installedEdgeVersion}/edgedriver_win64.zip";

                // Create a single instance of HttpClient
                using (var client = new HttpClient())
                {
                    await DownloadAndReplaceEdgeDriver(client, downloadUrl, edgeDriverPath, installedEdgeVersion);
                }
            }
        }

        private string GetInstalledEdgeVersion()
        {
            string edgeVersion = "";
            try
            {
                using (RegistryKey key = Registry.CurrentUser.OpenSubKey("Software\\Microsoft\\Edge\\BLBeacon"))
                {
                    if (key != null)
                    {
                        Object o = key.GetValue("version");
                        if (o != null)
                        {
                            edgeVersion = o.ToString();
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("An error occurred while getting Edge version: " + ex.Message);
            }
            return edgeVersion;
        }

        private async Task DownloadAndReplaceEdgeDriver(HttpClient client, string url, string edgeDriverPath, string expectedVersion)
        {
            string tempDirectory = "";
            string backupDriverPath = "";

            try
            {
                // Create a temporary directory for the download process
                tempDirectory = Path.Combine(Path.GetTempPath(), "EdgeDriver");
                Directory.CreateDirectory(tempDirectory);

                // Define the path for the downloaded zip file
                string zipPath = Path.Combine(tempDirectory, "msedgedriver.zip");

                // Delete the zip file if it already exists
                if (File.Exists(zipPath))
                {
                    File.Delete(zipPath);
                }

                // Download the zip file
                var response = await client.GetAsync(url);
                using (var fs = new FileStream(zipPath, FileMode.CreateNew))
                {
                    await response.Content.CopyToAsync(fs);
                }

                // Define the path for the extracted contents
                string extractedPath = Path.Combine(tempDirectory, "extracted");

                // Delete the extracted path if it already exists
                if (Directory.Exists(extractedPath))
                {
                    Directory.Delete(extractedPath, true);
                }

                // Extract the downloaded zip file
                System.IO.Compression.ZipFile.ExtractToDirectory(zipPath, extractedPath);

                // Check if the new driver exists
                string newDriverPath = Path.Combine(extractedPath, "msedgedriver.exe");
                if (!File.Exists(newDriverPath))
                {
                    Console.WriteLine("Downloaded Edge driver is not found. Update aborted.");
                    return;
                }

                // Check the version of the downloaded driver
                string downloadedDriverVersion = FileVersionInfo.GetVersionInfo(newDriverPath).FileVersion;
                if (downloadedDriverVersion != expectedVersion)
                {
                    Console.WriteLine($"Downloaded Edge driver version mismatch: expected {expectedVersion}, but got {downloadedDriverVersion}. Update aborted.");
                    return;
                }

                // Backup existing driver
                backupDriverPath = edgeDriverPath + ".bak";
                if (File.Exists(edgeDriverPath))
                {
                    File.Copy(edgeDriverPath, backupDriverPath, overwrite: true);
                    File.Delete(edgeDriverPath);
                }

                // Replace the existing driver with the new one
                File.Copy(newDriverPath, edgeDriverPath);

                // Clean up backup if update is successful
                if (File.Exists(backupDriverPath))
                {
                    File.Delete(backupDriverPath);
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("An error occurred while updating Edge driver: " + ex.Message);

                // Restore backup in case of error
                if (!string.IsNullOrEmpty(backupDriverPath) && File.Exists(backupDriverPath))
                {
                    if (File.Exists(edgeDriverPath))
                    {
                        File.Delete(edgeDriverPath);
                    }
                    File.Move(backupDriverPath, edgeDriverPath);
                }
            }
            finally
            {
                // Clean up temp directory
                if (!string.IsNullOrEmpty(tempDirectory) && Directory.Exists(tempDirectory))
                {
                    Directory.Delete(tempDirectory, true);
                }
            }
        }


    }
}
