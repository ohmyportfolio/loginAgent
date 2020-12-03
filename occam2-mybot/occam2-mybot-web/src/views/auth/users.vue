<template>
  <div class="responsive-paddings">
    <oc-data-grid title="사용자 관리" resource="users" :has-form="true" :show-refresh="true">
      <oc-column data-field="id" caption="ID" width="140" :visible="false"/>
      <oc-column data-field="user_id" caption="사용자ID" width="140"/>
      <oc-column data-field="user_name" caption="이름" width="140"/>
      <oc-column data-field="user_type" caption="사용자 유형" target-type="code" type-id="user_type" width="100"/>
      <oc-column data-field="user_status" caption="상태" target-type="code" type-id="user_status" width="100"/>
      <oc-column data-field="group_id" caption="소속 부서" calculate-display-value="ocgroup__name" target-type="resource" type-id="groups" width="160"/>
      <oc-column data-field="login_failure_count" caption="로그인 실패 횟수" target-type="integer" :visible="false"/>
      <oc-column data-field="blocked_date" caption="잠금일시" target-type="datetime" :visible="false"/>
      <oc-column data-field="expiration_date" caption="만료일시" target-type="datetime" :visible="false"/>
      <oc-column data-field="password_expiration_date" caption="비밀번호 만료일시" target-type="datetime" :visible="false"/>
      <oc-column data-field="reg_date" target-type="datetime" sort-order="desc" caption="등록일시" :read-only="true" width="180"/>
      <oc-column data-field="reg_user__name" caption="등록자" :read-only="true" width="100"/>
      <oc-column data-field="mod_date" caption="수정일시" target-type="datetime" :read-only="true" width="180"/>
      <oc-column data-field="mod_user__name" caption="수정자" :read-only="true" width="100"/>
      <dx-filter-row :visible="true" apply-filter="onClick"/>
      <dx-search-panel :visible="true"/>
      <dx-filter-panel :visible="true"/>
      <dx-export :enabled="true" :allow-export-selected-data="true" file-name="export"/>
      <dx-editing :allow-adding="true" :allow-updating="true" :allow-deleting="true"/>
    </oc-data-grid>
  </div>
</template>