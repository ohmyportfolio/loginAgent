import Vue from "vue";
import Router from "vue-router";

import service from "./services/service";
import auth from "./services/auth";

import Page from "./components/page";

import defaultLayout from "./layouts/side-nav-outer-toolbar";
import simpleLayout from "./layouts/single-card";

Vue.use(Router);

export default {
  layout: {
    default: defaultLayout,
    simple: simpleLayout
  },
  async load() {
    let routes = await service.get("meta/routes");
    for(let route of routes) {
      if(route.src)
        route.name = route.name || route.src.substr(route.src.lastIndexOf("/") + 1);
      if(route.name)
        route.path = route.path || "/" + route.name
      if(route.src) {
        route.props = { content: true };
        if(!route.noAuth)
          route.meta = { requiresAuth: true };
        let createPage = async () => {
          let component = await import("@/views" + route.src);
          return Vue.component(route.name, { extends: Page, mixins: [component.default] });
        };
        let page = createPage();
        let layout = route.noAuth ? this.layout.simple : this.layout.default;
        if(route.layout) {
          layout = this.layout[route.layout];
        }
        route.components = { layout, content: () => page };
      }
    }
    let router = new Router({routes});
    router.beforeEach((to, from, next) => {
      if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!auth.authenticated()) {
          next({
            name: "login",
            query: { redirect: to.fullPath }
          });
        } else {
          next();
        }
      } else {
        next();
      }
    });
    return router;
  }
}