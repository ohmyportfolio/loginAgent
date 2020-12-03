<template>
  <dx-drop-down-box
    ref="component"
    v-bind="$attrs"
    v-on="$listeners"
    :data-source="dataSource"
    :items="items"
    value-expr="id" display-expr="name"
    :value="dataParent.formData[dataField]" @valueChanged="onValueChanged">
    <template #content>
      <oc-tree-list v-bind="$attrs" v-on="$listeners" :resource="resource" :show-column-headers="false"
        parent-id-expr="parent_id" root-value="root" select="id,name,parent_id" :showToolbar="false" :pageSize="6"
        :selected-row-keys="selectedKeys" @selection-changed="onSelectionChanged" @row-dbl-click="onRowDblClick">
        <oc-column data-field="name" caption="이름"/>
        <dx-filter-row :visible="true" apply-filter="onClick"/>
      </oc-tree-list>
    </template>
  </dx-drop-down-box>
</template>
<script>
import DataComponent from './data-component';

export default {
  mixins: [DataComponent],
  props: {
    dataField: String
  },
  data() {
    return {
      dataParent: null,
      selectedKeys: [],
      items: [],
      dataSource: null,
    }
  },
  created() {
    this.dataSource = this.buildDataSource({resource: this.resource, select: this.select || 'id,name'});
    this.dataParent = this.getDataParent();
  },
  methods: {
    onSelectionChanged: function(e) {
      this.items = e.selectedRowsData;
      if(e.selectedRowKeys.length) {
        this.dataParent.formData[this.dataField] = e.selectedRowKeys[0];
      }
    },
    onRowDblClick: function(e) {
      this.$refs.component.instance._popup.hide();
    },
    onValueChanged: function(e) {
      this.selectedKeys = [e.value];
    },
  }
};
</script>