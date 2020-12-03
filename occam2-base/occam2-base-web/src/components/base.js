import CustomStore from 'devextreme/data/custom_store';
import DataSource from 'devextreme/data/data_source';

export default {
  methods: {
    getComponent(name) {
      let route = this.$router.options.routes.find((item) => item.name == name);
      if (!route)
        throw "route not found - name : " + name
      return route.components.content;
    },
    buildDataSource(options) {
      return new DataSource({
        paginate: true,
        pageSize: options == null || options.pageSize == null ? (this.$appInfo.pageSize || 12) : options.pageSize,
        store: this.buildDataStore(options)
      });
    },
    buildDataStore(options) {
      let that = this;
      let optionsEmpty = options == undefined; // item이나 column용은 options가 있음
      if (typeof options == "string")
        options = { resource: options };

      options = options || {};
      if (optionsEmpty) {
        options.resource = options.resource || this.resource;
        options.primaryKey = options.primaryKey || this.primaryKey;
        options.sort = options.sort || this.sort;
        options.filter = options.filter || this.filter;
        options.orderby = options.orderby || this.orderby;
        options.pageSize = options.pageSize == null ? this.pageSize : options.pageSize;
        options.defaultData = options.defaultData || this.defaultData;
        options.params = options.params || this.params;
        options.select = options.select || this.select;
        options.loadAfterUpdate = options.loadAfterUpdate || this.loadAfterUpdate;

        if(!options.select) {
          let childrenFields = this.getChildrenFields(this);
          if(childrenFields) {
            if(options.primaryKey) {
              options.select = options.primaryKey + "," + childrenFields;
            } else {
              options.select = childrenFields;
            }
          }
        }
        if (this.selectMore) {
          if(options.select) {
            options.select = options.select + "," + this.selectMore;
          } else {
            options.select = this.selectMore;
          }
        }
      }
      if (!options.resource)
        return;

      let dataStore = new CustomStore({
        key: options.primaryKey || "id",
        onModified: options.onModified,
        onRemoved: options.onRemoved,
        onInserted: options.onInserted,
        onUpdated: options.onUpdated,
        onLoaded: options.onLoaded,
        load: async (loadOptions) => {
          if(options.preLoad)
            options.preLoad(loadOptions);
          
          loadOptions = Object.assign({}, options, loadOptions);

          let params = Object.assign({}, loadOptions.params || options.params || {});
          params.offset = loadOptions.skip;
          params.limit = loadOptions.take;

          if (loadOptions.select) {
            params.select = loadOptions.select;
          }
          params.orderby = params.orderby || options.orderby;
          if (loadOptions.sort) {
            let passedParams = "";
            for (let sort of loadOptions.sort) {
              passedParams += sort.selector + " " + (sort.desc ? "desc" : "asc") + ", ";
            }
            params.orderby = passedParams + (params.orderby || "");
          }

          let filter = loadOptions.filter || [];
          if (typeof filter == 'function') {
            filter = filter(that.currentRow.data);
          }
          if(options.filter && options.filter != filter) {
            filter = [options.filter, filter];
          }
          if (loadOptions.searchExpr && loadOptions.searchValue) {
            filter = [filter, [loadOptions.searchExpr, loadOptions.searchOperation, loadOptions.searchValue]];
          }
          if (optionsEmpty && that.rootValue) {
            filter = [filter, [that.keyExpr || options.primaryKey, "<>", that.rootValue]];
          }
          if(params.filter) {
            params.filter = [params.filter, filter];
          } else {
            params.filter = filter;
          }
          let result = await that.service.select(options.resource, params);
          this.loadedData = result.data;
          return result;
        },
        insert: async (data) => {
          data = Object.assign({}, options.defaultData, data);
          let key = await that.service.insert(options.resource, data);
          if(options.loadAfterUpdate) {
            data = await dataStore.byKey(key);
            return data;
          } else {
            data[dataStore.key()] = key;
            return data;
          }
        },
        update: async (key, data) => {
          if (Array.isArray(options.primaryKey)) {
            key = options.primaryKey.map((pk) => key[pk]).join(",");
          }
          await that.service.update(options.resource, key, data);
          if(options.loadAfterUpdate) {
            return await dataStore.byKey(key);
          } else {
            return data;
          }
        },
        remove: key => {
          if (!Array.isArray(key))
            key = [key];
          return that.service.remove(options.resource, key);
        },
        byKey: function (key) {
          let params = {};
          if (options.select) {
            params.select = options.select;
          }
          options.resource = options.resource.split("/")[0];
          if(this.loadedData) {
            let item = this.loadedData.find((item) => item.id == key);
            if(item != null)
              return item;
          }
          return that.service.get(options.resource, key, params);
        }
      });
      return dataStore;
    },
    log(message) {
      let now = new Date();
      message = now.getMinutes() + ":" + now.getSeconds() + "." + now.getMilliseconds() + " " + message;
      console.log(message);
    },
    debug($event) {
      debugger;
    },
  }
}