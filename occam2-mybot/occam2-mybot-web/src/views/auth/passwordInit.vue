<template>
  <dx-validation-group ref="validationGroup">
    <div class="login-header" >
      <div class="title" style="display: flex;justify-content: center;">{{ title }}</div>
      <p/>
    </div>
    <div class="dx-field">
      <dx-text-box placeholder="기존 비밀번호" width="100%" mode="password" :value.sync="oldPassword" v-if="!secureKey">
        <dx-validator>
          <dx-required-rule message="기존 비밀번호를 입력하세요." />
        </dx-validator>
      </dx-text-box>
    </div>
    <div class="dx-field">
      <dx-text-box placeholder="새 비밀번호" width="100%" mode="password" :value.sync="password">
        <dx-validator>
          <dx-required-rule message="새 비밀번호를 입력하세요." />
          <dx-stringLength-rule :min="8" :max="15" message="8-15자 이내의 영문과 숫자, 특수문자를 조합해서 설정해야 합니다."/>
          <dx-custom-rule :validation-callback="associatedPasswrod" message="시스템관 연관된 단어는 사용할 수 없습니다."/>
          <dx-custom-rule :validation-callback="defaultSpecialChar" message="기본적으로 제공되는 특수문자만 사용 가능합니다."/>
          <dx-custom-rule :validation-callback="mixPassword" message="비밀번호는 영문과 숫자, 특수문자 조합이어야 합니다."/>
          <dx-custom-rule :validation-callback="sameCharPassword" message="3자리 이상 반복되는 숫자나 문자, 특수문자는 사용하실 수 없습니다."/>
          <dx-custom-rule :validation-callback="ascDescPassword" message="3자리 이상 연속되는 숫자나 문자는 사용하실 수 없습니다."/>
        </dx-validator>
      </dx-text-box>
    </div>
    <div class="dx-field">
      <dx-text-box placeholder="새 비밀번호 확인" width="100%" mode="password" :value.sync="rePassword" @enterKey="onPasswordInitClick">
        <dx-validator>
          <dx-required-rule message="새 비밀번호를 재입력하세요." />
          <dx-compare-rule :comparison-target="passwordComparison" message="재입력한 비밀번호가 일치하지 않습니다."/>
        </dx-validator>
      </dx-text-box>
    </div>

    <div class="dx-field">
      <dx-button type="default" text="확인" width="100%" @click="onPasswordInitClick"/>
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
      title: "비밀번호 변경",
      password: "",
      rePassword: "",
      oldPassword: "",
    };
  },
  computed: {
     secureKey() {
      return this.$route.params.secureKey;
    }
  },
  methods: {
    associatedPasswrod(e) {
      if (0 == e.value.length) return !0;
      var value = e.value.toLowerCase();
      var b = !1; - 1 != value.search(/^((?!shilla|tlsfk|eksrl|test|eam|wktks).)*$/) && (b = !0);
      return b
    },
    defaultSpecialChar(e) {
      if (0 == e.value.length) return !0;
      var b = !1; - 1 != e.value.search(/^[a-zA-Z0-9\~\`\!\@\\\#\$\%\^\&\*\(\)\-\_\=\+\\\|\[\]\{\}\;\:\'\"\,\.\/\<\>\?]+$/) && (b = !0);
      return b
    },
    mixPassword(e) {
      if (0 == e.value.length) return !0;
      var b = !1; - 1 == e.value.search(/\d/g) || -1 == e.value.search(/^.*(?=[\~\`\!\@\\\#\$\%\^\&\*\(\)\-\_\=\+\\\|\[\]\{\}\;\:\'\"\,\.\/\<\>\?]).*$/gm) || -1 == e.value.search(/[a-z]/g) && -1 == e.value.search(/[A-Z]/g) || (b = !0);
      return b
    },
    sameCharPassword(e) {
      var a = e.value;
      if (3 > a.length) return !0;
      for (var b = a.split(""), c = a = 1; c < b.length && !(b[c - 1].toUpperCase() == b[c].toUpperCase() ? a++ : a = 1, 3 <= a); c++);
      b = !1;
      3 > a && (b = !0);
      return b
    },
    ascDescPassword(e) {
      if (3 > e.value.length) return !0;
      var b = e.value.split("");
      var a = 1;
      for (var c = "", d = 1; d < b.length; d++) {
          var f = " 1234567890 ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(b[d - 1].toUpperCase()),
              e = " 1234567890 ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(b[d].toUpperCase());
          f + 1 == e ? ("ASC" == c ? a++ : a = 2, c = "ASC") : f - 1 == e ? ("DESC" == c ? a++ : a = 2, c = "DESC") : a = 1;
          if (3 <= a) break
      }
      b = !1;
      3 > a && (b = !0);
      return b
    },
    passwordComparison(){
      return this.password;
    },
    onPasswordInitSelf(e) {
      this.service.execute("users",'any','passwordInit',{password: this.password, old_password: this.oldPassword}).then((data)=>{
        if (data.success) {
          notify({message: data.message}, 'info', 10000);
          this.$router.push("/login");
        } else {
          notify({message: data.message}, 'error', 10000);
        }
      })
    },
    onPasswordInitClick(e) {
      let result = e.validationGroup.validate();
      if (!result.isValid)
        return;
      let params = {password: this.password, old_password: this.oldPassword}
      if (this.secureKey)
        params = {password: this.password, secure_key: this.secureKey, filter: ["secure_key", "=", this.secureKey]}
      this.service.execute("users",'any','passwordInit', params).then((data)=>{
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