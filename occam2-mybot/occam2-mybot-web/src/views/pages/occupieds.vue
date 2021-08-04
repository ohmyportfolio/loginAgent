<template>
  <div class="responsive-paddings">
    <oc-data-grid title="계정 사용 현황 관리" resource="occupieds" :has-form="false" :show-refresh="true" 
      :show-bulk-delete="true" :enableImport="true">
      <oc-column data-field="id" caption="#" width="50" :visible="false"/>
      <oc-column data-field="account_id" caption="계정 ID" width="200" target-type="resource" type-id="accounts" calculate-display-value="account__user_id" :read-only="true"/>
      <oc-column data-field="site_id" caption="사이트" width="200" target-type="resource" type-id="sites" calculate-display-value="site__name" :read-only="true"/>
      <oc-column data-field="pc_ip" caption="PC IP" width="200" :read-only="true"/>
      <oc-column data-field="pc__name" caption="자리번호" width="200" target-type="resource" type-id="pcs" :read-only="true"/>
      <oc-column data-field="pc__store__name" caption="매장" width="200" target-type="resource" type-id="pcs" :read-only="true"/>
      <oc-column data-field="start_date" caption="시작시간" width="200" target-type="datetime" :read-only="true"/>
      <dx-filter-row :visible="true" apply-filter="onClick"/>
      <dx-filter-panel :visible="true" />
      <dx-search-panel :visible="true"/>
      <dx-editing :allow-adding="false" :allow-updating="false" :allow-deleting="true" mode="batch"/>
    </oc-data-grid>
  </div>
</template>