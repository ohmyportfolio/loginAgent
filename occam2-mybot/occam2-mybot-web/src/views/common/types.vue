<template>
  <div class="responsive-paddings">
    <dx-toolbar>
      <dx-item #default location="before" locate-in-menu="never">
        <div class="dx-toolbar-label">코드 관리</div>
      </dx-item>
    </dx-toolbar>
    <div class="h-box">
      <oc-data-grid resource="types" selection-mode="none" title="코드 타입" @row-click="type = $event.data" :has-form="true" :showEditButton="false" width="300">
        <oc-column data-field="id" :is-required="true" caption="코드 타입 ID" width="149"/>
        <oc-column data-field="name" :is-required="true" caption="코드 타입명" width="149"/>
        <dx-filter-row :visible="true" apply-filter="onClick"/>
      </oc-data-grid>
      <oc-data-grid resource="codes" :title="type.name" :column-auto-width="true" :key="type.id"
        :primary-key="['type_id', 'code']" :filter="['type_id', '=', type.id]" :default-data="{type_id: type.id}" :show-refresh="true">
        <oc-column data-field="code" :is-required="true" caption="코드" width="100"/>
        <oc-column data-field="name" :is-required="true" caption="코드명" width="100"/>
        <oc-column data-field="seq" target-type="integer" caption="순서" width="70"/>
        <oc-column data-field="parent_code" caption="상위 코드" width="100"/>
        <oc-column data-field="val_1" caption="값1" width="100"/>
        <oc-column data-field="val_2" caption="값2" width="100"/>
        <oc-column data-field="val_3" caption="값3" width="100"/>
        <oc-column data-field="val_4" caption="값4" width="100"/>
        <oc-column data-field="val_5" caption="값5" width="100"/>
        <oc-column data-field="val_6" caption="값6" width="100"/>
        <oc-column data-field="val_7" caption="값7" width="100"/>
        <oc-column data-field="val_8" caption="값7" width="100"/>
        <oc-column data-field="use_yn" target-type="code" type-id="yn" caption="사용" width="70"/>
        <oc-column data-field="reg_date" caption="등록일시" target-type="datetime" :readOnly="true" width="180"/>
        <oc-column data-field="reg_user__name" caption="등록자" :readOnly="true" width="120"/>
        <dx-filter-row :visible="true" apply-filter="onClick"/>
        <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true" mode="batch"/>
        <dx-export :enabled="true" file-name="export"/>
      </oc-data-grid>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      type: {id: "", name: "코드 타입을 선택하세요."}
    };
  }
}
</script>