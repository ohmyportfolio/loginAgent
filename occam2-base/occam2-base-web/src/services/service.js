import client from './client'
import util from './util'

const API_URI = "/api/";

let cache = {};

export default {
  deleteCache(resource, key) {
    if(this.enableCache) {
      let cacheKey1 = resource+"|"+key;
      delete cache[cacheKey1];
    }
  },
  execute(resouce, key, command, params, data) {
    this.deleteCache(resouce, key);
    if (params && params.filter) {
      params.condition = this.filterToCondition(params.filter);
      delete params.filter;
    }
    key = key || "any";
    return client.request({ method: "post", url: API_URI + resouce + "/"+ key +"/" +  command, data, params});
  },
  select(resource, params, dataOnly) {
    if (params && params.filter) {
      params.condition = this.filterToCondition(params.filter);
      delete params.filter;
    }
    return client
      .request({ method: "get", url: API_URI + resource, params })
      .then(response => (dataOnly ? response.data : { data: response.data, totalCount: response.total }));
  },
  async insert(resource, data) {
    let result = await client.request({ method: "post", url: API_URI + resource, data });
    return Array.isArray(data) ? result : result[0];
  },
  update(resource, key, data) {
    this.deleteCache(resource, key);
    return client.request({ method: "put", url: API_URI + resource + "/" + encodeURIComponent(key), data });
  },
  remove(resource, key) {
    this.deleteCache(resource, key);
    if(typeof key == "string")
        key = [key];
    return client.request({ method: "delete", url: API_URI + resource, data: key });
  },
  async get(resource, key, params) {
    let cacheKey1, cacheKey2;
    if(this.enableCache) {
      cacheKey1 = resource+"|"+key;
      cacheKey2 = JSON.stringify(params);
      cache[cacheKey1] = cache[cacheKey1] || {}; 
      if(cache[cacheKey1][cacheKey2])
        return cache[cacheKey1][cacheKey2];
    }
    let url = API_URI + resource;
    if(key)
      url += "/" + encodeURIComponent(key);
    let result = await client.request({ method: "get", url, params });
    if(this.enableCache) {
      cache[cacheKey1][cacheKey2] = result;
      cache[cacheKey1].__timestamp = new Date().getTime();
    }
    return result;
  },
  async getById(resource, id, select="id,name") {
    id = id.trim().replace(/'/g, "\''");
    let result = await client.request({ method: "post", url: API_URI + resource + "/any/select", data: {select, id} });
    if(result.total > 0)
        return result.data[0];
    else
        return null;
  },
  async getByName(resource, name, select="id,name") {
    name = name.trim();
    let result = await client.request({ method: "get", url: API_URI + resource, params: {condition: {name: name}, select} });
    if(result.total > 0)
        return result.data[0];
    else
        return null;
  },
  filterToCondition(filter, condition = {}) {    
    if ("string" == typeof filter[0] && "string" == typeof filter[1]) {
      // [field, operator, value] 형태
      let field = filter[0];
      field = util.replaceLast(field, "__", "."); // replace last __ to .
      if (field.indexOf(".") == -1)
        field = "this." + field;
      let operator = filter[1];
      let value = filter[2];
      if (value && value.toJSON)
        value = value.toJSON();
      if(value && value.replace)
        value = value.replace(/'/g, "\''"); // sql에서 ' to ''
      let isAlphabet = /[a-zA-Z]/i.test(value);
      if(value && isAlphabet) {
        if(operator == "contains")
          operator = "containsi";
        else if(operator == "notcontains")
            operator = "notcontainsi";
      }
      if(operator == "=") {
        if(condition[field]) {
          if(Array.isArray(condition[field])) {
            condition[field].push(value);
          } else {
            condition[field] = [condition[field], value];
          }
        } else {
          condition[field] = value;
        }
      } else {
        condition[field + " " + operator] = value; 
      }
    } else if (Array.isArray(filter[0]) && "string" == typeof filter[1] && "string" == typeof filter[2]) {
      // [field array, operator, value] 형태
      condition.or = {};
      filter[0].forEach((field) => {
        condition.or = Object.assign(condition.or, this.filterToCondition([field, filter[1], filter[2]]));
      });
    } else {
      // [[field, operator, value], [field, operator, value]] 형태
      let conditions = {};
      for (let i = 0; i < filter.length; i++) {
        if (Array.isArray(filter[i])) {
          // TODO: [[field, operator, value], 'and', [field, operator, value]] 형태가 제대로 변환되지 않음
          this.filterToCondition(filter[i], conditions);
        }
      }
      if(Object.keys(conditions).length == 1) {
        condition = Object.assign(condition, conditions);
      } else if(Object.keys(conditions).length > 1) {
        let logic = "and"
        if(typeof filter[1] == "string" && filter[1].trim().toLowerCase() == "or") {
          logic = "or"
        }
        if(!condition[logic]) {
          condition[logic] = conditions;
        } else if(!condition[logic+'$1']) {
          condition[logic+'$1'] = conditions;
        } else if(!condition[logic+'$2']) {
          condition[logic+'$2'] = conditions;
        } else if(!condition[logic+'$3']) {
          condition[logic+'$3'] = conditions;
        } else if(!condition[logic+'$4']) {
          condition[logic+'$4'] = conditions;
        } else if(!condition[logic+'$5']) {
          condition[logic+'$5'] = conditions;
        }
      }
    }
    return condition;
  },
  filterToSql(filter) {
    let sql = "";
    if ("string" == typeof filter[0] && "string" == typeof filter[1]) {
      let field = filter[0];
      field = util.replaceLast(field, "__", "."); // replace last __ to .
      if (field.indexOf(".") == -1)
        field = "this." + field;
      let operator = filter[1];
      let value = filter[2];
      if (value && value.toJSON)
        value = value.toJSON();
      if(value && value.replace)
        value = value.replace(/'/g, "\''"); // sql에서 ' to ''
      let isAlphabet = /[a-zA-Z]/i.test(value);
      if (operator == "=" && value == null) {
        operator = "isnull";
      }
      if (operator === "eq") {
        sql += field + " = " + "'" + value + "'";
      } else if (operator === "neq") {
        if (value == null)
          return;
        sql += field + " <> " + "'" + value + "'";
      } else if (operator === "startswith") {
        if (value == null)
          return;
        if(isAlphabet)
          sql += "lower(" + field + ") like " + "lower('" + value + "%')";
        else
          sql += field + " like '" + value + "%'";
      } else if (operator === "contains") {
        if (value == null)
          return;
        if(isAlphabet)
          sql += "lower(" + field + ") like " + "lower('%" + value + "%')";
        else
          sql += field + " like '%" + value + "%'";
      } else if (operator === "doesnotcontain") {
        if (value == null)
          return;
        if(isAlphabet)
          sql += "lower(" + field + ") not like " + "lower('%" + value + "%')";
        else
          sql += field + " not like '%" + value + "%'";
      } else if (operator === "endswith") {
        if (value == null)
          return;
        if(isAlphabet)
          sql += "lower(" + field + ") like " + "lower('%" + value + "')";
        else
          sql += field + " like '%" + value + "'";
      } else if (operator === "isnull") {
        sql += field + " is null";
      } else if (operator === "isnotnull") {
        sql += field + " is not null";
      } else if (operator === "isempty") {
        sql += field + " = ''";
      } else if (operator === "isnotempty") {
        sql += field + " <> ''";
      } else if (operator === "isnullorempty") {
        sql += "(" + field + " is null or " + sql + " = ''" + ")";
      } else if (operator === "isnotnullorempty") {
        sql += "(" + field + " is not null or " + sql + " = ''" + ")";
      } else {
        if (value == null)
          return;
        if (Array.isArray(value)) {
          let valueExpr = "(" + value.map((item) => "'" + item + "'").join(",") + ")";
          sql += field + " " + operator + " " + valueExpr;
        } else {
          sql += field + " " + operator + " '" + value + "'";
        }
      }
    } else if (Array.isArray(filter[0]) && "string" == typeof filter[1] && "string" == typeof filter[2]) {
      filter[0].forEach((field) => {
        if (sql != "")
          sql += " or "
        sql += this.filterToSql([field, filter[1], filter[2]]);
      });
    } else {
      for (let i = 0; i < filter.length; i++) {
        if (Array.isArray(filter[i])) {
          let filterSql = this.filterToSql(filter[i]);
          if (filterSql) {
            if(!sql.endsWith("and") && !sql.endsWith("or")  && sql != "")
              sql += " and"
            sql += " (" + filterSql + ")";
          }
        } else {
          if (filter[i])
            sql += " " + filter[i];
        }
      }
    }
    return sql;
  },
}
