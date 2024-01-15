using System;
using System.Windows.Forms;
using System.Net;
using System.IO;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Text;
using OpenQA.Selenium;
using System.Linq;
using System.Diagnostics;
using OpenQA.Selenium.Edge;
using System.Reflection;
using System.Threading.Tasks;


namespace LoginAgent
{
    public partial class Main : Form
    {

        public Action KillBrowser;


        public Main()
        {
            InitializeComponent();

        }



        private void LoginSite(JObject data)
        {
            string id = data.GetValue("user_id").ToString();
            string pw_enc = data.GetValue("user_password").ToString();

            string siteId = data.GetValue("id").ToString();
            
            string pw = AppHelper.Decrypt(pw_enc, AppHelper.M_K);

            string url = data.GetValue("login_url").ToString();
            string idXpath = data.GetValue("id_xpath").ToString();
            string pwXpath = data.GetValue("pw_xpath").ToString();
            string logXpath = data.GetValue("login_xpath").ToString();
            string logXpath2 = data.GetValue("login_xpath2").ToString();

            

            if (siteId == "disney")
            {
                RunPyYouExe(url, idXpath, pwXpath, id, EncodeToBase64(pw));
            }
            else
            {
                EdgeDriverService _driverService = EdgeDriverService.CreateDefaultService();

                _driverService.HideCommandPromptWindow = true;


                EdgeOptions _options = new EdgeOptions();




                var userDataPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Microsoft\\Edge\\User Data");

                _options.AddArguments("--user-data-dir=" + userDataPath);

                _options.AddArguments("--disable-notifications --disable-infobars --start-maximized");

                _options.AddExcludedArgument("enable-automation");

                _options.AddUserProfilePreference("credentials_enable_service", false);
                _options.AddUserProfilePreference("profile.password_manager_enabled", false);

                _options.AddUserProfilePreference("profile.exited_cleanly", true);
                _options.AddUserProfilePreference("profile.exit_type", "Normal");
             

                //_options.AddArguments("--incognito");

                if (siteId != "disney")
                {
                    _options.AddArguments("-inprivate");
                }


                EdgeDriver _driver = new EdgeDriver(_driverService, _options);

                _driver.Navigate().GoToUrl(url); // 웹 사이트에 접속합니다.
                _driver.Manage().Timeouts().ImplicitWait = TimeSpan.FromSeconds(10);


                if (siteId == "disney" || siteId == "youtube")
                {


                    var element = _driver.FindElement(By.XPath(idXpath));
                    element.SendKeys(id);
                    element = _driver.FindElement(By.XPath(logXpath));
                    element.Click();
                    element = _driver.FindElement(By.XPath(pwXpath));
                    element.SendKeys(pw);
                    element = _driver.FindElement(By.XPath(logXpath2));
                    element.Click();

                }
                else
                {
                    //var element = _driver.FindElementByXPath(idXpath);
                    var element = _driver.FindElement(By.XPath(idXpath));

                    element.SendKeys(id);
                    element = _driver.FindElement(By.XPath(pwXpath));
                    element.SendKeys(pw);
                    element = _driver.FindElement(By.XPath(logXpath));
                    element.Click();
                }

            }




        }

        private void RunPyYouExe(string url, string idXpath, string pwXpath, string id, string encryptedPassword)
        {
            string args = $"--url \"{url}\" --id_xpath \"{idXpath}\" --pw_xpath \"{pwXpath}\" --dkdlel \"{id}\" --alqjs \"{encryptedPassword}\"";

            ProcessStartInfo startInfo = new ProcessStartInfo()
            {
                FileName = "py_you.exe",
                Arguments = args,
                UseShellExecute = false,
                CreateNoWindow = true,
                WindowStyle = ProcessWindowStyle.Hidden
            };

            Process proc = new Process() { StartInfo = startInfo };
            proc.Start();
        }

        public string EncodeToBase64(string plainText)
        {
            byte[] bytes = Encoding.UTF8.GetBytes(plainText);
            return Convert.ToBase64String(bytes);
        }


        private async void DoLogin(string site)
        {

            JObject data = GetSiteData(site);

            if (data == null)
            {
                MessageBox.Show("서버에 오류가 발생했습니다. 카운터에 문의 하세요");
                return;
            }

            if (data.GetValue("user_id") == null)
            {
                MessageBox.Show("계정을 모두 사용 중입니다. 카운터에 문의 하세요");
                return;
            }


            try
            {
                LoginSite(data);

            }
            catch (Exception e)
            {
                String msg = e.Message;
                Console.WriteLine(msg);
            }

            if (data.GetValue("saveOccupied").ToString() == "true")
            {
                LoginAccount(data.GetValue("id").ToString(), data.GetValue("account_id").ToString(), data.GetValue("user_id").ToString());
            }
        }

        private JObject GetSiteData(string site)
        {
            return GetObjectData("http://" + AppHelper.GetServerUrl() + "/api/sites/" + site + "/selectAvailableAccountSeq?select=accounts" + "&pc_ip=" + AppHelper.GetLocalIp());

        }

        private JObject GetObjectData(string uri)
        {
            try
            {
                var webRequest = (HttpWebRequest)WebRequest.Create(uri);
                var webResponse = (HttpWebResponse)webRequest.GetResponse();
                var reader = new StreamReader(webResponse.GetResponseStream());
                string s = reader.ReadToEnd();
                return JsonConvert.DeserializeObject<JObject>(s);
            }
            catch (Exception)
            {
                return null;
            }

        }

        private void LoginAccount(string siteId, string accountId, string userId)
        {


            string url = "http://" + AppHelper.GetServerUrl() + "/api/occupieds";
            string localIp = AppHelper.GetLocalIp();

            var dataObj = new JObject
            {
                { "site_id", siteId },
                { "account_id", accountId },
                { "user_id", userId },
                { "pc_ip", localIp }
            };

            string data = JsonConvert.SerializeObject(dataObj);

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

        private void CloseClick(object sender, EventArgs e)
        {
            this.Close();
        }


        private void NetFlixBtnClick(object sender, EventArgs e)
        {
            PerformLoginAction("netflix");
        }

        private void WavveBtnClick(object sender, EventArgs e)
        {
            PerformLoginAction("wavve");
        }

        private void TvingBtnClick(object sender, EventArgs e)
        {
            PerformLoginAction("tving");
        }

        private void DisneyBtn_Click(object sender, EventArgs e)
        {
            PerformLoginAction("disney");
        }

        private void YoutubeBtnClick(object sender, EventArgs e)
        {
            PerformLoginAction("youtube");
        }

        private void PerformLoginAction(string site)
        {
            KillBrowser();
            DoLogin(site);
            ProcessUtils.KillDriver();
        }

        private void Main_Load(object sender, EventArgs e)
        {

        }

        private void settingsBtn(object sender, EventArgs e)
        {
            // Display a password input dialog
            string password = Prompt.ShowDialog("Enter Code", "Code Required");

            // Check if the entered password is correct
            if (password == "!QAZxsw2")
            {
                // Call the testSite function
                testSite();
            }
            else
            {
                MessageBox.Show("Incorrect Password!", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        // TestSite method (implement this according to your requirements)
        private void testSite()
        {

            disneyBtn.Visible = true;
            this.Width = 860;

        }

        public static class Prompt
        {
            public static string ShowDialog(string text, string caption)
            {
                Form prompt = new Form()
                {
                    Width = 500,
                    Height = 150,
                    FormBorderStyle = FormBorderStyle.FixedDialog,
                    Text = caption,
                    StartPosition = FormStartPosition.CenterScreen
                };
                Label textLabel = new Label() { Left = 50, Top = 20, Text = text };
                TextBox textBox = new TextBox() { Left = 50, Top = 50, Width = 400 };
                textBox.UseSystemPasswordChar = true; // Hide password characters
                Button confirmation = new Button() { Text = "Ok", Left = 350, Width = 100, Top = 70, DialogResult = DialogResult.OK };
                confirmation.Click += (sender, e) => { prompt.Close(); };
                prompt.Controls.Add(textBox);
                prompt.Controls.Add(confirmation);
                prompt.Controls.Add(textLabel);
                prompt.AcceptButton = confirmation;

                return prompt.ShowDialog() == DialogResult.OK ? textBox.Text : "";
            }
        }
    }
}
