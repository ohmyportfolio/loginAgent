<template>
  <div class="responsive-paddings">
    <oc-form title="그룹" resource="groups" :default-data="{ group_type: 'dept', group_status: 'normal' }" :col-count="2">      
      <oc-item data-field="id"/>
      <oc-item data-field="name" :is-required="true"/>
      <oc-item data-field="group_type" target-type="code" type-id="group_type" :is-required="true"/>
      <oc-item data-field="group_status" target-type="code" type-id="group_status" :is-required="true"/>
      <oc-item data-field="locale"/>
      <oc-item data-field="timezone"/>
      <oc-item data-field="reg_date" :read-only="true" target-type="datetime"/>
      <oc-item data-field="reg_user_id" :read-only="true"/>
    </oc-form>
  </div>
</template>