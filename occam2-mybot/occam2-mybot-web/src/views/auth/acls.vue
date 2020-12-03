<template>
  <div class="responsive-paddings">
    <oc-data-grid title="권한 관리" resource="acls" :has-form="true" :show-refresh="true">
      <oc-column data-field="id" caption="권한ID" :is-required="true"/>
      <oc-column data-field="name" caption="권한명" :is-required="true"/>
      <oc-column data-field="admin_permit" caption="관리자 권한" target-type="code" type-id="permit"/>
      <oc-column data-field="owner_permit" caption="소유자 권한" target-type="code" type-id="permit"/>
      <oc-column data-field="group_permit" caption="부서원 권한" target-type="code" type-id="permit"/>
      <oc-column data-field="other_permit" caption="임직원 권한" target-type="code" type-id="permit"/>
      <oc-column data-field="guest_permit" caption="외부인 권한" target-type="code" type-id="permit"/>
      <oc-column data-field="reg_date" target-type="datetime" sort-order="desc" caption="등록일시" :read-only="true" width="180"/>
      <oc-column data-field="reg_user__name" caption="등록자" :read-only="true" width="100"/>
      <oc-column data-field="mod_date" caption="수정일시" target-type="datetime" :read-only="true" width="180"/>
      <oc-column data-field="mod_user__name" caption="수정자" :read-only="true" width="100"/>
      <dx-filter-row :visible="true" apply-filter="onClick"/>
      <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true"/>
    </oc-data-grid>
  </div>
</template>