using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Net.Http;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Security.Cryptography;

namespace AgentUpdater
{
    public class Configuration
    {
        public string Version { get; set; }
        public string ServerUrl { get; set; }
    }

    public partial class Form1 : Form
    {
        private readonly HttpClient client;
        private readonly string configFile = "config.ini";
        private readonly string[] processesToKill = { "LoginAgent", "msedgedriver" };
        private bool isUpdating = false;

        public Form1()
        {
            InitializeComponent();
            InitializeCustomComponents();

            // HttpClient 설정
            client = new HttpClient
            {
                Timeout = TimeSpan.FromMinutes(5)
            };
        }

        private void InitializeCustomComponents()
        {
            this.ClientSize = new Size(300, 100);
            this.Text = "Agent Updater";
            this.StartPosition = FormStartPosition.CenterScreen;

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
                if (isUpdating) return;
                isUpdating = true;

                string localVersion = GetConfigValue("version");
                string serverUrl = GetConfigValue("updateServer");
                string serverVersion = await GetServerVersion(serverUrl);

                SetStatus($"현재 버전: {localVersion}");
                SetStatus($"서버 버전: {serverVersion}");

                if (CompareVersions(serverVersion, localVersion) > 0)
                {
                    await PerformUpdate(serverUrl, serverVersion);
                }
                else
                {
                    Application.Exit();
                }
            }
            catch
            {
                // 예외 발생 시 조용히 종료
                Application.Exit();
            }
            finally
            {
                isUpdating = false;
            }
        }

        private async Task PerformUpdate(string serverUrl, string newVersion)
        {
            string zipPath = null;
            try
            {
                zipPath = await DownloadUpdate(serverUrl, newVersion);
                await CloseRunningProcesses();
          
                ExtractUpdate(zipPath);
                RestartApplication();
            }
            catch
            {
                Application.Exit();
            }
            finally
            {
                if (zipPath != null && File.Exists(zipPath))
                {
                    try
                    {
                        File.Delete(zipPath);
                    }
                    catch
                    {
                        // 임시 파일 삭제 실패 시 무시
                    }
                }
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

        private async Task<string> GetServerVersion(string serverUrl)
        {
            try
            {
                string response = await client.GetStringAsync($"http://{serverUrl}/dist/_CHECK");
                if (string.IsNullOrWhiteSpace(response))
                {
                    // 내용이 없을 경우 프로그램 종료
                    Application.Exit();
                }
                return response.Trim();
            }
            catch
            {
                // 예외 발생 시 프로그램 종료
                Application.Exit();
                return null; // 컴파일러 경고 방지용
            }
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

        private async Task<string> DownloadUpdate(string serverUrl, string version)
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
                        await Task.Delay(1000); // 프로세스가 완전히 종료되기를 기다림
                    }
                    catch (Exception ex)
                    {
                        SetStatus($"{processName} 종료 실패: {ex.Message}");
                    }
                }
            }
        }

        

      

        private void ExtractUpdate(string zipPath)
        {
            SetStatus("파일 압축 해제 중...");
            ZipFile.ExtractToDirectory(zipPath, AppDomain.CurrentDomain.BaseDirectory, true);
        }

        private void RestartApplication()
        {
            string exePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "LoginAgent.exe");
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
            if (!IsDisposed && statusLabel != null)
            {
                if (InvokeRequired)
                    Invoke(new Action(() => statusLabel.Text = status));
                else
                    statusLabel.Text = status;
            }
        }

        private void SetProgress(int progress)
        {
            if (!IsDisposed && progressBar != null)
            {
                if (InvokeRequired)
                    Invoke(new Action(() => progressBar.Value = progress));
                else
                    progressBar.Value = progress;
            }
        }

        protected override void OnFormClosing(FormClosingEventArgs e)
        {
            base.OnFormClosing(e);
            client?.Dispose();
        }
    }
}