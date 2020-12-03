import Vue from "vue";

import "./plugins/devextream-loader";

import "../../../occam2-base/occam2-base-web/src/occam2-loader";
import codes from "../../../occam2-base/occam2-base-web/src/services/codes";
import client from "../../../occam2-base/occam2-base-web/src/services/client";
import service from "../../../occam2-base/occam2-base-web/src/services/service";
import util from "../../../occam2-base/occam2-base-web/src/services/util";
import auth from "../../../occam2-base/occam2-base-web/src/services/auth";
import validator from "../../../occam2-base/occam2-base-web/src/services/validator";

import App from "./App";
import routerLoader from "../../../occam2-base/occam2-base-web/src/router-loader";
import appInfo from "./app-info";
import i18n from "./plugins/vue-i18n";
import actions from "./actions";

import defaultLayout from "./layouts/side-nav-outer";
import simpleLayout from "./layouts/single-card";

Vue.config.productionTip = false;

service.enableCache = false;

Vue.prototype.$appInfo = appInfo;
Vue.prototype.codes = codes;
Vue.prototype.client = client;
Vue.prototype.service = service;
Vue.prototype.actions = actions;
Vue.prototype.auth = auth;
Vue.prototype.window = window;
Vue.prototype.util = util;
Vue.prototype.validator = validator;

(async function () {
  codes.load();
  routerLoader.layout.default = defaultLayout;
  routerLoader.layout.simple = simpleLayout;
  let router = await routerLoader.load();
  await auth.load();
  new Vue({
    router, i18n,
    render: h => h(App),
    data() {
      return {
        isMobile : /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent),
        windowHeight: document.body.clientHeight
      }
    },
    created() {
      window.onresize = (event) => {
        this.windowHeight = document.body.clientHeight;
      };
    },
  }).$mount("#app");
}());