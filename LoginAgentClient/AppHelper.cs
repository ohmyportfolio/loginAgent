using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace LoginAgent
{
    static class AppHelper
    {
        public static string GetLocalIp()
        {
            IPHostEntry host = Dns.GetHostEntry(Dns.GetHostName());
            string ip4Addr = string.Empty;

            foreach (IPAddress ip in host.AddressList)
            {
                if (ip.AddressFamily == AddressFamily.InterNetwork)
                {
                    ip4Addr = ip.ToString();
                    Console.WriteLine(ip4Addr);
                }
            }
            return ip4Addr;
        }

        public static string GetServerUrl()
        {
            IniFile ini = new IniFile();
            ini.Load("config.ini");
            String url = ini["server"]["url"].GetString();
            Console.WriteLine("server url = " + url);
            return url;
        }
    }
}
