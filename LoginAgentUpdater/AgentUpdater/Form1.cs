using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Net.Http;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace AgentUpdater
{
    public partial class Form1 : Form
    {
        private readonly HttpClient client = new HttpClient();
        private string serverUrl;
        private readonly string configFile = "config.ini";
        private readonly string[] processesToKill = { "LoginAgent", "msedgedriver" };

        public Form1()
        {
            InitializeComponent();
            InitializeCustomComponents();
        }

        private void InitializeCustomComponents()
        {
            this.ClientSize = new Size(300, 100);
            this.Text = "Agent Updater";
            this.StartPosition = FormStartPosition.CenterScreen;

            // These controls are now defined in the Designer file
            statusLabel.Location = new Point(10, 10);
            statusLabel.Size = new Size(280, 20);
            statusLabel.Text = "업데이트 확인 중...";

            progressBar.Location = new Point(10, 40);
            progressBar.Size = new Size(280, 20);
            progressBar.Style = ProgressBarStyle.Continuous;
        }

        private async void Form1_Load(object sender, EventArgs e)
        {
            await CheckForUpdates();
        }

        private async Task CheckForUpdates()
        {
            try
            {
                string localVersion = GetConfigValue("version");
                serverUrl = GetConfigValue("updateServer");

                SetStatus($"현재 버전: {localVersion}");
                string serverVersion = await GetServerVersion();
                SetStatus($"서버 버전: {serverVersion}");

                if (CompareVersions(serverVersion, localVersion) > 0)
                {
                    SetStatus("새 버전이 있습니다. 업데이트를 시작합니다.");
                    string zipPath = await DownloadUpdate(serverVersion);
                    await CloseRunningProcesses();
                    ExtractUpdate(zipPath, AppDomain.CurrentDomain.BaseDirectory);
                    RestartApplication(AppDomain.CurrentDomain.BaseDirectory);
                }
                else
                {
                    SetStatus("최신 버전입니다");
                    await Task.Delay(1000);
                    Application.Exit();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"오류 발생: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                Application.Exit();
            }
        }

        private string GetConfigValue(string key)
        {
            if (!File.Exists(configFile))
                throw new FileNotFoundException("config.ini 파일을 찾을 수 없습니다.");

            foreach (string line in File.ReadLines(configFile))
            {
                if (line.StartsWith($"{key}="))
                    return line.Substring(key.Length + 1);
            }

            throw new FormatException($"config.ini 파일에서 {key} 정보를 찾을 수 없습니다.");
        }

        private async Task<string> GetServerVersion()
        {
            string response = await client.GetStringAsync($"http://{serverUrl}/dist/_CHECK");
            return response.Trim();
        }

        private int CompareVersions(string v1, string v2)
        {
            var version1 = v1.Split('.');
            var version2 = v2.Split('.');

            for (int i = 0; i < Math.Max(version1.Length, version2.Length); i++)
            {
                int num1 = i < version1.Length ? int.Parse(version1[i]) : 0;
                int num2 = i < version2.Length ? int.Parse(version2[i]) : 0;

                if (num1 < num2) return -1;
                if (num1 > num2) return 1;
            }

            return 0;
        }

        private async Task<string> DownloadUpdate(string version)
        {
            string zipFile = $"Login-Agent_{version}.zip";
            string downloadUrl = $"http://{serverUrl}/dist/{zipFile}";
            string zipPath = Path.Combine(Path.GetTempPath(), zipFile);

            SetStatus("파일 다운로드 중...");
            using (var response = await client.GetAsync(downloadUrl, HttpCompletionOption.ResponseHeadersRead))
            {
                response.EnsureSuccessStatusCode();
                long? totalBytes = response.Content.Headers.ContentLength;

                using (var stream = await response.Content.ReadAsStreamAsync())
                using (var fileStream = new FileStream(zipPath, FileMode.Create, FileAccess.Write, FileShare.None))
                {
                    var buffer = new byte[8192];
                    long totalBytesRead = 0;
                    int bytesRead;

                    while ((bytesRead = await stream.ReadAsync(buffer, 0, buffer.Length)) > 0)
                    {
                        await fileStream.WriteAsync(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;

                        if (totalBytes.HasValue)
                        {
                            int percentage = (int)((double)totalBytesRead / totalBytes.Value * 100);
                            SetProgress(percentage);
                        }
                    }
                }
            }

            return zipPath;
        }

        private async Task CloseRunningProcesses()
        {
            SetStatus("프로세스 종료 중...");
            foreach (var processName in processesToKill)
            {
                foreach (var process in Process.GetProcessesByName(processName))
                {
                    try
                    {
                        process.Kill();
                        await process.WaitForExitAsync();
                    }
                    catch (Exception ex)
                    {
                        SetStatus($"{processName} 종료 실패: {ex.Message}");
                    }
                }
            }
        }

        private void ExtractUpdate(string zipPath, string targetPath)
        {
            SetStatus("파일 압축 해제 중...");
            ZipFile.ExtractToDirectory(zipPath, targetPath, true);
            File.Delete(zipPath);
        }

       

        private void RestartApplication(string currentPath)
        {
            SetStatus("LoginAgent.exe 재시작 중...");
            string exePath = Path.Combine(currentPath, "LoginAgent.exe");
            if (File.Exists(exePath))
            {
                Process.Start(exePath);
                SetStatus("업데이트가 완료되었습니다. 프로그램이 재시작됩니다.");
                Application.Exit();
            }
            else
            {
                throw new Exception("업데이트 후 LoginAgent.exe를 찾을 수 없습니다.");
            }
        }

        private void SetStatus(string status)
        {
            if (InvokeRequired)
            {
                Invoke(new Action<string>(SetStatus), status);
                return;
            }
            statusLabel.Text = status;
        }

        private void SetProgress(int progress)
        {
            if (InvokeRequired)
            {
                Invoke(new Action<int>(SetProgress), progress);
                return;
            }
            progressBar.Value = progress;
        }
    }
}