<template>
  <div class="responsive-paddings">
    <oc-data-grid title="사이트 관리" resource="sites" :has-form="true" :show-refresh="true" 
      :show-bulk-delete="true" :enableImport="true">
      <oc-column data-field="id" caption="#" width="50" :visible="false"/>
      <oc-column data-field="name" caption="사이트명" width="200"/>
      <oc-column data-field="allow_login_count" caption="동시 로그인 허용" width="150"/>
      <oc-column data-field="login_url" caption="로그인 URL" width="250"/>
      <oc-column data-field="id_xpath" caption="ID XPATH" width="200"/>
      <oc-column data-field="pw_xpath" caption="PW XPATH" width="200"/>
      <oc-column data-field="login_xpath" caption="LOGIN XPATH" width="200"/>
      <dx-filter-row :visible="true" apply-filter="onClick"/>
      <dx-filter-panel :visible="true" />
      <dx-search-panel :visible="true"/>
      <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true" mode="batch"/>
      <dx-selection select-all-mode="page" show-check-boxes-mode="onClick" mode="multiple"/>
    </oc-data-grid>
  </div>
</template>