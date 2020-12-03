<template>
  <div class="responsive-paddings">
    <oc-form title="공통 코드" ref="form" resource="types" 
      :default-data="{component_type: 'DataGrid', code_yn: 'y'}">
      <dx-group-item :col-count="4" caption="기본 정보">
        <oc-item caption="ID" data-field="id" :is-required="true"/>
        <oc-item caption="이름" data-field="name" :is-required="true"/>
        <oc-item caption="타입 유형" data-field="type_cat" target-type="code" type-id="type_cat" :is-required="true"/>
        <oc-item data-field="seq" caption="순서" target-type="integer"/>
      </dx-group-item>
      <dx-group-item caption="열" class="flex">
        <oc-data-grid slot-scope="_" :default-data="{ target_type: 'string' }"
          resource="columns" :server-side="false" data-field="columns" :primary-key="['type_id', 'column_id']"
          :filter="['type_id', '=', dataId]">
          <oc-column data-field="column_id" caption="열 ID" :is-required="true"/>
          <oc-column data-field="name" caption="열 이름" :is-required="true"/>
          <oc-column data-field="seq" caption="순서" target-type="integer"/>
          <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true" mode="cell"/>
        </oc-data-grid>
      </dx-group-item>
    </oc-form>
  </div>
</template>