import Base from './base';

export default {
  extends: Base,
  props: {
    resource: String,
    select: String,
    selectMore: String,
    orderby: String,
    filter: Array,
    sort: Array,
    primaryKey: {},
    params: { default: () => { return {} } },
    pageSize: Number,
    dataId: null
  },
  methods: {
    getCodes(codeType) {
      return this.codes.list[codeType];
    },
    updateColumnOptions(column, component, onlyEditorName) {
      column.editorOptions = column.editorOptions || {};

      if(column.targetTypeField && this.formData) {
        column.targetType = this.formData[column.targetTypeField];
        column.typeId = this.formData[column.typeIdField];
      }

      if (column.targetType == "code") {
        column.editorType = column.editorName = "dxSelectBox";
        if (!onlyEditorName) {
          this.updateCodeOptions(column);
        }
      } else if (column.targetType == "codeRadio") {
        column.editorType = column.editorName = "dxRadioGroup";
        if (!onlyEditorName) {
          this.updateCodeOptions(column);
        }
      } else if (column.targetType == "codeTag") {
        column.editorType = column.editorName = "dxTagBox";
        if (!onlyEditorName) {
          this.updateCodeOptions(column);
          column.editorOptions.showSelectionControls = true;
        }
      } else if (column.targetType == "resource") {
        column.editorType = column.editorName = "dxSelectBox";
        if (!onlyEditorName) {
          this.updateResourceOptions(column);
        }
      } else if (column.targetType == "resourceTag") {
        column.editorType = column.editorName = "dxTagBox";
        if (!onlyEditorName) {
          this.updateResourceOptions(column);
        }
      } else if (column.targetType == "integer") {
        column.editorType = column.editorName = "dxNumberBox";
        if (!onlyEditorName) {
          column.format = column.format || {};
          column.format.type = "fixedPoint";
          column.dataType = "number";
          column.editorOptions.format =  column.editorOptions.format || "#,##0";
          if(column.editorOptions.showSpinButtons === undefined)
            column.editorOptions.showSpinButtons = true;
        }
      } else if (column.targetType == "float") {
        column.editorType = column.editorName = "dxNumberBox";
        if (!onlyEditorName) {
          column.format = column.format || {};
          column.format.type = "fixedPoint";
          column.format.precision = 2;
          column.dataType = "number";
          column.editorOptions.format = "#,##0.00";
        }
      } else if (column.targetType == "fileSize") {
        column.editorType = column.editorName = "dxNumberBox";
        if (!onlyEditorName) {
          column.format = column.format || {};
          column.format.formatter = this.util.formatFileSize;
          column.dataType = "number";
          column.editorOptions.format = "#,##0 bytes";
        }
      } else if (column.targetType == "percent") {
        column.editorType = column.editorName = "dxNumberBox";
        if (!onlyEditorName) {
          column.format = column.format || {};
          column.format.type = "percent";
          column.dataType = "number";
          column.editorOptions.format = "percent";
        }
      } else if (column.targetType == "date") {
        column.editorType = column.editorName = "dxDateBox";
        if (!onlyEditorName) {
          column.dataType = "date";
        }
      } else if (column.targetType == "dateMonth") {
        column.editorType = column.editorName = "dxDateBox";
        if (!onlyEditorName) {
          column.dataType = "date";
          column.editorOptions.displayFormat = "yyyy, MM";
          column.editorOptions.maxZoomLevel = "year";
        }
      }else if (column.targetType == "datetime") {
        column.editorType = column.editorName = "dxDateBox";
        if (!onlyEditorName) {
          column.dataType = "datetime";
          column.editorOptions.type = "datetime";
        }
      } else if (column.targetType == "time") {
        column.editorType = column.editorName = "dxDateBox";
        if (!onlyEditorName) {
          column.format = column.format || {};
          column.format.type = "shortTime";
          column.editorOptions.type = "time";
          column.editorOptions.displayFormat = "shortTime";
        }
      } else if (column.targetType == "list") {
        column.editorType = column.editorName = "dxSelectBox";
        if (!onlyEditorName) {
          column.editorOptions.dataSource = column.typeId.split(",").map((val) => val.trim());
          column.editorOptions.displayExpr = null;
          column.editorOptions.valueExpr = null;
        }
      } else if (column.targetType == "listRadio") {
        column.editorType = column.editorName = "dxRadioGroup";
        if (!onlyEditorName) {
          column.editorOptions.layout = "horizontal"
          column.editorOptions.dataSource = column.typeId.split(",").map((val) => val.trim());
          column.editorOptions.displayExpr = null;
          column.editorOptions.valueExpr = null;
        }
      } else if (column.targetType == "textArea") {
        column.editorType = column.editorName = "dxTextArea";
        if (!onlyEditorName) {
          column.editorOptions.type = "textArea";
          if(!column.editorOptions.height)
            column.editorOptions.autoResizeEnabled = true;
        }
      } else if (column.targetType == "checkBox") {
        column.editorType = column.editorName = "dxCheckBox";
        if (!onlyEditorName) {
          column.editorOptions.readOnly = column.readOnly;
          
        }
      }
      
      if(!column.dataType)
        column.dataType = "string";
      if(column.itemField && this.formData) {
        column.editorOptions.onSelectionChanged = (e) => { this.formData[column.itemField] = e.selectedItem };
      }
      if(column.onValueChanged && column.parentType == "dataRow") {
        column.editorOptions.onValueChanged = (e) => {
          if(!e.event)
            return;
          e.container = this;
          e.column = column;
          if(this.updateRowValue) {
            e.data = this.currentRow.data;
            e.data[column.dataField] = e.value;
            this.updateRowValue(column.dataField, e.value);
          } else {
            e.data = this.formData;
            e.data[column.dataField] = e.value;
          }
          column.onValueChanged(e);
        };
      }
      if(column.updateOnChange && column.parentType == "dataRow") {
        column.editorOptions.onValueChanged = (e) => {
          // TODO component 파라미터 제거하고 this로 해야하지 않을까?
          component.updateComponent();
        };
      }
      if(column.popupWidth) {
        column.editorOptions.onOpened = column.editorOptions.onContentReady = (e) => {
          e.component.element().parentElement.style.width = column.popupWidth + "px";
        }
      }
    },
    updateCodeOptions(column) {
      if (!column.isRequired && column.editorType != "dxTagBox")
        column.editorOptions.showClearButton = true;
      // editing batch, cell 모드에서 클릭하면 바로 열리게
      column.editorOptions.onFocusIn = (e) => {
        let editing = e.component.option("editing");
        let ds = e.component.option("dataSource");
        let isFilter = ds && ds.postProcess; // filter cell 구분을 위해서
        if (!isFilter && editing && (editing.mode == "batch" || editing.mode == "cell")) {
          e.component.open();
        }
      };
      column.editorOptions.layout = "horizontal";
      column.editorOptions.dataSource = this.getCodes(column.typeId);
      let displayExpr = column.displayExpr || column.editorOptions.displayExpr || 'name';
      let valueExpr = column.valueExpr || column.editorOptions.valueExpr || 'code';
      column.editorOptions.displayExpr = displayExpr;
      column.editorOptions.valueExpr = valueExpr;
      column.editorOptions.searchEnabled = column.searchEnabled === undefined ? true : column.searchEnabled;
      column.lookup = {};
      if(!column.cellTemplate && !column.calculateDisplayValue) {
        column.cellTemplate = (cellElement, cellInfo) => {
          let value = cellInfo.data[cellInfo.column.dataField];
          let codeMap = this.codes.map[cellInfo.column.typeId] || {};
          let code = codeMap[cellInfo.data[cellInfo.column.dataField]];
          cellElement.textContent = code == null ? (value == null ? "" : value) : code.name;
        }
      }
    },
    updateResourceOptions(column) {
      if (!column.isRequired && column.editorType != "dxTagBox")
        column.editorOptions.showClearButton = true;
      column.editorOptions.searchEnabled = true;
      column.editorOptions.acceptCustomValue = true;
      let displayExpr = column.displayExpr || column.editorOptions.displayExpr || 'name';
      let valueExpr = column.valueExpr || column.editorOptions.valueExpr || 'id';
      column.editorOptions.displayExpr = displayExpr;
      column.editorOptions.valueExpr = valueExpr;
      let dataSource = this.buildDataSource({ resource: column.typeId, filter: column.filter, orderby: column.orderby, pageSize: column.pageSize, 
        primaryKey: column.primaryKey, select: column.select || valueExpr+","+displayExpr});
      if(column.serverSide === false) {
        dataSource._store.load().then((response) => {
          column.editorOptions.dataSource = response.data;
        })
      } else {
        column.editorOptions.dataSource = dataSource;
      }
      column.lookup = {};
      if(typeof column.calculateDisplayValue != "function") {
        column.displayField = column.calculateDisplayValue;
        column.calculateDisplayValue = (data) => {
          return data[column.displayField] || data[column.dataField]
        }
      }
    },
    getChildrenFields(parent) {
      let childSelect = "";
      parent.$children.forEach(child => {
        childSelect += this.getChildFields(parent, child);
      });
      if (parent.$slots.default && parent.$vnode.tag.endsWith("oc-form")) {
        parent.$slots.default.forEach(child => {
          childSelect += this.getChildFields(parent, child);
        });
      }
      return childSelect;
    },
    getChildFields(parent, child) {
      if (child.componentOptions && child.componentOptions.propsData.dataField && !child.componentOptions.propsData.exceptSelect) {
        return child.componentOptions.propsData.dataField + ",";
      } else {
        if (!child.$props || child.$props.resource || (child.$props.dataSource && parent.dataSource != child.$props.dataSource))
          return "";
        let childSelect = "";
        if (child.$props.dataField && !child.$props.exceptSelect) {
          childSelect += child.$props.dataField + ",";
        }
        if (typeof child.$props.calculateDisplayValue == "string") {
          childSelect += child.$props.calculateDisplayValue + ",";
        }
        if (typeof child.$props.targetTypeField == "string") {
          childSelect += child.$props.targetTypeField + ",";
        }
        if (typeof child.$props.typeIdField == "string") {
          childSelect += child.$props.typeIdField + ",";
        }
        child.$children.forEach(child2 => {
          childSelect += this.getChildFields(child, child2);
        });
        return childSelect;
      }
    },
    getDataParent(parent = this.$parent) {
      if (parent.$parent == null || parent == null)
        return null;
      return parent.formData ? parent : this.getDataParent(parent.$parent);
    },
    getPk(data) {
      if(this.primaryKey) {
        let pks = this.primaryKey.split(",").map((str) => str.trim());
        let pkValues = [];
        for(let pk of pks) {
          pkValues.push(data[pk]);
        }
        return pkValues.join(",");
      } else {
        return data.id;
      }
    },
    isNew() {
      return !this.dataId || this.dataId == "new";
    }
  }
}