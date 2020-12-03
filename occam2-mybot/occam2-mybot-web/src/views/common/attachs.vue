<template>
  <div v-if="targetId && targetType" style="width:100%">
    <dx-progress-bar :min="0" :max="bytesTotal" :value="bytesLoaded" :status-format="statusFormat" v-show="loading" style="padding-bottom: 10px"/>
    <oc-data-grid ref="grid" :title="title" resource="attachs" :toolbarHeight="55" 
      :filter="[['target_type', '=', targetType], 'and', ['target_id', '=', targetId]]" 
      @row-dbl-click="open" @selection-changed="selectedFiles = $event.selectedRowsData" :showRefresh="true">
      <template v-slot:toolbar>
        <div class="h-box" style="padding-top: 10px">
          <dx-file-uploader width="90" ref="uploader" accept="*" :multiple="true" upload-mode="instantly" 
            :upload-url="'/api/attachs?target_type='+targetType+'&target_id='+targetId" 
            @uploaded="onUploaded" @progress="onProgress" @content-ready="onUploadReady"/>
          <dx-button text="다운로드" icon="download" @click="download" :disabled="selectedFiles.length == 0" stylingMode="text"/>
          <dx-button text="열기" icon="export" @click="open" :disabled="selectedFiles.length == 0" stylingMode="text"/>
        </div>
      </template>
      <oc-column data-field="id" caption="파일ID" width="100" :readOnly="true" :visible="false"/>
      <oc-column data-field="name" caption="파일명" :is-required="true" width="100%"/>
      <oc-column data-field="file_size" caption="파일크기" target-type="integer" :readOnly="true" width="130"/>
      <oc-column data-field="reg_date" caption="등록일시" target-type="datetime" :readOnly="true" width="180"/>
      <oc-column data-field="reg_user__name" caption="등록자" :readOnly="true" width="120"/>
      <dx-editing v-if="!$root.isMobile" :allow-deleting="true" :allow-updating="true"/>
    </oc-data-grid>
  </div>
</template>
<script>
export default {
  props: {
    title: { default: "첨부" },
    targetType: String,
    targetId: String
  },
  data() {
    return {
      selected: true,
      bytesLoaded: 0,
      bytesTotal: 0,
      uploadingFile: null,
      selectedFiles: [],
      loading: false
    }
  },
  methods: {
    onUploadReady(e) {
      let button = e.component._selectButton
      button.option("icon", "upload");
      button.option("stylingMode", "text");
    },
    onProgress(e) {
      this.loading = true;
      this.uploadingFile = e.file.name;
      this.bytesLoaded = e.bytesLoaded;
      this.bytesTotal = e.bytesTotal;
    },
    statusFormat(value) {
      return this.uploadingFile + " - " + this.util.formatFileSize(this.bytesLoaded) + " / " + this.util.formatFileSize(this.bytesTotal);
    },
    onUploaded(e) {
      this.loading = false;
      this.$refs.grid.refresh();
    },
    download(e) {
      let attach = this.selectedFiles[0]; 
      this.util.downloadURI("/api/attachs/"+attach.id+"/download", attach.name);
    },
    open(e) {
      let attach = this.selectedFiles[0]; 
      window.open("/api/attachs/"+attach.id+"/open");
    }
  }
}
</script>