<template>
  <div class="responsive-paddings">
    <oc-tree-list title="메뉴 관리" resource="menus" parent-id-expr="parent_id" root-value="root"
      :show-refresh="true">
      <oc-column data-field="id" caption="ID" :is-required="true"/>
      <oc-column data-field="name" caption="메뉴명" :is-required="true"/>
      <oc-column data-field="icon" caption="아이콘"/>
      <oc-column data-field="path" caption="경로"/>
      <oc-column data-field="seq" caption="순서" target-type="integer" sort-order="asc"/>
      <oc-column data-field="parent_id" :visible="false"/>
      <oc-column data-field="group_ids" caption="접근 허용 그룹" target-type="resourceTag" type-id="groups" :visible="false"/>
      <oc-column data-field="use_yn" caption="사용여부" target-type="code" type-id="yn" />
      <oc-column data-field="reg_user_id" caption="등록자" :read-only="true"/>
      <oc-column data-field="reg_date" caption="등록일" target-type="datetime" :read-only="true"/>
      <dx-filter-row :visible="true" apply-filter="onClick"/>
      <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true" mode="popup"/>
    </oc-tree-list>
  </div>
</template>