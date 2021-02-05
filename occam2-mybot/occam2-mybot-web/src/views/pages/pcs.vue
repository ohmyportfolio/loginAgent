<template>
  <div class="responsive-paddings">
    <oc-data-grid title="사이트 관리" resource="pcs" :has-form="false" :show-refresh="true" 
      :show-bulk-delete="true" :enableImport="true">
      <oc-column data-field="store_id" caption="매장명" width="200" target-type="resource" type-id="stores" calculate-display-value="store__name"/>
      <oc-column data-field="ip" caption="IP 주소" width="200"/>
      <oc-column data-field="name" caption="자리번호" width="200"/>
      
      <dx-filter-row :visible="true" apply-filter="onClick"/>
      <dx-filter-panel :visible="true" />
      <dx-search-panel :visible="true"/>
      <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true" mode="batch"/>
      <dx-selection select-all-mode="page" show-check-boxes-mode="onClick" mode="multiple"/>
    </oc-data-grid>
  </div>
</template>