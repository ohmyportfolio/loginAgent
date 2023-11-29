
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
using System.Threading;
using System.Windows.Automation;
using System.Windows.Forms;
using System.IO.Compression;
using System.Threading.Tasks;
using System.Net.Http;



namespace LoginAgent
{
    public partial class Launcher : MetroForm
    {
        private Main main;
        public Thread t1;
        public Thread t2;
        public Thread t3;


        public Launcher()
        {

            Process[] procs = Process.GetProcessesByName("LoginAgent");
            // 두번 이상 실행되었을 때 처리할 내용을 작성합니다.
            if (procs.Length > 1)
            {
                //MessageBox.Show("프로그램이 이미 실행되고 있습니다.\n다시 한번 확인해주시기 바랍니다.");
                Application.ExitThread();
                Environment.Exit(0);
            }

            KillDriver();

            InitializeComponent();
            Rectangle workingArea = Screen.GetWorkingArea(this);
            this.Location = new Point(workingArea.Right - Size.Width,
                                      workingArea.Bottom - Size.Height);
            this.ShowInTaskbar = false;
            this.versionLabel.Text = AppHelper.GetVersion();
            this.driverVer.Text = AppHelper.GetDriverVer();

            Task.Run(async () => await UpdateEdgeDriverAsync());
        }
 

        public void KillDriver()
        {
            foreach (Process process in Process.GetProcessesByName("msedgedriver"))
            {
                try
                {
                    process.Kill();
                }
                catch (Exception)
                {
                    Console.WriteLine("Error :: Kill msedgedriver");
                }
            }
        }


        private void MainFormCloedEvent(object sender, FormClosedEventArgs e)
        {
            Console.WriteLine("MainForm Closed");
            Console.WriteLine("Thread Stop");
            if (t1 != null && t1.IsAlive)
            {
                t1.Abort();
            }
            if (t2 != null && t2.IsAlive)
            {
                t2.Abort();
            }
            KillAllSite();
        }

        private async void OpenMainBtnClick(object sender, EventArgs e)
        {

            KillAllSite();
            KillDriver();

            main = new Main();
            main.FormClosed += new FormClosedEventHandler(MainFormCloedEvent);
            main.KillBrowser = this.KillAllSite;
            main.KillDriver = this.KillDriver;

            

            if (t1 == null || !t1.IsAlive)
            {
                t1 = new Thread(new ThreadStart(KillAccountPage));
                t1.IsBackground = true;
                t1.Start();
            }


            await UpdateEdgeDriverAsync();
            main.ShowDialog();
        }

        private void Launcher_Load(object sender, EventArgs e)
        {
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
        }

        public void KillAllSite()
        {

           

            foreach (Process process in Process.GetProcessesByName("msedge"))
            {
                try
                {
                    process.Kill();
                }
                catch (Exception)
                {
                    Console.WriteLine("Error :: KillAccountPage");
                }
            }


            CheckAndSendUseInfo(null);

        }

        private void KillAccountPage()
        {
            do
            {
                Console.WriteLine("Check Account Page and Kill");
                Thread.Sleep(3000);
                foreach (Process process in Process.GetProcessesByName("msedge"))
                {
                    try
                    {
                        string url = GetEdgeBrowserUrl(process);
                        if (url == null)
                            continue;


                        //DB화해서 관리하도록 변경 해야함.
                        Console.WriteLine("Edge Url for '" + process.MainWindowTitle + "' is " + url);
                        if (url.Contains("netflix.com/YourAccount") || url.Contains("uflix.co.kr/uws/web/mine/userInfo")
                            || url.Contains("member.wavve.com/me") || url.Contains("tving.com/my/main") || url.Contains("tving.com/my/watch")
                            || url.Contains("netflix.com/ManageProfiles") || url.Contains("edit-profiles")
                            || url.Contains("profiles/manage") || url.Contains("profilesForEdit") || url.Contains("profileForEdit")
                            || url.Contains("wavve.com/my") || url.Contains("wavve.com/voucher") || url.Contains("membership/tving")
                            || url.Contains("app-settings") || url.Contains("help.disneyplus.com") || url.Contains("/edit-profile/") //disney
                            || url.Contains("passwords")


                            )
                        {
                            process.Kill();
                        }


                    }
                    catch (Exception)
                    {
                        Console.WriteLine("Error :: KillAccountPage");
                    }
                }

            } while (true);
        }
        public string GetEdgeBrowserUrl(Process process)
        {
            if (process == null)
                throw new ArgumentNullException("process");

            if (process.MainWindowHandle == IntPtr.Zero)
                return null;

            AutomationElement element = AutomationElement.FromHandle(process.MainWindowHandle);
            if (element == null)
                return null;

            AutomationElementCollection edits5 = element.FindAll(TreeScope.Subtree, new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.Edit));
            AutomationElement edit = edits5[0];
            string vp = ((ValuePattern)edit.GetCurrentPattern(ValuePattern.Pattern)).Current.Value as string;
            Console.WriteLine(vp);
            return vp;
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
