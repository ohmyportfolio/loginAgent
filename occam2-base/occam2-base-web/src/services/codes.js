import client from './client'

var that = {
  list: {},
  map: {},
  loaded: false,
  codeGroupField: 'type_id',
  codeField: 'code',
  labelField: 'name',
  async load() {
    if (that.loaded) {
      return
    }
    let result = await client.request({ method: "get", url: "/api/codes" });
    that.list = {}
    that.map = {}
    let items = result.data
    for (var index = 0; index < items.length; index++) {
      var code = items[index]
      
      if (!that.list[code[that.codeGroupField]]) {
        that.list[code[that.codeGroupField]] = []
        that.map[code[that.codeGroupField]] = {}
      }

      that.list[code[that.codeGroupField]].push(code)
      that.map[code[that.codeGroupField]][code[that.codeField]] = code
    }
    that.loaded = true
  },
  label(codeGroup, code) {
    if (this.map[codeGroup] && this.map[codeGroup][code]) {
      return this.map[codeGroup][code][that.labelField]
    } else if (code) {
      return code
    } else {
      return ''
    }
  }
}

window.codes = that

export default that
