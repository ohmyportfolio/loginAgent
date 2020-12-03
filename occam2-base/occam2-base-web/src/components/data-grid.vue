<template>
  <div>
    <dx-data-grid
      ref="component" width="100%"
      v-bind="$attrs"
      v-on="$listeners"
      :data-source="dataSource"
      :remote-operations="serverSide"
      :allow-column-resizing="!$root.isMobile"
      column-resizing-mode="widget"
      :allow-column-reordering="!$root.isMobile"
      :column-fixing="{enabled: true}"
      :repaint-changes-only="true"
      :show-column-lines="true"
      :show-borders="true"
      :scrolling="{ showScrollbar: 'always' }"
      :selection="{ mode: 'single' }"
      :editing="batchEditing"
      @initialized="onInitialized"
      @editorPreparing="onEditorPreparing"
      @cellPrepared="onCellPrepared"
      @toolbar-preparing="onToolbarPreparing"
      @init-new-row="onInitNewRow"
      @row-dbl-click="onRowDblClick"
      @row-click="onRowClick"
      @selection-changed="onSelectionChange"
      @cell-click="onCellClick"
      @cell-dbl-click="onCellDblClick"
      @row-validating="onRowValidating"
      @exporting="onExporting" @exported="onExported">
      <slot/>
      <dx-column v-if="hasForm && showEditButton && !$root.isMobile" :width="81" :buttons="computedRowButtons" type="buttons"/>
      <template #toolbar>
        <slot name="toolbar"/>
      </template>
      <template #importExcelFile>
        <div>
          <form ref="fileUploaderForm" method="post" action="/api/import/excel" enctype="multipart/form-data" target="_hiddenFame">
            <input type="hidden" name="resource"/>
            <input type="hidden" name="fields"/>
            <input type="hidden" value="{}" name="defaultData"/>
            <dx-file-uploader width="135" label-text="file"
              accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" upload-mode="useForm"
              @content-ready="onImportExcelUploadReady" @valueChanged="onImportExcelChanged"/>
          </form>
          <iframe name="_hiddenFame" style="display:none"/>
        </div>
      </template>
      <dx-paging :page-size="pageSize || $appInfo.pageSize || 12"/>
      <dx-pager :show-page-size-selector="true" :show-info="true" :allowed-page-sizes="allowPageSizes || [(pageSize || $appInfo.pageSize || 12), 100, 500]"/>
      <dx-row-dragging v-if="enableDragging" :allow-drop-inside-item="allowDropInsideItem" :allow-reordering="allowReordering" :show-drag-icons="showDragIcons"
        :on-drag-change="onDragChange" :on-reorder="onReorder" :on-add="onReorder" :on-remove="onRemove" :group="draggingGroup"/>
      <dx-state-storing v-if="enableStateStoring" :enabled="enableStateStoring" type="custom" :customSave="onCustomSave" :customLoad="onCustomLoad"/>
    </dx-data-grid>
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
        <component ref="form" :is="formComponent" :dataId="formDataId" :resource="resource" :openMode="formOpenMode" :params="formParams" :defaultData="defaultData" class="content" style="overflow:auto;height:100%" @close="closeForm" @saved="onFormSaved"/>
      </div>
    </dx-popup>
  </div>
</template>
<script>
import DataComponent from './data-component';
import { confirm } from 'devextreme/ui/dialog';

export default {
  mixins: [DataComponent],
  props: {
    dataSource: { default: () => { return {} } },
    title: String,
    selectionMode: { default: "multiple" },
    formName: String,
    formWidth: Number,
    formOpenMode: { default: "popup" }, // popup, route, newtab
    formParams: Object,
    hasForm: Boolean,
    closeFormOnSaved: { default: true },
    serverSide: { default: () => { return { paging: true, filtering: true, sorting: true, grouping: false, summary: false, groupPaging: false }} },
    defaultData: {},
    dataField: String,
    showRefresh: Boolean,
    showBulkUpdate: Boolean,
    showBulkDelete: Boolean,
    showBatchEdit: Boolean,
    actions: Array,
    pageSize: Number,
    loadAfterUpdate: { default: false },
    showToolbar: { default: true },
    showAddButton: {default: true},
    showEditButton: {default: true},
    enableImport: {default: false},
    showCustomToolbar: { default: true },
    toolbarHeight: Number,
    noLoad: { default: false },
    enableFilterByQuery: { default: false },
    enableDblClickOpen: { default: false },
    enableClickOpen: { default: false },
    enableDragging: { default: false },
    keyExpr: { default: "id" },
    parentIdExpr: { default: "parent_id" },
    seqExpr: { default: "seq" },
    allowDropInsideItem: { default: false },
    allowReordering: { default: false },
    allowPageSizes:Array,
    showDragIcons: { default: false },
    draggingGroup: { default: "default" },
    enableStateStoring: { default: false },    
    stateKey: String,
    rootValue: String,
    expandedRowKeys: { default: () => [] },
    rowButtons: Array
  },
  data() {
    return {
      selectedRows: [],
      toolbarItems: [],
      remoteOperations: false,
      batchEditing: null,
      editingMode: null,
      isEditing: false,
      isEditingPrev: false,
      bulkUpdateData: {},
      bulkUpdateOptions: { dataField: "" },
      inProgress: false,
      formComponent: null,
      formDataId: null,
      currentRow: {},
      currentCol: {},
      closeOnOutsideClick: false,
      visibleBulkUpdate: false,
      mergeElements: {},
      pastedResourceCache: {}
    };
  },
  computed: {
    computedRowButtons() {
      let rowButtons = ['delete', {
        hint: '편집',
        icon: 'edit',
        onClick: this.onEditClick
      }];
      if(this.rowButtons) {
        return rowButtons.concat(this.rowButtons);
      } else {
        return rowButtons;
      }
    }
  },
  watch: {
    filter: function(newVal, oldVal) {
      if(!this.util.deepCompare(newVal, oldVal)) {
        this.refresh();
      }
    },
    title: function(newVal) {
      this.toolbarItems[0].text = newVal;
    }
  },
  beforeMount() {
    if(this.select && !this.noLoad)
      this.bindDataSource();
    if(this.hasForm) {
      let formName = this.formName || this.resource.substr(0, this.resource.length - 1);
      this.formComponent = this.getComponent(formName);
    }
  },
  mounted() {
    if(!this.select && !this.noLoad)
      this.bindDataSource();
  },
  methods: {
    onInitialized(e) {
      let component = e ? e.component : this.component;
      this.component = component;
       
      component.element().onpaste = this.onPaste;
      if(!component._options.columns)
        return;
      component.option("columns").forEach((column) => {
        if(!column.dataField)
          return;
        
        this.updateColumnOptions(column, this);
        component.columnOption(column.dataField, "editorOptions", column.editorOptions);
        component.columnOption(column.dataField, "format", column.format);
        component.columnOption(column.dataField, "dataType", column.dataType);
        component.columnOption(column.dataField, "lookup", column.lookup);
        
        if(this.enableFilterByQuery && this.$route.query) {
          let filterValue = this.$route.query[column.dataField];
          if(filterValue) {
            if(filterValue.indexOf(".") >= 0) {
              let that = this;
              filterValue = eval("that." + filterValue);
            }
            setTimeout(() => {
              if(filterValue.indexOf("[") >= 0) {
                filterValue = eval(filterValue);
                component.columnOption(column.dataField, "filterValues", filterValue);
              } else {
                component.columnOption(column.dataField, "filterValue", filterValue);
              }
            });
          }
        }
        if(column.readOnly) {
          let allowEditing = !column.readOnly; 
          delete column.readOnly; // 그냥 두면 filter가 동작안함
          component.columnOption(column.dataField, "allowEditing", allowEditing);
        }
        let validationRules = column.validationRules || [];
        if(column.isRequired) {
          validationRules.push({ type: "required" });
        }
        if(validationRules.length > 0) {
          component.columnOption(column.dataField, "validationRules", validationRules);
        }
      });
    },
    onEditorPreparing(e) {
      if (e.parentType === "dataRow") {
        if(e.targetTypeField) {
          e.targetType = e.row.data[e.targetTypeField]
          e.typeId = e.row.data[e.typeIdField]
          this.updateColumnOptions(e, this);
        } else {
          // 다른 옵션은 이미 onInitialized에서 초기화 되었으므로 onlyEditorName true로 줌
          // -> SelectBox를 오픈해서 스크롤을 내리고 두번째 오픈하면 마지막 페이지 데이터만 나오는 버그 해결을 위해서 false로 변경
          this.updateColumnOptions(e, this, false);
        }
      } else if (e.parentType === "filterRow") {
        if(e.editorName == "dxSelectBox") {
          e.editorOptions.showClearButton = true;
        }
        let applyButton = e.element.querySelector(".dx-apply-button");
        if(applyButton) {
          if(e.editorName == "dxTextBox" || e.editorName == "dxTextArea") {
            e.editorOptions.onFocusOut = function(arg) {  
              e.element.querySelector(".dx-apply-button").click();
            }
            e.editorOptions.onEnterKey = function(arg) {  
              e.element.querySelector(".dx-apply-button").click();
            }
          } else {
            let onValueChanged = e.editorOptions.onValueChanged;
            e.editorOptions.onValueChanged = function(arg) {
              onValueChanged(arg);
              e.element.querySelector(".dx-apply-button").click();
            }
          }
        }
      }
    },
    onToolbarPreparing(e) {
      if(!this.showToolbar) {
        e.toolbarOptions.visible = false;
        return;
      }
      this.component = e.component;
      e.toolbarOptions.height = this.toolbarHeight;
      this.toolbarItems = e.toolbarOptions.items;

      /* allowExportSelectedData 동작안함
      const exportBtnIndex = this.toolbarItems.indexOf(this.toolbarItems.find(item => item.name === "exportButton"));
      this.toolbarItems[exportBtnIndex] = {  
        location: "after",  locateInMenu: "always", sortIndex: 30, widget: "dxButton",  
        options: {  
          text: "엑셀 내보내기", icon: "xlsxfile", hint: "엑셀로 내보내기", 
          onClick: function() {  
            e.component.exportToExcel(false);  
          }
        }
      };
      */

     const exportBtnIndex = 0;
      if(this.enableImport) {
        if(exportBtnIndex == -1)
          exportBtnIndex = 0;
        this.toolbarItems.splice(exportBtnIndex, 0, {
          location: 'after', locateInMenu: "always", showText: "always", sortIndex: 30,
          template: 'importExcelFile'
        });
      }

      if(this.title) {
        this.toolbarItems.unshift({
          location: "before", text: this.title
        });
      }

      if(this.showRefresh) {
        this.toolbarItems.push({
          location: "after", locateInMenu: "auto", showText: "inMenu", widget: "dxButton",
          options: {
            icon: "refresh", text: "새로고침", hint: "새로고침", 
            onClick: () => {
              this.component.refresh();
            }
          },
        });
      }

      if(this.showBatchEdit) {
        this.toolbarItems.unshift({
          location: "after", locateInMenu: "auto", showText: "always", widget: "dxButton",
          options: {
            icon: "edit", text: "일괄편집", hint: "일괄편집", stylingMode: this.batchEditing ? "contained" : "text",
            onClick: this.toggleBatchEditing.bind(this)
          }
        });
      }

      if(this.showBulkUpdate) {
        let columns = this.component._options.columns
          .filter((column) => column.visible != false && column.allowEditing != false && column.readOnly != true)
          .map((column) => { return { id:column.dataField, name:column.caption || column.dataField }});
        this.toolbarItems.unshift({
          location: "after", locateInMenu: "always", showText: "always", widget: "dxDropDownButton",
          options: {
            icon: "repeat", text: "일괄변경", hint: "선택된 항목 일괄변경", stylingMode: 'text',
            items: columns, displayExpr: "name", keyExpr: "id", disabled: true, 
            checkEnabled: (selections) => { return selections.length > 0 },
            onItemClick: this.openBulkUpdate.bind(this)
          }
        });
      }

      if(this.showBulkDelete) {
        this.toolbarItems.unshift({
          location: "after", locateInMenu: "always", showText: "always", widget: "dxButton",
          options: {
            icon: "remove", text: "선택삭제", hint: "선택삭제",
            disabled: true,
            checkEnabled: (selections) => { return selections.length > 0 },
            onClick: this.remove.bind(this)
          }
        });
      }

      if (this.hasForm && this.showAddButton) {
        this.toolbarItems.unshift({
          location: "after", locateInMenu: "auto", showText: "always", widget: "dxButton",
          options: {
            icon: "edit", text: "등록", hint: "등록", 
            onClick: this.goInsert.bind(this)
          }
        });
      }

      if (this.actions) {
        this.actions.forEach((action) => {
          let item = {
            location: action.location || "after", locateInMenu: action.locateInMenu || "auto", showText: action.showText || "always", 
            widget: action.widget || "dxButton"
          };
          item.options = action.options || {};
          item.options.stylingMode = item.options.stylingMode || action.stylingMode || "text";
          item.options.text = item.options.text || action.text
          item.options.icon = item.options.icon || action.icon
          item.options.disabled = item.options.disabled || action.disabled; 
          item.options.checkEnabled = item.options.checkEnabled || action.checkEnabled;
          if(action.onClick) {
            item.onClick = (e) => {
                action.onClick(this.component.getSelectedRowsData(), this);
              };
          }
          item.options.hint = item.options.hint || item.options.name;
          this.toolbarItems.unshift(item);
        });
      }

      if(this.showCustomToolbar) {
        this.toolbarItems.unshift({
          location: 'after',
          template: 'toolbar'
        });
      }

      this.toolbarItems.forEach((item) => {
        item.options = item.options || {};
        let existInitialized = item.options.onInitialized;
        item.options.onInitialized = (e) => {
          if(!e)
            return;
          item.instance = e.component;
          if(existInitialized) {
            existInitialized(e);
          }
        }
      });
      
      this.onSelectionChange({selectedRowsData: this.component.getSelectedRowsData()});
    },
    onSelectionChange(e) {
      this.selectedRows = e.selectedRowsData;
      
      this.toolbarItems.forEach(item => {
        if(item.options.checkEnabled) {
          let enabled = item.options.checkEnabled(e.selectedRowsData);
          item.options.disabled = !enabled;
          if(item.instance) {
            item.instance.option("disabled", !enabled);
          }
        }
      });
    },
    onInitNewRow(e) {
      let defaultData = typeof this.defaultData == "function" ? this.defaultData() : this.defaultData;
      e.data = Object.assign({}, defaultData, e.data);
    },
    onRowDblClick(e) {
      if((this.enableDblClickOpen) && this.hasForm) {
        this.openForm(this.getPk(e.data));
      }
    },
    onRowClick(e) {
      if((this.enableClickOpen || this.$root.isMobile) && this.hasForm) {
        this.openForm(this.getPk(e.data));
      }
    },
    onEditClick(e) {
      if(this.hasForm) {
        this.openForm(this.getPk(e.row.data));
      }
    },
    onCellDblClick(e) {
      this.currentRow = e.row;
      if(!e.row)
        return;
      let columns = this.component.getVisibleColumns();
      this.currentCol = columns.find((item) => item.dataField == e.column.dataField);
      if (e.rowType == 'data' && this.currentCol.onDblClick) {
        e.ocComponnet = this;
        this.currentCol.onDblClick(e);
      }
    },
    onCellClick(e) {
      this.currentRow = e.row;
      if(!e.row)
        return;
      let columns = this.component.getVisibleColumns();
      this.currentCol = columns.find((item) => item.dataField == e.column.dataField);
      if (e.rowType == 'data' && this.currentCol.onClick) {
        e.ocComponnet = this;
        this.currentCol.onClick(e);
      }
    },
    updateRowValue(dataField, value) {
      let columns = this.component.getVisibleColumns();
      let colIndex = columns.findIndex((item) => item.dataField == dataField);
      this.component.cellValue(this.currentRow.rowIndex, colIndex, value);
    },    
    updateAllRowValue(dataField, value) {
      this.component.beginUpdate();
      let rows = this.component.getVisibleRows();
      let columns = this.component.getVisibleColumns();
      let colIndex = columns.findIndex((item) => item.dataField == dataField);
      for (let rowIndex = 0; rowIndex < rows.length; rowIndex++) {
        this.component.cellValue(rowIndex, colIndex, value);
      }
      this.component.endUpdate();
    },
    onPaste(e) {
      if(!this.component.option("editing") || this.currentCol.editorName == "dxTextArea" || !this.currentRow)
        return;
      if(e.clipboardData.items && e.clipboardData.items.length > 0) {
        e.clipboardData.items[0].getAsString((text) => this.onPasteText(e, text));
      }
    },
    async onPasteText(e, text) {
      let newLine = text.indexOf("\r\n") >= 0 ? "\r\n" : "\n";
      let rows = text.replace(/\s+$/g, '').split(newLine);
      if(rows.length == 1)
        return;
      e.preventDefault();
      this.component.beginUpdate();
      this.component.beginCustomLoading();
      let rowLength = this.component.getVisibleRows().length;
      let dgColumns = this.component.getVisibleColumns();
      let currentColIndex = dgColumns.findIndex((item) => item.dataField == this.currentCol.dataField);
      let moreRows = this.currentRow.isNewRow ? rows.length - 1 : (this.currentRow.rowIndex + rows.length) - rowLength;
      if(moreRows > 0) {
        for (let index = 0; index < moreRows; index++) {
          this.component.addRow();
        }
      }
      this.component.endUpdate(); // addRow 완료 처리
      this.component.beginUpdate();
      for (let rowIndex = 0; rowIndex < rows.length; rowIndex++) {
        let rowText = rows[rowIndex];
        let columns = rowText.split("\t");
        let cellRow = (rowIndex + this.currentRow.rowIndex + (moreRows > 0 && !this.currentRow.isNewRow ? moreRows : 0)) % (rowLength + moreRows);
        for (let columnIndex = 0; columnIndex < columns.length; columnIndex++) {
          let cellVal = columns[columnIndex].trim();
          let cellColumn = columnIndex + currentColIndex;
          let currentCol = dgColumns[cellColumn];
          if(dgColumns.length < cellColumn + 1 || !currentCol.dataField)
            continue;
          if(currentCol.targetType == "code" && cellVal) {
            let codes = this.getCodes(currentCol.typeId);
            if(!codes.find((code) => code.code === cellVal)) {
              let code = codes.find((code) => code.name === cellVal);
              if(code)
                cellVal = code.code;
            }
            this.component.cellValue(cellRow, cellColumn, cellVal);
          } else if(currentCol.targetType == "resource" && cellVal) {
            let resource = currentCol.typeId;
            cellVal = cellVal.replace(/(\r\n|\n|\r)/gm, "");
            let data = this.pastedResourceCache[resource + "|" + cellVal];
            if(!data) {
              data = await this.service.getByName(resource, cellVal);
              if(!data) {
                data = await this.service.getById(resource, cellVal);
              }
              this.pastedResourceCache[resource + "|" + cellVal] = data || {};
            }
            if(data)
              this.component.cellValue(cellRow, cellColumn, data.id, data.name);
          } else {
            this.component.cellValue(cellRow, cellColumn, cellVal);
          }
        }
      }
      this.component.endUpdate();
      this.component.endCustomLoading();
    },
    onCellPrepared(e) {
      if (e.rowType === "data" && !e.isEditing && e.column.targetTypeField && !e.column.showEditorAlways) {
        let data = e.row.data;
        let targetType = data[e.column.targetTypeField];
        let typeId = data[e.column.typeIdField];
        if(targetType && targetType.indexOf("code") == 0) {
          e.cellElement.innerText = this.codes.label(typeId, data[e.column.dataField]);
        }
      }
      
      if(e.rowType == "data" && e.column.allowMerge) {
        this.isEditingPrev = this.isEditing;
        this.isEditing = e.component.getController("editing").isEditing();
        if(this.isEditing && e.column.allowEditing) {
          let rows = e.component.getVisibleRows();
          rows.forEach((row) => {
            if(row.rowType != "data")
              return;
            let rowEl = row.cells[e.columnIndex].cellElement;
            if(rowEl == null)
              return;
            rowEl.setAttribute('rowspan', 1);
            rowEl.style.display = '';
          });
        } else {
          e.cellElement.classList.add("mergecells");
          let prevVal = e.component.cellValue(e.rowIndex - 1, e.column.dataField);
          if(e.column.command != "edit" && prevVal == e.value && this.mergeElements[e.rowIndex - 1]) {
            var prev = this.mergeElements[e.rowIndex - 1][e.column.dataField];
            if(prev) {
              var span = prev.getAttribute('rowspan')
              if(span) {
                prev.setAttribute('rowspan', Number(span) + 1);
              } else {
                prev.setAttribute('rowspan', 2);
              }
              e.cellElement.style.display = 'none';
            }
            if(!this.mergeElements[e.rowIndex]) this.mergeElements[e.rowIndex] = {};
            this.mergeElements[e.rowIndex][e.column.dataField] = prev;
          } else {
            if(!this.mergeElements[e.rowIndex])
              this.mergeElements[e.rowIndex] = {};  
            this.mergeElements[e.rowIndex][e.column.dataField] = e.cellElement;
          }
        }
      }
    },
    onRowValidating(e) {
      this.component.isValid = e.isValid;
    },
    goInsert() {
      this.openForm("new");
    },
    openForm(dataId) {
      if(!dataId)
        return;
      this.formDataId = dataId;
      if(this.formOpenMode == "route") {
        this.$router.push(this.resource + "/" + this.formDataId);
      } else if(this.formOpenMode == "popup") {
        this.$refs.formPopup.instance.show();
      } else if(this.formOpenMode == "newtab") {
        this.util.openWindow(this.resource, this.formDataId);
      }
    },
    closeForm(success) {
      this.$refs.formPopup.instance.hide();
      setTimeout(() => {
        this.formDataId = null;
      });
    },
    onFormSaved(e) {
      this.component.refresh();
      if(this.closeFormOnSaved && !e.isNew) {
        this.closeForm(true);
      } else {
        this.formDataId = e.dataId;
      }
    },
    bindDataSource() {
      if(!this.resource)
        return;
      let dataSource = this.buildDataSource();
      Object.assign(this.dataSource, dataSource);
      if(!this.serverSide) {
        let promise = dataSource._store.load();
        if(promise)
          promise.then(function(response) {
            this.component.option("dataSource", response.data);
            if(this.dataField) {
              this.getDataParent(this.$parent).formData[this.dataField] = response.data;
            }
          }.bind(this));
      } else {
        this.component.option("dataSource", dataSource);
      }
    },
    refresh() {
      this.component.clearSelection();
      this.bindDataSource(); // 안하면 filter 변경이 반영안됨
    },
    async remove() {
      let keys = this.component.getSelectedRowKeys();
      let confirmed = await confirm(keys.length + "개의 항목을 정말 삭제하시겠습니까?", "확인");
      if(confirmed) {
        await this.dataSource._store.remove(keys);
        this.component.refresh();
      }
    },
    toggleBatchEditing(e) {
      if(!this.batchEditing) {
        this.batchEditing = this.editingMode || { mode: 'batch', allowUpdating: true, allowAdding: true, allowDeleting: true };
      } else {
        this.batchEditing = null;
      }
    },
    openBulkUpdate(e) {
      this.bulkUpdateData = {};
      this.bulkUpdateOptions = this.component._options.columns.find((item) => item.dataField == e.itemData.id);
      this.visibleBulkUpdate = true;
      this.$nextTick(() => {
        this.$refs.bulkUpdatePopup.instance.show();
      });
    },
    async bulkUpdateSave(e) {
      this.inProgress = true;
      try {
        for(let key of this.component.getSelectedRowKeys())
          await this.dataSource._store.update(key, this.bulkUpdateData);
      } finally {
        this.inProgress = false;
      }
      this.$refs.bulkUpdatePopup.instance.hide();
      this.component.refresh();
    },
    onImportExcelUploadReady(e) {
      let button = e.component._selectButton
      button.option("text", "엑셀 불러오기");
      button.option("hint", "엑셀 불러오기");
      button.option("icon", "xlsfile");
      button.option("stylingMode", "text");
    },
    onImportExcelChanged(e) {
      if(e.value.length == 0)
        return;
      this.component._options.columns.forEach(data => { data.editorOptions = undefined; });
      this.$refs.fileUploaderForm.elements.resource.value = this.resource;
      this.$refs.fileUploaderForm.elements.fields.value = JSON.stringify(this.component._options.columns);
      if(this.defaultData != undefined) {
        this.$refs.fileUploaderForm.elements.defaultData.value = JSON.stringify(this.defaultData);
      }
      this.$refs.fileUploaderForm.submit();
      e.component.option("value", [])
    },
    onExporting(e) {
      let isCallBeginUpdate = false;
      let primaryKeys = ["id"];
      primaryKeys = primaryKeys.concat(this.primaryKey);

      primaryKeys.forEach(key => {
        let field = e.component._options.columns.find(data => data.dataField == key);
        if(field) {
          let visible = field.visible == undefined ? true : field.visible;
          if(field !== undefined && visible == false) {
            if(isCallBeginUpdate == false) {
              e.component.beginUpdate();
              isCallBeginUpdate = true;
            }
            e.component.columnOption(key, "visible", true);
          }
        }
      })
    },
    onExported(e) {
      let isSetColumnOption = false;
      let primaryKeys = ["id"];
      primaryKeys = primaryKeys.concat(this.primaryKey);

      primaryKeys.forEach(key => {
        let field = e.component._options.columns.find(data => data.dataField == key);
        if(field) {
          let visible = field.visible == undefined ? true : field.visible;
          if(field !== undefined) {
            e.component.columnOption(key, "visible", visible);
            isSetColumnOption = true;
          }
        }
      })
      if(isSetColumnOption == true)
        e.component.endUpdate();
    },
    async onReorder(e) {
      let visibleRows = e.component.getVisibleRows();
      let sourceData = e.itemData;
      let row = visibleRows[e.toIndex];
      let targetData = row.data;
      let targetNode = row.node;
      if(!sourceData[this.keyExpr])
        return;
      let set = {};
      set._keyExpr = this.keyExpr;
      set._parentIdExpr = this.parentIdExpr;
      set._seqExpr = this.seqExpr;
      set._update_seq = true;
      if (e.dropInsideItem) {
        set[this.parentIdExpr] = targetData[this.keyExpr];
        if(set[this.parentIdExpr] == null)
          throw "parent_id is null";        
        set[this.seqExpr] = 1;
        await this.service.update(this.resource, sourceData[this.keyExpr], set);
        this.expandedRowKeys.push(targetData[this.keyExpr]);
        e.fromComponent.refresh();
      } else {        
        set[this.parentIdExpr] = targetData[this.parentIdExpr];
        if(set[this.parentIdExpr] == null)
          throw "parent_id is null";
        if(targetData[this.parentIdExpr] == sourceData[this.parentIdExpr]) {
          set[this.seqExpr] = targetData[this.seqExpr];
          if(set[this.seqExpr] < 1)
            set[this.seqExpr] = 1;
        } else {
          set[this.seqExpr] = 1;
        }
        await this.service.update(this.resource, sourceData[this.keyExpr], set);
        e.fromComponent.refresh();
      }
    },
    onDragChange(e) {
      if(e.fromComponent.NAME == "dxTreeList" && e.toComponent.NAME == "dxDataGrid") {
          e.cancel = true;
          return;
      }
      if(!e.dropInsideItem && e.fromComponent.NAME == "dxDataGrid" && e.toComponent.NAME == "dxTreeList") {
          e.cancel = true;
          return;
      }
      if(!this.allowReordering && e.fromComponent.NAME == "dxDataGrid" && e.toComponent.NAME == "dxDataGrid") {
          e.cancel = true;
          return;
      }      
      if(e.fromComponent.NAME == "dxDataGrid")
        return;
      let visibleRows = e.toComponent.getVisibleRows();
      let sourceNode = e.fromComponent.getNodeByKey(e.itemData[this.keyExpr]);
      let row = visibleRows[e.toIndex];
      if(row == null) {
        e.cancel = true;
        return;
      }
      let targetData = row.data;
      let targetNode = row.node;

      while (sourceNode && targetNode && targetNode.data) {
        if (targetNode.data[this.keyExpr] === sourceNode.data[this.keyExpr]) {
          e.cancel = true;
          break;
        }
        targetNode = targetNode.parent;
      }
    },
    onRemove(e) {
      if(e.fromComponent != e.toComponent)
        this.component.refresh();
    },
    getStateKey() {
      return this.stateKey || this.$route.fullPath.substr(1);
    },
    onCustomSave(state) {
      if(Object.keys(state).length == 0 || state.pageSize == 0)
        return;
      let value = JSON.stringify(state);
      if(this.loadedState != value) {
        this.service.update("gridStates", this.getStateKey(), {state: value});
      }
    },
    async onCustomLoad() {
      let result = await this.service.get("gridStates", this.getStateKey());
      this.loadedState = result.state;
      return JSON.parse(result.state);
    },
    updateComponent() {
        this.$nextTick(() => {
          if(this.$refs.component && this.$refs.component.update) {
            this.$refs.component.update();
          }
        });
    },
  }
};
</script>