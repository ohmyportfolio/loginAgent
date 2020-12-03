<template>
  <div class="responsive-paddings">
    <oc-form title="사용자" resource="users" :default-data="{ user_type: 'employee' }" :col-count="4" selectMore="id">
      <oc-item data-field="user_id" caption="사용자ID" :is-required="true" :read-only="!isNew()"/>
      <oc-item data-field="user_name" caption="이름" :is-required="true" :col-span="2"/>
      <oc-item data-field="password" caption="비밀번호" :editor-options="{ mode: 'password' }" :except-select="true"/>
      <oc-item data-field="user_type" caption="사용자 유형" target-type="code" type-id="user_type"/>
      <oc-item data-field="user_status" caption="사용자 상태" target-type="code" type-id="user_status"/>
      <oc-item data-field="email" caption="E-Mail"/>
      <oc-item data-field="locale" caption="언어"/>
      <oc-item data-field="timezone" caption="시간대" />
      <oc-item data-field="group_id" caption="소속부서" target-type="resource" type-id="groups"/>
      <oc-item data-field="group_ids" caption="겸임부서" target-type="resourceTag" type-id="groups"/>
      <oc-item data-field="place_id" caption="기본 장소" target-type="resource" type-id="places"/>
      <oc-item data-field="checklist_id" caption="기본 체크리스트" target-type="resource" type-id="checklists"/>
      <oc-item data-field="acl_id" caption="권한 그룹" target-type="resource" type-id="acls"/>
      <oc-item data-field="login_failure_count" caption="로그인 실패 횟수" target-type="integer" />
      <oc-item data-field="blocked_date" caption="잠금일시" target-type="datetime"/>
      <oc-item data-field="expiration_date" caption="만료일시" target-type="datetime"/>
      <oc-item data-field="password_expiration_date" caption="비밀번호 만료일시" target-type="datetime"/>
      <oc-item data-field="reg_date" caption="등록일시" target-type="datetime" :visible="!isNew()" :read-only="true"/>
      <oc-item data-field="reg_user__name" caption="등록자" :visible="!isNew()" :read-only="true"/>
      <oc-item data-field="mod_date" caption="수정일시" target-type="datetime" :visible="!isNew()" :read-only="true"/>
      <oc-item data-field="mod_user__name" caption="수정자" :visible="!isNew()" :read-only="true"/>
    </oc-form>
  </div>
</template>