using MetroFramework.Forms;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net;
using System.Text;
using System.Threading;
using System.Windows.Automation;
using System.Windows.Forms;

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

            foreach (Process process in Process.GetProcessesByName("chromedriver"))
            {
                try
                {
                    process.Kill();
                }
                catch (Exception)
                {
                    Console.WriteLine("Error :: Kill chromedriver");
                }
            }


            UpdateCheck();
            InitializeComponent();
            Rectangle workingArea = Screen.GetWorkingArea(this);
            this.Location = new Point(workingArea.Right - Size.Width,
                                      workingArea.Bottom - Size.Height);
            this.ShowInTaskbar = false;
            this.versionLabel.Text = AppHelper.GetVersion();
            
        }

        private void ThreadManager()
        {
           
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

        private void OpenMainBtnClick(object sender, EventArgs e)
        {

            KillAllSite();

            main = new Main();
            main.FormClosed += new FormClosedEventHandler(MainFormCloedEvent);
            
            if (t1 == null || !t1.IsAlive)
            {
                t1 = new Thread(new ThreadStart(KillAccountPage));
                t1.IsBackground = true;
                t1.Start();
            }

            if (t2 == null || !t2.IsAlive)
            {
                //t2 = new Thread(new ThreadStart(CheckOpenedSite));
                //t2.IsBackground = true;
                //t2.Start();

            }

            if (t3 == null || !t3.IsAlive)
            {
                t3 = new Thread(new ThreadStart(ThreadManager));
                t3.IsBackground = true;
                t3.Start();

            }

            main.ShowDialog();
        }

        private void Launcher_Load(object sender, EventArgs e)
        {
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
        }

        private void KillAllSite()
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
            foreach (Process process in Process.GetProcessesByName("msedgedriver"))
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
                foreach (Process process in Process.GetProcessesByName("chrome"))
                {
                    try
                    {
                        string url = GetChromeUrl(process);
                        if (url == null)
                            continue;

                        Console.WriteLine("CH Url for '" + process.MainWindowTitle + "' is " + url);
                        if (url.Contains("netflix.com/YourAccount") || url.Contains("uflix.co.kr/uws/web/mine/userInfo")
                            || url.Contains("member.wavve.com/me") || url.Contains("tving.com/my/main")
                            || url.Contains("netflix.com/ManageProfiles")
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
        public string GetChromeUrl(Process process)
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

        private void CheckOpenedSite()
        {
            do
            {
                try
                {
                    Console.WriteLine("Check Opened Site");
                    Thread.Sleep(10000);
                    Process[] procsChrome = Process.GetProcessesByName("msedge");
                    if (procsChrome.Length <= 0)
                    {
                        Console.WriteLine("Chrome is not running");
                    }
                    else
                    {
                        foreach (Process proc in procsChrome)
                        {
                            // the chrome process must have a window
                            if (proc.MainWindowHandle == IntPtr.Zero)
                            {
                                continue;
                            }
                            AutomationElement root = AutomationElement.FromHandle(proc.MainWindowHandle);
                            Condition condition = new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.TabItem);
                            var tabs = root.FindAll(TreeScope.Descendants, condition);
                            CheckAndSendUseInfo(tabs);

                        }
                    }
                }
                catch (Exception ex)
                {
                    ex.ToString();
                }
            } while (true);

        }

        private void CheckAndSendUseInfo(AutomationElementCollection tabs)
        {

            String ip = AppHelper.GetLocalIp();
            Boolean netflix = false;
            Boolean uflix = false;
            Boolean tving = false;
            Boolean wavve = false;
            if(tabs != null)
            {
                foreach (AutomationElement tabitem in tabs)
                {
                    string urlname = tabitem.Current.Name;
                    if (urlname.Contains("Netflix") == true)
                    {
                        netflix = true;
                    }
                    if (urlname.Contains("U+영화월정액") == true)
                    {
                        uflix = true;
                    }
                    if (urlname.Contains("TVING｜오늘의 노는 법") == true)
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
        }

        private void UpdateCheck()
        {
            var uri = "http://" + AppHelper.GetServerUrl() + "/dist/_CHECK";
            var webRequest = (HttpWebRequest)WebRequest.Create(uri);
            var webResponse = (HttpWebResponse)webRequest.GetResponse();
            var reader = new StreamReader(webResponse.GetResponseStream());
            string s = reader.ReadToEnd();

            String str = "서버 버전 : " + s + "\r";
            str = str + "설치 버전 : " + AppHelper.GetVersion() + "\r";

            if (long.Parse(s) > long.Parse(AppHelper.GetVersion()))
            {
                updateLoginAgent(s); 
            }
           
        }

        private string filename = null;
        private void updateLoginAgent(String version)
        {
            Uri uri = new Uri("http://" + AppHelper.GetServerUrl() + "/dist/setup-LA-" + version + ".exe");
            filename = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Temp/setup-LA.exe");

            try
            {
                if (File.Exists(filename))
                {
                    File.Delete(filename);
                }

                WebClient wc = new WebClient();
                wc.DownloadFileAsync(uri, filename);
                wc.DownloadFileCompleted += new AsyncCompletedEventHandler(wc_DownloadFileCompleted);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message.ToString());
            }
        }

        
        private void wc_DownloadFileCompleted(object sender, AsyncCompletedEventArgs e)
        {
            if (e.Error == null)
            {
                Process.Start(filename);
                Close();
                Application.Exit();
            }
            else
            {
                MessageBox.Show("Unable to download exe, please check your connection", "Download failed!");
            }
        }

    }
}
