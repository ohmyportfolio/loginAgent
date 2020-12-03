<template>
  <div class="responsive-paddings">
    <oc-form title="계정" resource="accounts" :col-count="4" selectMore="id">
      <oc-item data-field="id" caption="#" :visible="false"/>
      <oc-item data-field="site_id" caption="사이트명" target-type="resource" type-id="sites" :is-required="true"/>
      <oc-item data-field="user_id" caption="ID"/>
      <oc-item data-field="user_password" caption="비밀번호"/>
    </oc-form>
  </div>
</template>
<script>

export default {
  data() {
    return {
     
    };
  },
  methods: {
  }
}
</script>
