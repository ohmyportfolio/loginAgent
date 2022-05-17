namespace LoginAgent
{
    partial class Main
    {
        /// <summary>
        /// 필수 디자이너 변수입니다.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// 사용 중인 모든 리소스를 정리합니다.
        /// </summary>
        /// <param name="disposing">관리되는 리소스를 삭제해야 하면 true이고, 그렇지 않으면 false입니다.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form 디자이너에서 생성한 코드

        /// <summary>
        /// 디자이너 지원에 필요한 메서드입니다. 
        /// 이 메서드의 내용을 코드 편집기로 수정하지 마세요.
        /// </summary>
        private void InitializeComponent()
        {
            this.metroButton1 = new MetroFramework.Controls.MetroButton();
            this.disneyBtn = new MetroFramework.Controls.MetroButton();
            this.metroButton2 = new MetroFramework.Controls.MetroButton();
            this.tvingBtn = new MetroFramework.Controls.MetroButton();
            this.netFlixBtn = new MetroFramework.Controls.MetroButton();
            this.labelNetflix = new System.Windows.Forms.Label();
            this.labelTving = new System.Windows.Forms.Label();
            this.labelWavve = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // metroButton1
            // 
            this.metroButton1.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.metroButton1.FontSize = MetroFramework.MetroButtonSize.Tall;
            this.metroButton1.Highlight = true;
            this.metroButton1.Location = new System.Drawing.Point(355, 484);
            this.metroButton1.Name = "metroButton1";
            this.metroButton1.Size = new System.Drawing.Size(185, 55);
            this.metroButton1.Style = MetroFramework.MetroColorStyle.Black;
            this.metroButton1.TabIndex = 1;
            this.metroButton1.Text = "영상보기 종료";
            this.metroButton1.UseSelectable = true;
            this.metroButton1.Click += new System.EventHandler(this.CloseClick);
            // 
            // disneyBtn
            // 
            this.disneyBtn.BackgroundImage = global::LoginAgent.Resource1.disney;
            this.disneyBtn.Location = new System.Drawing.Point(673, 27);
            this.disneyBtn.Name = "disneyBtn";
            this.disneyBtn.Size = new System.Drawing.Size(198, 399);
            this.disneyBtn.TabIndex = 4;
            this.disneyBtn.UseSelectable = true;
            this.disneyBtn.Click += new System.EventHandler(this.disneyBtn_Click);
            // 
            // metroButton2
            // 
            this.metroButton2.BackgroundImage = global::LoginAgent.Resource1.wavve;
            this.metroButton2.Location = new System.Drawing.Point(458, 27);
            this.metroButton2.Name = "metroButton2";
            this.metroButton2.Size = new System.Drawing.Size(198, 399);
            this.metroButton2.TabIndex = 2;
            this.metroButton2.UseSelectable = true;
            this.metroButton2.Click += new System.EventHandler(this.WavveBtnClick);
            // 
            // tvingBtn
            // 
            this.tvingBtn.BackgroundImage = global::LoginAgent.Resource1.tving;
            this.tvingBtn.Location = new System.Drawing.Point(237, 26);
            this.tvingBtn.Name = "tvingBtn";
            this.tvingBtn.Size = new System.Drawing.Size(199, 399);
            this.tvingBtn.TabIndex = 0;
            this.tvingBtn.UseSelectable = true;
            this.tvingBtn.Click += new System.EventHandler(this.TvingBtnClick);
            // 
            // netFlixBtn
            // 
            this.netFlixBtn.BackgroundImage = global::LoginAgent.Resource1.netflix;
            this.netFlixBtn.Location = new System.Drawing.Point(21, 26);
            this.netFlixBtn.Name = "netFlixBtn";
            this.netFlixBtn.Size = new System.Drawing.Size(199, 400);
            this.netFlixBtn.TabIndex = 0;
            this.netFlixBtn.UseSelectable = true;
            this.netFlixBtn.Click += new System.EventHandler(this.NetFlixBtnClick);
            // 
            // labelNetflix
            // 
            this.labelNetflix.AutoSize = true;
            this.labelNetflix.Location = new System.Drawing.Point(93, 438);
            this.labelNetflix.Name = "labelNetflix";
            this.labelNetflix.Size = new System.Drawing.Size(31, 12);
            this.labelNetflix.TabIndex = 3;
            this.labelNetflix.Text = "0 / 0";
            this.labelNetflix.Visible = false;
            // 
            // labelTving
            // 
            this.labelTving.AutoSize = true;
            this.labelTving.Location = new System.Drawing.Point(326, 439);
            this.labelTving.Name = "labelTving";
            this.labelTving.Size = new System.Drawing.Size(31, 12);
            this.labelTving.TabIndex = 3;
            this.labelTving.Text = "0 / 0";
            this.labelTving.Visible = false;
            // 
            // labelWavve
            // 
            this.labelWavve.AutoSize = true;
            this.labelWavve.Location = new System.Drawing.Point(552, 439);
            this.labelWavve.Name = "labelWavve";
            this.labelWavve.Size = new System.Drawing.Size(31, 12);
            this.labelWavve.TabIndex = 3;
            this.labelWavve.Text = "0 / 0";
            this.labelWavve.Visible = false;
            // 
            // Main
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.metroButton1;
            this.ClientSize = new System.Drawing.Size(892, 551);
            this.Controls.Add(this.disneyBtn);
            this.Controls.Add(this.labelWavve);
            this.Controls.Add(this.labelTving);
            this.Controls.Add(this.labelNetflix);
            this.Controls.Add(this.metroButton2);
            this.Controls.Add(this.metroButton1);
            this.Controls.Add(this.tvingBtn);
            this.Controls.Add(this.netFlixBtn);
            this.MaximizeBox = false;
            this.Name = "Main";
            this.ShowIcon = false;
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private MetroFramework.Controls.MetroButton netFlixBtn;
        private MetroFramework.Controls.MetroButton tvingBtn;
        private MetroFramework.Controls.MetroButton metroButton1;
        private MetroFramework.Controls.MetroButton metroButton2;
        private MetroFramework.Controls.MetroButton disneyBtn;
        private System.Windows.Forms.Label labelNetflix;
        private System.Windows.Forms.Label labelTving;
        private System.Windows.Forms.Label labelWavve;
    }
}

