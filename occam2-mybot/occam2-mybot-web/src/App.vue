<template>
  <div id="root">
    <div :class="cssClasses">
      <router-view
        name="layout"
        :title="title"
        :is-x-small="screen.isXSmall"
        :is-large="screen.isLarge">
        <div class="content">
          <router-view name="content" :key="$route.fullPath" style="max-height: 100vh"/>
        </div>
      </router-view>
    </div>
  </div>
</template>

<script>
import { sizes, subscribe, unsubscribe } from "../../../occam2-base/occam2-base-web/src/utils/media-query";
function getScreenSizeInfo() {
  const screenSizes = sizes();

  return {
    isXSmall: screenSizes["screen-x-small"],
    isLarge: screenSizes["screen-large"],
    cssClasses: Object.keys(screenSizes).filter(cl => screenSizes[cl])
  };
}

export default {
  name: "app",
  data() {
    return {
      title: this.$appInfo.title,
      screen: getScreenSizeInfo()
    };
  },
  computed: {
    cssClasses() {
      return ["app"].concat(this.screen.cssClasses);
    }
  },
  methods: {
    screenSizeChanged() {
      this.screen = getScreenSizeInfo();
    }
  },

  mounted() {
    subscribe(this.screenSizeChanged);
  },

  beforeDestroy() {
    unsubscribe(this.screenSizeChanged);
  }
};
</script>

<style lang="scss">
html,
body {
  margin: 0px;
  min-height: 100%;
  height: 100%;
}

#root {
  height: 100%;
}

* {
  box-sizing: border-box;
}

.app {
  @import "./themes/generated/variables.base.scss";
  display: flex;
  height: 100%;
  width: 100%;
}
</style>
