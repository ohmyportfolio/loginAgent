<template>
  <div class="responsive-paddings">
    <oc-data-grid title="사이트 관리" resource="stores" :has-form="false" :show-refresh="true" 
      :show-bulk-delete="true" :enableImport="true">
      <oc-column data-field="id" caption="#" width="50" :visible="false"/>
      <oc-column data-field="name" caption="매장명" width="200"/>
      <oc-column data-field="addr" caption="주소" width="150"/>
      <oc-column data-field="tel" caption="전화번호" width="150"/>
      <dx-filter-row :visible="true" apply-filter="onClick"/>
      <dx-filter-panel :visible="true" />
      <dx-search-panel :visible="true"/>
      <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true" mode="batch"/>
    </oc-data-grid>
  </div>
</template>