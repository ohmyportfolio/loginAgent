<template>
  <div>
    <dx-tree-list
      ref="component" width="100%"
      v-bind="$attrs"
      v-on="$listeners"
      :data-source="dataSource"
      :remote-operations="{ filtering: serverSide, sorting: serverSide, grouping: serverSide }"
      :allow-column-resizing="!$root.isMobile"
      :allow-column-reordering="!$root.isMobile"
      column-resizing-mode="widget"
      :column-fixing="{enabled: true}"
      :show-column-lines="true"
      :show-row-lines="true"
      :show-borders="true"
      :scrolling="{ showScrollbar: 'always' }"
      :selection="{ mode: 'single' }"
      :editing="batchEditing"
      :root-value="rootValue"
      :key-expr="keyExpr"
      :parent-id-expr="parentIdExpr"
      :expandedRowKeys="expandedRowKeys"
      @editorPreparing="onEditorPreparing"
      @cellPrepared="onCellPrepared"
      @initialized="onInitialized"
      @toolbar-preparing="onToolbarPreparing"
      @init-new-row="onInitNewRow"
      @row-dbl-click="onRowDblClick"
      @selection-changed="onSelectionChange"
      @cell-click="onCellClick"
      @row-validating="onRowValidating"
      @exporting="onExporting" @exported="onExported">
      <slot />
      <dx-column v-if="hasForm && showEditButton" :width="113" :buttons="treeEditButtons" type="buttons"/>
      <template #toolbar>
        <slot name="toolbar"/>
      </template>
      <dx-paging :enabled="true" :page-size="pageSize"/>
      <dx-pager :show-page-size-selector="true" :show-info="true" :allowed-page-sizes="allowPageSizes || [6, 12, 24, 100, 500]"/>
      <dx-scrolling mode="standard"/>
      <dx-row-dragging v-if="enableDragging" :allow-drop-inside-item="allowDropInsideItem" :allow-reordering="allowReordering" :show-drag-icons="showDragIcons"
        :on-drag-change="onDragChange" :on-reorder="onReorder" :on-add="onReorder" :on-remove="onRemove" :group="draggingGroup"/>
      <dx-state-storing v-if="enableStateStoring" :enabled="enableStateStoring" type="custom" :customSave="onCustomSave" :customLoad="onCustomLoad"/>
    </dx-tree-list>
    <dx-popup ref="bulkUpdatePopup" v-if="visibleBulkUpdate && showBulkUpdate" :show-title="true" 
      :width="300" :height="230" class="popup" title="일괄변경" :animation="{}" @hidden="visibleBulkUpdate = false">
      <div>
        <oc-form ref="bulkUpdateForm" :formData="bulkUpdateData" :showToolbar="false" class="oc-normal-form">
          <oc-item v-bind="bulkUpdateOptions"/>
          <oc-item>
            <template #default>
              <dx-button text="일괄변경" type="success" @click="bulkUpdateSave" :disabled="inProgress"/>
            </template>
          </oc-item>
        </oc-form>
      </div>
    </dx-popup>
    <dx-popup ref="formPopup" v-if="hasForm && formOpenMode == 'popup'" :visible="false" :max-width="formWidth || $appInfo.formWidth || 800" width="100%" height="100%" class="popup" 
      :show-title="false" position="right" :closeOnOutsideClick="closeOnOutsideClick" :animation="{}" :shading="false">
      <div style="height:100%">
        <component ref="form" v-if="formDataId" :key="formDataId" :is="formComponent" :dataId="formDataId" :resource="resource" :openMode="formOpenMode" :params="formParams" :defaultData="defaultData" class="content" style="overflow:auto;height:100%" @close="closeForm" @saved="onFormSaved"/>
      </div>
    </dx-popup>
  </div>
</template>
<script>
import DataGrid from "./data-grid.vue";

export default {
  extends: DataGrid,
  props: {
  },
  data() {
    return {
      treeEditButtons:  ['add', 'delete', {
        hint: '편집',
        icon: 'edit',
        onClick: this.onEditClick
      }]
    }
  },
  methods: {
  }
};
</script>