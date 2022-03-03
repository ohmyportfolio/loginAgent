using System;
using System.Windows.Forms;
using System.Net;
using System.IO;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Text;
using OpenQA.Selenium.Edge;
using OpenQA.Selenium;

namespace LoginAgent
{
    public partial class Main : Form
    {

        public Action KillBrowser;

        public Action KillDriver;

        public Main()
        {
            InitializeComponent();

            SiteUsageStatus();
        }

        
        private void NetFlixBtnClick(object sender, EventArgs e)
        {
            
            this.KillBrowser();
            DoLogin("netflix");
            SiteUsageStatus();
            this.KillDriver();

        }

        private void WavveBtnClick(object sender, EventArgs e)
        {
            
            this.KillBrowser();
            DoLogin("wavve");
            SiteUsageStatus();
            this.KillDriver();
        }


        private void TvingBtnClick(object sender, EventArgs e)
        {
            
            this.KillBrowser();
            DoLogin("tving");
            SiteUsageStatus();
            this.KillDriver();
        }

        private void LoginSite(JObject data)
        {


            string id = data.GetValue("user_id").ToString();
            string pw = data.GetValue("user_password").ToString();
            string siteId = data.GetValue("id").ToString();

            pw = AppHelper.Decrypt(pw, AppHelper.M_K);

            string url = data.GetValue("login_url").ToString();
            string idXpath = data.GetValue("id_xpath").ToString();
            string pwXpath = data.GetValue("pw_xpath").ToString();
            string logXpath = data.GetValue("login_xpath").ToString();

            
            EdgeDriverService _driverService = null;
            EdgeOptions _options = null;
            EdgeDriver _driver = null;

            _driverService = EdgeDriverService.CreateDefaultService();

            if (AppHelper.GetWebDriverDebugMode() == "true")
            {
                _driverService.HideCommandPromptWindow = false;
                _driverService.UseVerboseLogging = true;
            }
            else
            {
                _driverService.HideCommandPromptWindow = true;
                _driverService.UseVerboseLogging = false;
            }

            _options = new EdgeOptions();

            var userDataPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Microsoft\\Edge\\User Data");
            _options.AddArguments("user-data-dir=" + userDataPath);

            
            
            //_options.UseInPrivateBrowsing = true;
            //_options.AddArguments("--disable-notifications --disable-infobars --start-maximized");

            _options.AddArguments("--disable-session-crashed-bubble");


            _options.AddExcludedArgument("enable-automation");
            _options.AddAdditionalCapability("useAutomationExtension", false);
            _options.AddUserProfilePreference("credentials_enable_service", false);
            _options.AddUserProfilePreference("profile.password_manager_enabled", false);

            _options.AddUserProfilePreference("profile.exited_cleanly", true);
            _options.AddUserProfilePreference("profile.exit_type", "Normal");



            _driver = new EdgeDriver(_driverService, _options);
            _driver.Navigate().GoToUrl(url); // 웹 사이트에 접속합니다.
            _driver.Manage().Timeouts().ImplicitWait = TimeSpan.FromSeconds(10);
            
            
            if(siteId == "disney")
            {

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

            /*
             * todo : selenium 으로 tag remove 하는 방법
             * https://stackoverflow.com/questions/53376933/how-to-remove-an-element-attribute-using-selenium-and-c 참고
             * 
            WebDriverWait wait = new WebDriverWait(_driver, TimeSpan.FromSeconds(10));
            wait.Until(driver => driver.Url.Contains("browse"));
            Console.WriteLine("Wailttttttttttttttttttttttttttttttttt");
            element = _driver.FindElementByXPath("//*[@class='account-menu-item']");
            Console.WriteLine(element.ToString());
            */

        }

        private void DoLogin(string site)
        {
            JObject data = GetObjectData("http://" + AppHelper.GetServerUrl() + "/api/sites/" + site + "/selectAvailableAccountSeq?select=accounts" + "&pc_ip=" + AppHelper.GetLocalIp());

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

        private JObject GetObjectData(string uri)
        {
            var webRequest = (HttpWebRequest)WebRequest.Create(uri);
            var webResponse = (HttpWebResponse)webRequest.GetResponse();
            var reader = new StreamReader(webResponse.GetResponseStream());
            string s = reader.ReadToEnd();
            return JsonConvert.DeserializeObject<JObject>(s);
        }

        private JArray GetListData(string uri)
        {
            var webRequest = (HttpWebRequest)WebRequest.Create(uri);
            var webResponse = (HttpWebResponse)webRequest.GetResponse();
            var reader = new StreamReader(webResponse.GetResponseStream());
            string s = reader.ReadToEnd();
            return JsonConvert.DeserializeObject<JArray>(s);
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

        private void SiteUsageStatus()
        {
            JObject result = GetObjectData("http://" + AppHelper.GetServerUrl() + "/api/sites/any/getUsedAcountCount");
            JArray accounList = (JArray)result.GetValue("data");

            foreach (JObject row in accounList)
            {
                Console.WriteLine(row.GetValue("id"));
                Console.WriteLine(row.GetValue("total_cnt"));
                Console.WriteLine(row.GetValue("use_cnt"));
                row.GetValue("id").ToString().Equals("netflix");
                if (row.GetValue("id").ToString().Equals("netflix"))
                {
                    labelNetflix.Text = row.GetValue("use_cnt").ToString() + " / " + row.GetValue("total_cnt").ToString();
                }
                else if (row.GetValue("id").ToString().Equals("tving"))
                {
                    labelTving.Text = row.GetValue("use_cnt").ToString() + " / " + row.GetValue("total_cnt").ToString();
                }
                else if (row.GetValue("id").ToString().Equals("wavve"))
                {
                    labelWavve.Text = row.GetValue("use_cnt").ToString() + " / " + row.GetValue("total_cnt").ToString();
                }
            }
            
        }

        private void disneyBtn_Click(object sender, EventArgs e)
        {

            this.KillBrowser();
            DoLogin("disney");
            SiteUsageStatus();
            this.KillDriver();

        }
    }
}
