import axios from 'axios';
import qs from 'qs';

import notify from 'devextreme/ui/notify';

export default {
  log(message) {
    let now = new Date();
    message = now.getMinutes() + ":" + now.getSeconds() + "." + now.getMilliseconds() + " " + message;
    console.log(message);
  },
  request (config) {
    config.url = config.url;
    config.data = this.filterRequestData(config.data);
    config.params = this.filterParams(config.params);
    config.paramsSerializer = (params) => { return qs.stringify(params) };
    // this.log("request begin " + config.url);
    let result = axios.request(config).then((response) => {
      // this.log("request end " + config.url);
      return this.filterResponseData(response.data);
    });
    result.catch((error) => {
      let message = (error.response && error.response.data) ? (error.response.data.message || error.response.data) : error.message;
      if(error.response && error.response.data && error.response.data.byteLength) {
        let arrayBuffer = new Uint8Array(error.response.data);
        let data = JSON.parse(String.fromCharCode.apply(null, arrayBuffer));
        message = data.message;
      }
      if(message)
        message = message.replace("net.exinno.occam2.base.core.OccamException: ", "");
      notify({message}, 'error', 10000);
      if(error.response && error.response.status == 403) {
        location.reload();
        return;
      }
    });
    return result;
  },
  filterResponseData (data) {
    if (!data) {
      return data
    }
    if (Array.isArray(data)) {
      for (var i in data) {
        this.filterResponseData(data[i])
      }
    } else {
      Object.keys(data).forEach(function (key) {
        if (typeof data[key] === 'string' && data[key].length === 28 && data[key].substr(10, 1) === 'T') {
          // IE 11 호환을 위해
          data[key] = new Date(data[key].substr(0, 26) + ":" + data[key].substr(26, 2))
        } else if (Array.isArray(data[key])) {
          var array = data[key]
          for (var i = 0; i < array.length; i++) {
            this.filterResponseData(array[i])
          }
        }
      }, this)
    }
    return data
  },
  filterRequestData (data) {
    if (!data) {
      return data
    }
    if (Array.isArray(data)) {
      for (var i in data) {
        this.filterRequestData(data[i])
      }
    } else {
      Object.keys(data).forEach(function (key) {
        const dataType = Object.prototype.toString.call(data[key])
        if (Array.isArray(data[key])) {
          var array = data[key]
          for (var i = 0; i < array.length; i++) {
            this.filterRequestData(array[i])
          }
        } else if (dataType === '[object Object]' && data[key] != null) {
          data[key + '_id'] = data[key].id
          data[key] = undefined
        } else if (dataType === '[object Function]') {
          data[key] = undefined
        }
      }, this)
    }
    return data
  },
  filterParams (params) {
    if (!params) {
      return params
    }
    params = Object.assign({}, params)
    Object.keys(params).forEach(function (key) {
      let type = Object.prototype.toString.call(params[key]);
      if (type === '[object Function]') {
        params[key] = undefined;
      } else if (type === '[object Object]') {
        params[key] = JSON.stringify(params[key]);
      }
    }, this)
    return params
  }
}
