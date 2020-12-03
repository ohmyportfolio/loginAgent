<template>
  <form>
    <dx-validation-group ref="validationGroup">
      <div class="login-header" >
        <div class="title" style="display: flex;justify-content: center;">{{ $appInfo.title }}</div>
        <p/>

      </div>

      <div class="dx-field">
        <dx-text-box placeholder="아이디" width="100%" :value.sync="userId">
          <dx-validator>
            <dx-required-rule message="Login is required" />
          </dx-validator>
        </dx-text-box>
      </div>

      <div class="dx-field">
        <dx-text-box
          placeholder="비밀번호"
          width="100%"
          mode="password"
          :value.sync="password" @enterKey="onLoginClick"
        >
          <dx-validator>
            <dx-required-rule message="Password is required" />
          </dx-validator>
        </dx-text-box>
      </div>

      <div class="dx-field">
        <dx-button
          type="default"
          text="로그인"
          width="100%"
          @click="onLoginClick"
        />
      </div>
      <!--
      <div class="dx-field">
        <dx-button
          type="info"
          text="Password Reset"
          width="100%"
          @click="onPasswordForgetClick"
        />
      </div>
      -->
        <div class="dx-field" style="display: flex;justify-content: center;">
        <img src="../../assets/logo.png"/>
      </div>
    </dx-validation-group>
  </form>
</template>

<script>
export default {
  data() {
    return {
      userId: "",
      password: "",
      rememberUser: false
    };
  },
  methods: {
    async onLoginClick(e) {
      window.focus();
      let validationGroup = this.$refs.validationGroup.instance;
      if (!validationGroup.validate().isValid) {
        return;
      }

      let result = await this.auth.login(this.userId, this.password);
      if(result) {
        this.$router.push(this.$route.query.redirect || "/checks");
        validationGroup.reset();
      }
    },
    onPasswordForgetClick(e) {
      this.$router.push("/password-forget");
    }
  }
};
</script>
