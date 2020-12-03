package net.mycorp.jimin.base.domain;

import static net.mycorp.jimin.base.core.Global.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import net.mycorp.jimin.base.core.OccamException;

public class OcContext extends OcMap {

	private static final long serialVersionUID = 6096286580867668276L;

	private List<OcMap> rows;
	
	private Date created = new Date();

	public OcContext() {
		super();
	}

	public OcContext(String resource, String command) {
		super();
		setResource(resource);
		setCommand(command);
	}

	public OcContext(String command, Map<String, Object> pathVariable, Map<String, Object> requestParam) {
		super();
		putAll(pathVariable);
		putAll(requestParam);
		setCommand(command);
	}

	public OcContext(Map<String, Object> contextMap) {
		if(contextMap == null)
			return;
		putAll(contextMap);
		try {
			BeanUtils.copyProperties(this, contextMap);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new OccamException(e);
		}
	}

	public OcContext add(String key, Object value) {
		put(key, value);
		return this;
	}

	public String getSelect() {
		return getString("select");
	}

	public void setSelect(String select) {
		put("select", select);
	}

	public OcContext select(String select) {
		setSelect(select);
		return this;
	}

	public String getWhere() {
		return getString("where");
	}

	public void setWhere(String where) {
		put("where", where);
	}

	public OcContext where(String where) {
		setWhere(where);
		return this;
	}
	
	public OcMap getCondition() {
		return getMap("condition", true);
	}

	public void setCondition(Map<String, Object> condition) {
		put("condition", condition);
	}
	
	public OcContext condition(Map<String, Object> condition) {
		setCondition(condition);
		return this;
	}
	
	public OcContext condition(Object... items) {
		Map<String, Object> condition = getCondition();
		for (int i = 0; i < items.length; i = i+2) {
			String key = (String) items[i];
			Object value = items[i+1];
			condition.put(key, value);
		}
		return this;
	}

	public String getOrderby() {
		return getString("orderby");
	}

	public void setOrderby(String orderby) {
		put("orderby", orderby);
	}

	public OcContext orderby(String orderby) {
		setOrderby(orderby);
		return this;
	}

	/**
	 * resource xml의 command만 호출하게 함
	 * @return
	 */
	public boolean getXmlOnly() {
		return getBool("xmlOnly");
	}

	public void setXmlOnly(boolean xmlOnly) {
		put("xmlOnly", xmlOnly);
	}
	
	public Integer getLimit() {
		return getInt("limit");
	}

	public void setLimit(Integer limit) {
		put("limit", limit);
	}

	public OcContext limit(Integer limit) {
		setLimit(limit);
		return this;
	}

	public Integer getOffset() {
		return getInt("offset");
	}

	public void setOffset(Integer offset) {
		put("offset", offset);
	}

	public OcContext offset(Integer offset) {
		setOffset(offset);
		return this;
	}

	public Object getId() {
		return get("id");
	}
	
	@SuppressWarnings("unchecked")
	public List<String> ids() {
		Object id = get("id");
		if(id instanceof List) {
			return (List<String>) id;
		} else {
			List<String> ids = new ArrayList<String>();
			ids.add((String) id);
			return ids;
		}
	}
	
	public void setId(Object id) {
		put("id", id);
	}

	public OcContext id(Object id) {
		setId(id);
		return this;
	}

	public String getResource() {
		String resource = getString("resource");
		if(resource == null)
			return getCommandResource();
		else
			return resource;
	}

	public void setResource(String resource) {
		put("resource", resource);
	}

	public OcContext resource(String resource) {
		setResource(resource);
		return this;
	}

	public String getCommand() {
		return getString("command");
	}

	public void setCommand(String command) {
		put("command", command);
	}

	public String getCommandResource() {
		return getString("commandResource");
	}

	public void setCommandResource(String commandResource) {
		put("commandResource", commandResource);
	}
	
	public OcContext command(String command) {
		setCommand(command);
		return this;
	}

	public List<OcMap> getRows() {
		return rows;
	}

	public void setRows(List<?> rows) {
		this.rows = new ArrayList<OcMap>();
		for (Object row : rows) {
			if(row instanceof OcMap)
				this.rows.add((OcMap) row);
			else
				this.rows.add(new OcMap(row));
		}
	}

	public OcMap getRow() {
		return rows != null ? rows.get(0) : null;
	}

	public void setRow(OcMap row) {
		rows = new ArrayList<OcMap>();
		rows.add(row);
	}
	
	public OcContext row(Object row) {
		if(row == null)
			return this;
		if (row instanceof List) {
			setRows((List<?>) row);
		} else if (row instanceof OcMap) {
			setRow((OcMap) row);
		} else {
			setRow(new OcMap(row));
		}
		return this;
	}

	public OcContext rows(List<?> rows) {
		this.rows = new ArrayList<OcMap>();
		for (Object row : rows) {
			if(row instanceof OcMap) {
				this.rows.add((OcMap)row);
			} else {
				this.rows.add(new OcMap(row));
			}
		}
		return this;
	}

	public OcMap row() {
		if (getRow() == null) {
			OcMap row = new OcMap();
			setRow(row);
			return row;
		} else {
			OcMap row = new OcMap(getRow());
			setRow(row);
			return row;
		}
	}

	public OcContext row(String key, Object value) {
		OcMap row = row();
		row.put(key, value);
		return this;
	}

	public OcContext row(String key1, Object value1, String key2, Object value2) {
		OcMap row = row();
		row.put(key1, value1);
		row.put(key2, value2);
		return this;
	}

	public OcContext row(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
		OcMap row = row();
		row.put(key1, value1);
		row.put(key2, value2);
		row.put(key3, value3);
		return this;
	}

	public OcContext row(String key1, Object value1, String key2, Object value2, String key3, Object value3,
			String key4, Object value4) {
		OcMap row = row();
		row.put(key1, value1);
		row.put(key2, value2);
		row.put(key3, value3);
		row.put(key4, value4);
		return this;
	}
	
	public Object execute() {
		return commands.execute(this);
	}
	
	public void setCommandPath(String commandPath) {
		String[] commandPaths = commandPath.split("\\.");
		if (commandPaths.length == 2) {
			setCommandResource(commandPaths[0]);
			setCommand(commandPaths[1]);
		} else if (commandPaths.length == 1) {
			setCommand(commandPaths[0]);
		} else {
			throw new OccamException("Illegal commandPath %s", commandPath);
		}
	}

	public OcContext addAll(Map<String, Object> requestParam) {
		putAll(requestParam);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public List<OcMap> list() {
		Object result = commands.execute(this);
		if(result instanceof OcResult) {
			return ((OcResult) result).getData();
		} else {
			return (List<OcMap>) result;
		}
	}

	public OcMap map() {
		return list().get(0);
	}

	public Object value() {
		List<OcMap> list = list();
		return list.size() == 0 ? null : list.get(0).values().iterator().next();
	}

	public OcContext fromClient(boolean val) {
		if(val)
			skipPermit(false);
		put("_fromClient", val);
		return this;
	}
	
	public boolean isFromClient() {
		return getBool("_fromClient");
	}

	public OcContext skipPermit(boolean val) {
		put("_skipPermit", val);
		return this;
	}
	
	public boolean isSkipPermit() {
		return getBool("_skipPermit");
	}
	
	public OcContext noLogRow(boolean val) {
		put("_noLogRow", val);
		return this;
	}
	
	public boolean isNoLogRow() {
		return getBool("_noLogRow");
	}
	
	public Date getCreated() {
		return created;
	}
	
}
