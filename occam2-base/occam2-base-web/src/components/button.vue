<template>
  <dx-button v-bind="$attrs" v-on="$listeners" @click="handleClick" :disabled="disabled"/>
</template>
<script>
import notify from 'devextreme/ui/notify';

export default {
  props: {
    onClick: Function,
    successMessage: String,
    failMessage: String,
  },
  data() {
    return {
      disabled: false,
    }
  },
  methods: {
    async handleClick(e) {
      if(this.onClick == null)
        return;
      this.disabled = true;
      try {
        await this.onClick(e);
        if(this.successMessage)
          notify({ message: this.successMessage }, 'info');
      } catch {
        if(this.failMessage)
          notify({ message: this.failMessage }, 'error');
      }
      this.disabled = false;
    }
  }
}
</script>