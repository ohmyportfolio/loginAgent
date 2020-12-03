<script>
import Vue from "vue";
import { DxColumn } from 'devextreme-vue/data-grid';

let component = Vue.extend({
  extends: DxColumn,
  props: {
    targetType: String,
    typeId: String,
    targetTypeField: String,
    typeIdField: String,
    serverSide: { default: true },
    primaryKey: String,
    filter: {}, // Array or Function
    select: String,
    orderby: String,
    pageSize: Number,
    valueExpr: {},
    displayExpr: {},
    isRequired: Boolean,
    readOnly: Boolean,
    allowMerge: Boolean,
    searchEnabled: Boolean,
    popupWidth: Number,
    onClick: Function,
    onDblClick: Function,
    onValueChanged: Function
  }
});
component.$_optionName = "columns";
component.$_isCollectionItem = true;

export default component;
</script>