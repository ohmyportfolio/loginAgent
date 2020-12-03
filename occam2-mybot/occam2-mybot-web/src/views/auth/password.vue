<template>
  <oc-form ref="form" class="responsive-paddings" selectMore="is_password_use"  title="패스워드 변경" :col-count="3" 
    :formData.sync="formData" :showCloseButton="false" :key="formData.is_password_use"
    resource="users" saveCommand="updatePassword" :dataId="auth.user.id" @saved="close">
    <oc-item data-field="old_password" caption="기존 비밀번호" :editor-options="{ mode: 'password' }" :except-select="true" :visible="formData.is_password_use == 1">
      <dx-required-rule message="기존 비밀번호를 입력하세요." />
    </oc-item>
    <oc-item data-field="password" caption="비밀번호" :editor-options="{ mode: 'password' }" :except-select="true">

      <dx-required-rule message="새 비밀번호를 입력하세요." />
      <dx-stringLength-rule :min="8" :max="15" message="8-15자 이내의 영문과 숫자, 특수문자를 조합해서 설정해야 합니다."/>
      <dx-custom-rule :validation-callback="associatedPasswrod" message="시스템관 연관된 단어는 사용할 수 없습니다."/>
      <dx-custom-rule :validation-callback="defaultSpecialChar" message="기본적으로 제공되는 특수문자만 사용 가능합니다."/>
      <dx-custom-rule :validation-callback="mixPassword" message="비밀번호는 영문과 숫자, 특수문자 조합이어야 합니다."/>
      <dx-custom-rule :validation-callback="sameCharPassword" message="3자리 이상 반복되는 숫자나 문자, 특수문자는 사용하실 수 없습니다."/>
      <dx-custom-rule :validation-callback="ascDescPassword" message="3자리 이상 연속되는 숫자나 문자는 사용하실 수 없습니다."/>

    </oc-item>
    <oc-item data-field="re_password" caption="비밀번호 확인" :editor-options="{ mode: 'password' }" :except-select="true">
      <dx-required-rule message="새 비밀번호를 재입력하세요." />
      <dx-compare-rule :comparison-target="() => formData.password" message="재입력한 비밀번호가 일치하지 않습니다."/>
    </oc-item>
  </oc-form>
</template>
<script>
export default {
  methods: {
    associatedPasswrod(e) {
      if (0 == e.value.length) return !0;
      var value = e.value.toLowerCase();
      var b = !1; - 1 != value.search(/^((?!test).)*$/) && (b = !0);
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
    }
  }
};
</script>