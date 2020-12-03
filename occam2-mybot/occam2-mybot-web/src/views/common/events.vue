<template>
  <div class="responsive-paddings">
    <oc-data-grid title="사용 이력" resource="events" :show-refresh="true">
      <oc-column data-field="reg_date" target-type="datetime" sort-order="desc" caption="일시" :read-only="true" width="180"/>
      <oc-column data-field="owner_id" caption="사용자" calculate-display-value="owner__name" target-type="resource" type-id="users" width="80" :read-only="true"/>
      <oc-column data-field="group_id" caption="부서" calculate-display-value="ocgroup__name" target-type="resource" type-id="groups" width="160" :read-only="true"/>
      <oc-column data-field="remote_addr" caption="사용자 IP" width="140" :read-only="true"/>
      <oc-column data-field="resource_name" caption="리소스" width="140" target-type="code" type-id="resource" :read-only="true"/>
      <oc-column data-field="command_name" caption="명령" width="140" target-type="code" type-id="command" :read-only="true"/>
      <oc-column data-field="result_count" caption="결과 건수" width="100" target-type="integer" :read-only="true"/>
      <oc-column data-field="elapsed_ms" caption="소요 ms" target-type="integer" width="90" :read-only="true"/>
      <oc-column data-field="data_id" caption="데이터 ID" width="100" :read-only="true"/>
      <oc-column data-field="condition_val" caption="조회 조건" width="200" :read-only="true"/>
      <oc-column data-field="row_val" caption="입력 값" width="400" :read-only="true"/>
      <oc-column data-field="host_addr" caption="서버 IP" width="140" :read-only="true"/>
      <oc-column data-field="request_url" caption="요청 URL" width="300" :read-only="true"/>
      <oc-column data-field="request_method" caption="요청 Method" width="90" :read-only="true"/>
      <oc-column data-field="note" caption="비고" width="300" targetType="textArea"/>
      <dx-filter-row :visible="true" apply-filter="onClick"/>
      <dx-search-panel :visible="true"/>
      <dx-filter-panel :visible="true"/>
      <dx-export :enabled="true" :allow-export-selected-data="true" file-name="export"/>
      <dx-editing :allow-updating="true" mode="popup" :form="{items:[
        { dataField: 'note', colSpan: 2 },
        { dataField: 'reg_date' },
        { dataField: 'owner_id' },
        { dataField: 'group_id' },
        { dataField: 'remote_addr' },
        { dataField: 'resource_name' },
        { dataField: 'command_name' },
        { dataField: 'result_count' },
        { dataField: 'elapsed_ms' },
        { dataField: 'data_id' },
        { dataField: 'condition_val' },
        { dataField: 'row_val' },
        { dataField: 'host_addr' },
        { dataField: 'request_url' },
        { dataField: 'request_method' },
      ]}"/>
    </oc-data-grid>
  </div>
</template>