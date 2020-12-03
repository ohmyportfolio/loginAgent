<template>
    <div class="oc-form-widget">
      <dx-load-panel :visible="loading" :position="{ of: '#scrollview' }"/>
      <dx-toolbar v-if="showToolbar" :height="toolbarHeight" class="oc-form-toolbar">
        <dx-item #default location="before" locate-in-menu="never" v-show="title">
          <div class="dx-toolbar-label">{{ title }}</div>
        </dx-item>
        <dx-item #default location="after" locate-in-menu="never" v-show="showCustomToolbar">
          <slot name="toolbar"/>
        </dx-item>
        <dx-item v-for="action in actions" :options="Object.assign({}, action, action.options)" :key="action.key" 
          :showText="action.locateInMenu || 'always'" :location="action.location || 'after'" 
          :locate-in-menu="action.locateInMenu || 'auto'" :widget="action.widget || 'dxButton'"/>
        <dx-item v-if="!isNew() && showButtons && showUrlButton" :options="{icon: 'link', text: '주소복사', onClick: copyUrl, disabled: loading}" showText="always" location="after" locate-in-menu="auto" widget="dxButton"/>
        <dx-item v-if="showButtons && showSaveButton" :options="{icon: 'todo', text: saveText, onClick: save, disabled: loading}" showText="always" location="after" locate-in-menu="never" widget="dxButton"/>
        <dx-item v-if="showButtons && showCloseButton" :options="{icon: 'close', text: '닫기', onClick: close, disabled: loading}" showText="always" location="after" locate-in-menu="never" widget="dxButton"/>
      </dx-toolbar>
      <dx-scroll-view id="scrollview" ref="scrollview">
        <form>
          <dx-form ref="component" :key="loaded" :form-data="formData" class="oc-form"
            v-bind="$attrs" v-on="$listeners" @initialized="onInitialized" @fieldDataChanged="onFieldDataChanged"
              :show-validation-summary="false" :col-count="colCount">
            <slot/>
          </dx-form>
         </form>
      </dx-scroll-view>
    </div>
</template>
<script>
import DataComponent from './data-component';
import notify from 'devextreme/ui/notify';

export default {
  mixins: [DataComponent],
  props: {
    formData: { default: () => { return {} } },
    defaultData: { default: () => { return {} } },
    colCount: Number,
    showToolbar: { default: true },
    showCustomToolbar: { default: true },
    showButtons: { default: true },
    showSaveButton: { default: true },
    showCloseButton: { default: true },
    showUrlButton: { default: false },
    updateAfterLoad: { default: false },
    toolbarHeight: Number,
    title: String,
    actions: Array,
    subForms: { type: Function, default: () => [] },
    saveCommand: String,
    saveMessage: String,
    closeOnEditSaved: { default: true },
    closeOnNewSaved: { default: false },
    saveText: { default: '저장' },
  },
  data() {
    return {
      initedId: null,
      dataSource: null,
      loading: false,
      loaded: false,
      savedData: null,
    };
  },
  computed: {
    formDataId() {
      return this.dataId || this.$parent.dataId;
    }
  },
  watch: {
    formDataId(newVal, oldVal) {
      if(newVal)
        this.init();
    }
  },
  created() {
    if(this.dataId || this.select)
      this.init()
  },
  mounted() {
    if(!this.dataId && !this.select)
      this.init()
  },
  methods: {
    init() {
      if(!this.dataSource && this.resource)
        this.dataSource = this.buildDataSource();
      if(!this.isNew() && (!this.formDataId || this.initedId == this.formDataId))
        return;
      this.loading = true;
      this.$parent.loaded = false;
      this.loaded = false;
      this.initedId = this.formDataId;
      this.$emit("dataLoading", { dataId: this.formDataId });
      if (!this.isNew()) {
        if(!this.dataSource) {
            this.dataLoaded();
        } else {
          this.dataSource._store.byKey(this.formDataId).then((data) => {
            this.util.assignObject(this.formData, data);
            this.dataLoaded();
          });
        }
      } else {
        // formData가 prop으로 들어오면 defaultData는 무시
        for(let key of Object.keys(this.defaultData)) {
          if(this.formData[key] == null)
            this.formData[key] = this.defaultData[key];
        }
        this.dataLoaded();
      }
    },
    dataLoaded() {
      this.$parent.loaded = true;
      this.loaded = true;
      this.loading = false;
      this.savedData = Object.assign({}, this.formData);
      this.$emit("dataLoaded", { data: this.formData });
      // dx-form :key="loaded" 로 처리해서 아래는 필요없을듯..
      /*
      if(this.updateAfterLoad) {
        this.updateComponent();
      } else {
        this.updateItems(false, true);
      }
      */
    },
    updateComponent() {
        this.$nextTick(() => {
          if(this.$refs.component && this.$refs.component.update) {
            this.$refs.component.update();
          }
          // this.updateItems(false, false);
        });
    },
    onInitialized(e) {
      this.component = e.component;
      this.updateItems();
    },
    onFieldDataChanged(e) {
      if(this.formData) {
        this.$set(this.formData, e.dataField, e.value);
      }
    },
    updateItems(lazy=true, onlyDynamic) {
      this.component.beginUpdate();
      if(this.component._options.items) {
        let items = this.component._options.items;
        if(onlyDynamic) {
          items = items.filter((item) => item.targetTypeField != null);
        }
        items.forEach((item) => this.updateItem(item, lazy));
      }
      this.component.endUpdate();
    },
    updateItem(item, lazy) {
      let component = this.component;
      if(item.items) {
        item.items.forEach(this.updateItem.bind(this));
      }
      if(!item.dataField)
        return;
      
      if(item.caption) {
        item.label = { text: item.caption };
        if(!lazy)
          component.itemOption(item.dataField, "label", { text: item.caption });
      }
      
      let validationRules = item.validationRules || [];
      item.editorOptions = item.editorOptions || {};
      if(item.readOnly) {
        item.editorOptions.readOnly = true;
      }
      
      this.updateColumnOptions(item, this);

      if(item.editorType) {
        if(!lazy)
          component.itemOption(item.dataField, "editorType", item.editorType);
      }
      if(Object.keys(item.editorOptions).length > 0) {
        if(!lazy)
          component.itemOption(item.dataField, "editorOptions", item.editorOptions);
      }
      if(validationRules.length > 0) {
        item.validationRules = validationRules;
      }
    },
    getItem(dataField) {
      if(!this.component)
        return null;
      return this.component._options.items.find((item) => item.dataField == dataField);
    },
    async saveChildrenEditData(parent) {
      let failed = false;
      if(!parent.$children)
        return true;
      for (let i = 0; i < parent.$children.length; i++) {
        const child = parent.$children[i];
        if(child.component && child.component.saveEditData) {
          if(child.component.hasEditData()) {
            await child.component.saveEditData();
            failed = failed || !child.component.isValid;
          }
        } else {
          failed = failed || !(await this.saveChildrenEditData(child));
        }
      }
      return !failed;
    },
    async saveDataChildren(parent) {
      let result = [];
      if(!parent || !parent.$children)
        return [];
      for(let child of parent.$children) {
        if(child.$vnode && child.$vnode.componentOptions.tag == "dx-popup")
          continue;
        if(child.saveData) {
          result.push(await child.saveData());
        }
        result = result.concat(await this.saveDataChildren(child));
      }
      return result;
    },
    isValidChildren(parent) {
      let failed = false;
      if(!parent || !parent.$children)
        return true;
      for(let child of parent.$children) {
        if(child.isValid) {
          failed = failed || !child.isValid();
        } else {
          failed = failed || !this.isValidChildren(child);
        }
      }
      return !failed;
    },
    isValid() {
      let validate = this.component.validate();
      return validate.isValid && this.isValidChildren(this);
    },
    async save() {
      let subFormValid = true;
      for(let form of this.subForms()) {
        subFormValid = await form.save();
        if(!subFormValid)
          return;
      }
      this.loading = true;
      let isValidChildrenEdit = await this.saveChildrenEditData(this);
      if(!this.component.validate().isValid || !isValidChildrenEdit) {
        notify("입력 항목을 확인해주세요.", 'error');
        this.loading = false;
        return false;
      }
      try {
        let isNew = this.isNew();
        //let dataIds = await this.saveDataChildren(this);
        let dataId = await this.saveData();// || dataIds[0];
        if(dataId) {
          notify(this.saveMessage || "저장되었습니다.", 'info');
        }
        this.$emit("saved", { dataId, isNew, savedData: this.savedData });
        if(this.$parent.$el != this.$el)
          this.$parent.$emit("saved", { dataId, isNew, savedData: this.savedData, formData: this.formData });
        // TODO: 이하가 동작하면 data-grid의 closeFormOnSaved 설정에 따라 동작하지 않게 됨
        if((!this.isNew() && this.closeOnEditSaved) || (this.isNew() && this.closeOnNewSaved))
          this.close(dataId);
      } finally {
        this.loading = false;
      }
      return true;
    },
    async saveData() {
      this.$emit("saveData", { formData: this.formData, form: this });
      if(this.resource && this.saveCommand) {
        let result = await this.service.execute(this.resource, this.isNew() ? "any" : this.formDataId, this.saveCommand, null, this.formData);
        return result;
      }
      if(!this.dataSource)
        return null;
      if (this.isNew()) {
        let newData = await this.dataSource._store.insert(this.formData);
        this.savedData = this.formData;
        return this.getPk(newData);
      } else {
        let changedData = this.util.getChangedData(this.savedData, this.formData);
        if(Object.keys(changedData).length == 0) {
          // notify("변경된 데이터가 없습니다.", 'warning');
          return;
        } else {
          await this.dataSource._store.update(this.formDataId, changedData);
          this.savedData = changedData;
          return this.formDataId;
        }        
      }
    },
    isNew() {
        return !this.formDataId || this.formDataId == "new";
    },
    close(dataId) {
      if(this.$parent.close)
        this.$parent.close();
      else
        this.$emit("close", {dataId});
    },
    copyUrl() {
      this.util.copyToClipboard(window.location.origin + "/#/" + this.resource + "/" + this.formDataId);
      notify("클립보드에 주소가 복사되었습니다.", 'info');
    }
  }
};
</script>
<style scoped>
  .buttons {
    text-align: right;
  }
  .buttons > * {
    margin-left: 10px;
  }
</style>