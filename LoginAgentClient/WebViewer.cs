using Microsoft.Web.WebView2.WinForms;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace LoginAgent
{
    public partial class WebViewer : Form
    {

        public WebView2 WebView21
        {
            get { return webView21; }
            set { webView21 = value; }
        }

        public WebViewer()
        {
            InitializeComponent();
        }
    }
}
