<template>
  <div class="user-panel">
    <div class="user-info">
      <i class="dx-icon-user" style="font-size: 26px"/>
      <div class="user-name">{{ user.name }}</div>
    </div>

    <dx-context-menu
      v-if="menuMode === 'context'"
      target=".user-button"
      :items="menuItems"
      :width="170"
      :position="menuPositionConfig"
      show-event="dxclick"
      css-class="user-menu"
    />

    <dx-list
      v-if="menuMode === 'list'"
      class="dx-toolbar-menu-action"
      :items="menuItems"
    />
  </div>
</template>

<script>
import DxContextMenu from "devextreme-vue/context-menu";
import DxList from "devextreme-vue/list";
import auth from "../services/auth";

export default {
  props: {
    menuMode: String,
    menuItems: Array
  },
  data() {
    return {
      menuPositionConfig: {
        my: "top center",
        at: "bottom center"
      },
      user: auth.user
    };
  },
  components: {
    DxContextMenu,
    DxList
  }
};
</script>

<style lang="scss">
@import "../themes/generated/variables.base.scss";

.user-info {
  display: flex;
  align-items: center;

  .dx-toolbar-menu-section & {
    padding: 10px 6px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  }
  
  .user-name {
    font-size: 14px;
    color: $base-text-color;
    margin: 0 9px;
  }
}

.user-panel {
  .dx-list-item .dx-icon {
    vertical-align: middle;
    color: rgba(0, 0, 0, 0.87);
    margin-right: 16px;
  }
  .dx-rtl .dx-list-item .dx-icon {
    margin-right: 0;
    margin-left: 16px;
  }
}

.dx-context-menu.user-menu.dx-menu-base {
  &.dx-rtl {
    .dx-submenu .dx-menu-items-container .dx-icon {
      margin-left: 16px;
    }
  }
  .dx-submenu .dx-menu-items-container .dx-icon {
    margin-right: 16px;
  }
  .dx-menu-item .dx-menu-item-content {
    padding: 3px 15px 4px;
  }
}

.dx-theme-generic .user-menu .dx-menu-item-content .dx-menu-item-text {
  padding-left: 4px;
  padding-right: 4px;
}
</style>
