using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LoginAgent
{
    public static class ProcessUtils
    {
        public static void KillDriver()
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
    }
}