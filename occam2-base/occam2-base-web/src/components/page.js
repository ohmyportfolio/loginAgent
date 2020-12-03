import Base from './base'

export default {
    props: {
        dataId: null,
        openMode: { default: "popup" },
        title: String,
        defaultData: { default: () => {} },
        params: { default: () => { return {} } },
    },
    extends: Base,
    data() {
        return {
            formData: {},
            tabIndex: 0,
        };
    },
    methods: {
        close(success) {
            if(this.openMode == "popup") {
                this.$emit("close", success == true);
                this.$parent.$emit("close", success == true);
            } else if(this.openMode == "route") {
                this.$router.go(-1);
            }
        },
        isNew() {
            return !this.dataId || this.dataId == "new";
        }
    }
}