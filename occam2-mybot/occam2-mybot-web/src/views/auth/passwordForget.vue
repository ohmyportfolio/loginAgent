<template>
  <dx-validation-group ref="validationGroup">
    <div class="login-header" >
      <div class="title" style="display: flex;justify-content: center;">{{ title }}</div>
      <p/>

    </div>

    <div class="dx-field">
      <dx-text-box placeholder="User Id" width="100%" :value.sync="userId">
        <dx-validator>
          <dx-required-rule message="Login is required" />
        </dx-validator>
      </dx-text-box>
    </div>

    <div class="dx-field">
      <dx-text-box
        placeholder="Email"
        width="100%"
        :value.sync="email" @enterKey="onPasswordInitMailClick">
        <dx-validator>
          <dx-required-rule message="Email is required"/>
          <dx-email-rule message="Email is invalid"/>
        </dx-validator>
      </dx-text-box>
    </div>
    <div class="dx-field">
      <dx-button
        type="default"
        text="Password Init"
        width="100%"
        @click="onPasswordInitMailClick"
      />
    </div>
    <div class="dx-field">
      <dx-button
        type="info"
        text="Login"
        width="100%"
        @click="onLoginClick"
      />
    </div>
      <div class="dx-field" style="display: flex;justify-content: center;">
      <img src="../../assets/logo.png"/>
    </div>
  </dx-validation-group>
</template>

<script>
import notify from 'devextreme/ui/notify';

export default {
  data() {
    return {
      title: "패스워드 재설정",
      userId: "",
      email: ""
    };
  },
  methods: {
    onLoginClick() {
      this.$router.push("/login");
    },

    onPasswordInitMailClick(e) {
      let result = e.validationGroup.validate();
      if (!result.isValid)
        return;
      this.service.execute("users",'any','passwordInitMail',{id: this.userId, email: this.email}).then((data)=>{
        debugger
        if (data.success) {
          notify({message: data.message}, 'info', 10000);
          this.$router.push("/login");
        } else {
          notify({message: data.message}, 'error', 10000);
        }
      })
    }
  }
};
</script>