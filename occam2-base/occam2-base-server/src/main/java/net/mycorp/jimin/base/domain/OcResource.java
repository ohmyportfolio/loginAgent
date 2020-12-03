package net.mycorp.jimin.base.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.util.Helper;

@XStreamAlias("resource")
public class OcResource extends OcNamed implements OcDatasourced {

	private static final String BASE_MIXIN = "base";

	private static final String DEFAULT_PKGENERATOR = "rand16c";

	@XStreamAsAttribute
	private String datasource;

	@XStreamAsAttribute
	private String table;

	@XStreamAsAttribute
	private String where;

	@XStreamAsAttribute
	private String orderby;
	
	@XStreamAsAttribute
	private String pk;

	@XStreamAsAttribute
	private String pkGenerator;

	private List<OcCommand> commands;

	private List<OcColumn> columns;
	
	@XStreamAsAttribute
	private String data;

	@JsonIgnore
	private Map<String, OcCommand> commandMap = new LinkedHashMap<>();

	@JsonIgnore
	private Map<String, OcColumn> columnMap = new LinkedHashMap<>();

	@XStreamAsAttribute
	private String mixins;

	@JsonIgnore
	private OcMeta meta;
	
	private Set<DbColumn> dbColumns;

	@XStreamAsAttribute
	private String depends;
	
	@XStreamAsAttribute
	private String parent;

	@XStreamAsAttribute
	private String partOf;

	@XStreamAsAttribute
	private boolean joinParent;
	
	@XStreamAsAttribute
	private String bean;

	@XStreamAsAttribute
	private boolean cacheable;
	
	private boolean fromDb;

	public enum LogLevel {
		all, list, write, none;
	}
	
	@XStreamAsAttribute
	private LogLevel logLevel = LogLevel.none;

	public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasources) {
		this.datasource = datasources;
	}

	public String getTable() {
		if(table != null)
			return table;
		if(table == null)
			return getName();
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}
	
	public String getPkGenerator() {
		return pkGenerator;
	}

	public void setPkGenerator(String pkGenerator) {
		this.pkGenerator = pkGenerator;
	}

	public List<OcCommand> getCommands() {
		return commands;
	}

	public void setCommands(List<OcCommand> commands) {
		this.commands = commands;
	}

	public List<OcColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<OcColumn> columns) {
		this.columns = columns;
	}

	public OcCommand getCommand(String name) {
		OcCommand command = commandMap.get(name);
		if (command != null)
			return command;
		if (BASE_MIXIN.equals(getName()))
			return null;
		for (String mixin : getMixinList()) {
			command = meta.getResource(mixin).getCommand(name);
			if (command != null)
				return command;
		}
		return null;
	}

	public OcColumn getColumn(String name, boolean required) {
		OcColumn column = getColumn(name);
		if(required && column == null) {
			throw new OccamException("column %s.%s not found.", getName(), name);
		} else {
			return column;
		}
	}
	
	public OcColumn getColumn(String name) {
		OcColumn column = columnMap.get(name);
		if (column != null)
			return column;
		if (BASE_MIXIN.equals(getName()))
			return null;
		for (String mixin : getMixinList()) {
			column = meta.getResource(mixin).getColumn(name);
			if (column != null)
				return column;
		}
		return null;
	}

	public String getMixins() {
		return mixins;
	}

	public void setMixins(String mixins) {
		this.mixins = mixins;
	}

	public List<String> getMixinList() {
		List<String> list = new ArrayList<>();
		if(parent != null)
			list.add(parent);
		if(Helper.empty(mixins))
			return list;
		String[] mixinArr = mixins.split(",");
		for (int i = 0; i < mixinArr.length; i++) {
			String mixin = mixinArr[i].trim();
			if(Helper.notEmpty(mixin)) {
				list.add(mixin);
			}
		}
		return list;
	}
	
	public void init(OcMeta meta) {
		this.meta = meta;
		
		if (pkGenerator == null)
			pkGenerator = DEFAULT_PKGENERATOR;
		if (mixins == null)
			mixins = BASE_MIXIN;

		columnMap = new LinkedHashMap<>();
		if (columns != null)
			for (OcColumn column : columns) {
				if (column.getName() == null)
					throw new OccamException("Null command name not allowed");
				columnMap.put(column.getName(), column);
			}

		commandMap = new LinkedHashMap<>();
		if (commands != null)
			for (OcCommand command : commands) {
				if (command.getName() == null)
					throw new OccamException("Null command name not allowed");
				commandMap.put(command.getName(), command);
			}
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(String where) {
		this.where = where;
	}

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public String getData() {
		return data;
	}

	public void setData(String rows) {
		this.data = rows;
	}

	public Set<DbColumn> getDbColumns() {
		return dbColumns;
	}

	public void setDbColumns(Set<DbColumn> dbColumns) {
		this.dbColumns = dbColumns;
	}

	public String getDepends() {
		return depends;
	}

	public void setDepends(String depends) {
		this.depends = depends;
	}

	public List<String> getDependList() {
		List<String> list = new ArrayList<>();
		if(parent != null)
			list.add(parent);
		if(Helper.empty(depends))
			return list;
		String[] dependArr = depends.split(",");
		for (int i = 0; i < dependArr.length; i++) {
			String depend = dependArr[i].trim();
			if(Helper.notEmpty(depend)) {
				list.add(depend);
			}			
		}
		return list;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getBean() {
		return bean;
	}

	public void setBean(String bean) {
		this.bean = bean;
	}

	public List<String> getPks() {
		return Helper.empty(pk) ? null : Arrays.asList(pk.split(","));
	}

	public void setPks(List<String> pks) {
		this.pk = String.join(",", pks);
	}

	public boolean isJoinParent() {
		return joinParent;
	}

	public void setJoinParent(boolean joinParent) {
		this.joinParent = joinParent;
	}

	public String getPartOf() {
		return partOf;
	}

	public void setPartOf(String partOf) {
		this.partOf = partOf;
	}

	public boolean isCacheable() {
		return cacheable;
	}

	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public void addColumn(OcColumn column) {
		if (column.getName() == null)
			throw new OccamException("Null column name not allowed");
		columnMap.put(column.getName(), column);
		columns = new ArrayList<OcColumn>(columnMap.values());
	}

	public boolean isFromDb() {
		return fromDb;
	}

	public void setFromDb(boolean fromDb) {
		this.fromDb = fromDb;
	}
}
