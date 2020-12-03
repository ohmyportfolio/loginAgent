<template>
  <div class="responsive-paddings">
    <oc-data-grid title="계정 관리" resource="accounts" :has-form="true" :show-refresh="true" 
      :show-bulk-delete="true" :enableImport="true">
      <oc-column data-field="id" caption="#" width="50" :visible="false"/>
      <oc-column data-field="site_id" caption="사이트명" width="130" target-type="resource" type-id="sites" calculate-display-value="site__name"/>
      <oc-column data-field="user_id" caption="아이디" width="250"/>
      <oc-column data-field="user_password" caption="비밀번호" width="200" />
      <!-- :editor-options="{ mode: 'password' }" -->
      <dx-filter-row :visible="true" apply-filter="onClick"/>
      <dx-filter-panel :visible="true" />
      <dx-search-panel :visible="true"/>
      <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true" mode="batch"/>
      <dx-selection select-all-mode="page" show-check-boxes-mode="onClick" mode="multiple"/>
    </oc-data-grid>
  </div>
</template>