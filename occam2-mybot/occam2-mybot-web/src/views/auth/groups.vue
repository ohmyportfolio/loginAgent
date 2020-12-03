<template>
  <div class="responsive-paddings">
    <oc-data-grid title="그룹 관리" resource="groups" :has-form="true" :show-refresh="true">
      <oc-column data-field="id" />
      <oc-column data-field="name" />
      <oc-column data-field="group_type" target-type="code" type-id="group_type" />
      <oc-column data-field="group_status" target-type="code" type-id="group_status" />
      <oc-column data-field="member_count" target-type="integer" />
      <oc-column data-field="reg_date" target-type="datetime" sort-order="desc" />
      <oc-column data-field="reg_user_id" />
      <dx-filter-row :visible="true" apply-filter="onClick"/>
      <dx-search-panel :visible="true"/>
      <dx-filter-panel :visible="true"/>
      <dx-export :enabled="true" :allow-export-selected-data="true" file-name="export"/>
      <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true"/>
    </oc-data-grid>
  </div>
</template>