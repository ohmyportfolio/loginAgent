using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

namespace LoginAgent
{
    public static class ProcessUtils
    {
        // 프로세스 이름을 인자로 받아 종료하는 메서드
        public static void KillProcessByName(string processName)
        {
            string taskKillPath = Path.Combine(Application.StartupPath, "taskkill.exe");
            try
            {
                Process.Start(new ProcessStartInfo
                {
                    FileName = taskKillPath,
                    Arguments = $"/im {processName} /f",
                    CreateNoWindow = true,
                    UseShellExecute = false
                });
            }
            catch (Exception e)
            {
                Console.WriteLine($"Error :: Could not kill process {processName}: {e.Message}");
            }
        }
    }
}
