<template>
  <div class="responsive-paddings">
    <oc-form title="계정" resource="sites" :col-count="4" selectMore="id">
      <oc-item data-field="id" caption="#" width="50" :visible="false"/>
      <oc-item data-field="name" caption="사이트명"/>
      <oc-item data-field="login_url" caption="로그인 URL" :col-span="2"/>
      <oc-item data-field="id_xpath" caption="ID XPATH" :col-span="4"/>
      <oc-item data-field="pw_xpath" caption="PW XPATH" :col-span="4"/>
      <oc-item data-field="login_xpath" caption="LOGIN XPATH" :col-span="4"/>
      <oc-item :col-span="4">
        <template #default>
          <oc-data-grid ref="grid" title="계정정보" resource="accounts" :show-refresh="true" :show-bulk-update="true"
            :show-bulk-delete="true" :enableImport="true" :filter="['site_id', '=', dataId]" @editorPreparing="onEditorPreparing">
           
            <oc-column data-field="name" caption="계정명" width="200" :visible="false"/>
            <oc-column data-field="user_id" caption="ID" width="200" />
            <oc-column data-field="user_password" caption="패스워드" width="100"/>
            <dx-filter-row :visible="true" apply-filter="onClick"/>
            <dx-export :enabled="true" :allow-export-selected-data="true" file-name="export"/>
            <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true" mode="batch"/>
          </oc-data-grid>
        </template>
      </oc-item>
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
