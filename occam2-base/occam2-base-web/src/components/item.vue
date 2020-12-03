<script>
import Vue from "vue";
import {  DxSimpleItem } from 'devextreme-vue/form';

let component = Vue.extend({
  extends: DxSimpleItem,
  props: {
    targetType: String,
    typeId: String,
    targetTypeField: String,
    typeIdField: String,
    serverSide: { default: true },
    primaryKey: String,
    filter: Array,
    select: String,
    orderby: String,
    pageSize: Number,
    caption: String,
    valueExpr: String,
    displayExpr: String,
    readOnly: Boolean,
    exceptSelect: Boolean,
    itemField: String,
    popupWidth: Number,
    onValueChanged: Function,
    updateOnChange: Boolean
  },
  watch: {
    readOnly() {
      this.$vnode.componentOptions.$_config.updateValue("editorOptions", {readOnly: this.readOnly});
    }
  }
});

component.$_optionName = "items";
component.$_isCollectionItem = true;

export default component;
</script>