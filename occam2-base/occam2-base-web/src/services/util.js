import Vue from "vue";

import client from './client';
import { saveAs } from 'file-saver';

export default {
  formatFileSize(size) {
    if (isNaN(size) || size == 0)
      return size;
    var i = Math.floor(Math.log(size) / Math.log(1024));
    return (size / Math.pow(1024, i)).toFixed(2) * 1 + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i];
  },
  downloadAll(urls) {
    var link = document.createElement('a');
  
    link.setAttribute('download', null);
    link.style.display = 'none';
  
    document.body.appendChild(link);
  
    for (var i = 0; i < urls.length; i++) {
      link.setAttribute('href', urls[i]);
      link.click();
    }
  
    document.body.removeChild(link);
  },  
  downloadURI(uri, name) {
    var link = document.createElement("a");
    if (name)
      link.download = name;
    link.href = uri;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  },
  downloadPost(url, data, fileName) {
    client.request({
      method: 'post', url: url,
      responseType: 'arraybuffer', data: data
    }).then(response => {
      var blob = new Blob([response]);
      saveAs(blob, fileName);
    });
  },
  assignObject(target, source) {
    Object.keys(target).forEach((key) => { target[key] = undefined });
    Object.keys(source).forEach((key) => { Vue.set(target, key, source[key]) });
    // Object.assign(target, source);
  },
  deepCompare(newVal, oldVal) {
    return newVal == oldVal || JSON.stringify(newVal) == JSON.stringify(oldVal);
  },
  getChangedData(oldData, newData) {
    let changedData = {};
    for(let key of Object.keys(newData)) {
      if(newData[key] != oldData[key])
        changedData[key] = newData[key];
    }
    return changedData;
  },
  truncateDate(date) {
    date.setHours(0);
    date.setMinutes(0);
    date.setSeconds(0);
    date.setMilliseconds(0);
    return date;
  },
  adjustDate(field, value, date) {
    if (!date)
      date = this.truncateDate(new Date());
    field = this.capitalize(field);
    date['set' + field](date['get' + field]() + value);
    return date;
  },
  diffDays(first, second) {
    return Math.round((second-first)/(1000*60*60*24));
  },
  diffWorkingDays(startDate, endDate) {
    var count = 0;
    var curDate = new Date(startDate);
    while (curDate <= endDate) {
        var dayOfWeek = curDate.getDay();
        if(!((dayOfWeek == 6) || (dayOfWeek == 0)))
           count++;
        curDate.setDate(curDate.getDate() + 1);
    }
    return count;
  },
  addWorkingDays(fromDate, days) {
    fromDate = new Date(fromDate);
    var count = 0;
    while (count < days) {
        fromDate.setDate(fromDate.getDate() + 1);
        if (fromDate.getDay() != 0 && fromDate.getDay() != 6) // Skip weekends
            count++;
    }
    return fromDate;
  },
  capitalize(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  },
  replaceLast(str, searchValue, replaceValue) {
    if(str.lastIndexOf(searchValue) == -1)
      return str;
    else {
      let index = str.lastIndexOf(searchValue);
      return str.substr(0, index) + str.substr(index).replace(searchValue, replaceValue);
    }
  },
  openWindow(page, id) {
    window.open("#/" + page + "/" + id);
  },
  copyToClipboard (str) {
    var el = document.createElement('textarea');
    el.value = str;
    el.setAttribute('readonly', '');
    // IE 에선 style 이 readonly 속성이라 안됨..
    //el.style = { position: 'absolute', left: '-9999px' };
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
  },
  parseJSON(str) {
    if(!str)
      return;
    let v;
    eval("v="+str)
    return v;
  },
  isJSON(str) {
    try {
      this.parseJSON(str);
      return true;
    } catch {
      return false;
    }
  },
  redirect(url) {
    window.location.href = url;
  },
  pad(n, width) {
    n = n + '';
    return n.length >= width ? n : new Array(width - n.length + 1).join('0') + n;
  }
}
