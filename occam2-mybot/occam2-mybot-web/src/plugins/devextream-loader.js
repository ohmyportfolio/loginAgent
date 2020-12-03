import Vue from "vue";
import 'devextreme/dist/css/dx.common.css';
import '../themes/generated/theme.base.css';
import '../themes/generated/theme.additional.css';
import "./devextream-ko";

import 'splitpanes/dist/splitpanes.css'

let components = [
  require("devextreme-vue"),
  require("devextreme-vue/toolbar"),
  require("devextreme-vue/ui/button"),
  require("devextreme-vue/form"),
  require("devextreme-vue/data-grid"),
  require("devextreme-vue/tree-list"),
  require("devextreme-vue/ui/popup"),
  require("devextreme-vue/ui/select-box"),  
  require("devextreme-vue/check-box"),
  require("devextreme-vue/text-box"),
  require("devextreme-vue/validator"),
  require("devextreme-vue/validation-group"),
  require("devextreme-vue/autocomplete"),
  require("devextreme-vue/tag-box"),
  require("devextreme-vue/scroll-view"),
  require("devextreme-vue/load-panel"),
  require("devextreme-vue/tab-panel"),
  require("devextreme-vue/file-uploader"),
  require("devextreme-vue/tooltip"),
  require("devextreme-vue/pivot-grid"),
  
  require("splitpanes")
];

let loadedComponents = [];

components.map(function (modules) {
  let moduleList = Object.keys(modules)
  // console.log(modules);
  moduleList.map(function (name) {
    if(name == "default")
      return;
    if(loadedComponents.indexOf(name) > -1)
      return;
    loadedComponents.push(name);
    let component = modules[name];
    Vue.component(name, component);
  })
});
