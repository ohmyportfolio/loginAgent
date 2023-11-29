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

        public Action KillDriver;

        public Main()
        {
            InitializeComponent();

        }

        
        private void NetFlixBtnClick(object sender, EventArgs e)
        {
            
            this.KillBrowser();
            DoLogin("netflix");
            this.KillDriver();

        }

        private void WavveBtnClick(object sender, EventArgs e)
        {
            
            this.KillBrowser();
            DoLogin("wavve");
            this.KillDriver();
        }


        private void TvingBtnClick(object sender, EventArgs e)
        {
            
            this.KillBrowser();
            DoLogin("tving");
            this.KillDriver();
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
                                      

            EdgeDriverService _driverService = EdgeDriverService.CreateDefaultService();

            _driverService.HideCommandPromptWindow = true;
                                   

            EdgeOptions _options = new EdgeOptions();

           
            _options.AddArguments("-inprivate");

            var userDataPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Microsoft\\Edge\\User Data");
                            
            _options.AddArguments("--user-data-dir=" + userDataPath);

            _options.AddArguments("--disable-notifications --disable-infobars --start-maximized");
                 
            _options.AddExcludedArgument("enable-automation");
            
            _options.AddUserProfilePreference("credentials_enable_service", false);
            _options.AddUserProfilePreference("profile.password_manager_enabled", false);

            _options.AddUserProfilePreference("profile.exited_cleanly", true);
            _options.AddUserProfilePreference("profile.exit_type", "Normal");
                

            //_options.AddArguments("--incognito");

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

        private void LoginAccount(string siteId , string accountId , string userId)
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

       

        private void DisneyBtn_Click(object sender, EventArgs e)
        {

            this.KillBrowser();
            DoLogin("disney");
            this.KillDriver();

        }


        private void YoutubeBtnClick(object sender, EventArgs e)
        {

            this.KillBrowser();
            DoLogin("youtube");
            this.KillDriver();

        }

       
    }
}
