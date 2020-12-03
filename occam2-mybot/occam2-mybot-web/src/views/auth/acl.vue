<template>
  <div class="responsive-paddings">
    <oc-form title="권한" ref="form" resource="acls" :col-count="4"
      :default-data="{owner_permit: '1', group_permit: '1', other_permit: '1'}">
      <oc-item caption="권한ID" data-field="id" :is-required="true"/>
      <oc-item caption="권한명" data-field="name" :col-span="2" :is-required="true"/>
      <oc-item caption="관리자 권한" data-field="admin_permit" target-type="code" type-id="permit" :is-required="true">
        <dx-compare-rule :comparison-target="() => '0'" comparison-type="!=" message="상속은 선택할 수 없습니다"/>
      </oc-item>
      <oc-item caption="소유자 권한" data-field="owner_permit" target-type="code" type-id="permit" :is-required="true">
        <dx-compare-rule :comparison-target="() => '0'" comparison-type="!=" message="상속은 선택할 수 없습니다"/>
      </oc-item>
      <oc-item caption="부서원 권한" data-field="group_permit" target-type="code" type-id="permit" :is-required="true">
        <dx-compare-rule :comparison-target="() => '0'" comparison-type="!=" message="상속은 선택할 수 없습니다"/>
      </oc-item>
      <oc-item caption="임직원 권한" data-field="other_permit" target-type="code" type-id="permit" :is-required="true">
        <dx-compare-rule :comparison-target="() => '0'" comparison-type="!=" message="상속은 선택할 수 없습니다"/>
      </oc-item>
      <oc-item caption="외부인 권한" data-field="guest_permit" target-type="code" type-id="permit" :is-required="true">
        <dx-compare-rule :comparison-target="() => '0'" comparison-type="!=" message="상속은 선택할 수 없습니다"/>
      </oc-item>
      <oc-item :col-span="4">
        <template #default>
          <oc-data-grid v-if="!isNew()"
            resource="aclTypes" data-field="aclTypes"
            :filter="['acl_id', '=', dataId]"
            :primary-key="['acl_id', 'type_id']">
            <oc-column data-field="type_id" caption="타입" />
            <oc-column data-field="admin_permit" caption="관리자 권한" target-type="code" type-id="permit"/>
            <oc-column data-field="owner_permit" caption="소유자 권한" target-type="code" type-id="permit"/>
            <oc-column data-field="group_permit" caption="부서원 권한" target-type="code" type-id="permit"/>
            <oc-column data-field="other_permit" caption="임직원 권한" target-type="code" type-id="permit"/>
            <oc-column data-field="guest_permit" caption="외부인 권한" target-type="code" type-id="permit"/>
            <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true" mode="batch"/>
          </oc-data-grid>
        </template>
      </oc-item>
    </oc-form>
  </div>
</template>