package net.mycorp.jimin.base.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.functions.T;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class OcResult {

	private Long total;

	private List<OcMap> data;
	
	@JsonIgnore
	private Object array;

	public OcResult(List<OcMap> data, Long total) {
		this.data = data;
		this.total = total;
	}

	public OcResult(List<OcMap> data) {
		this.data = data;
	}
	
	public OcResult(long total) {
		this.total = total;
	}

	public OcResult() {
	}

	public Long getTotal() {
		if (total == null) {
			return (long) data.size();
		} else
			return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public List<OcMap> getData() {
		return data;
	}

	public void setData(List<OcMap> data) {
		this.data = data;
	}
	
	@JsonIgnore
	public OcMap getObject() {
		if(data == null || data.size() == 0)
			return null;
		else
			return data.get(0);
	}

	public Object getArray() {
		return array;
	}

	public void setArray(Object list) {
		this.array = list;
	}

	@JsonIgnore
	public Object getValue() {
		Map<String, Object> map = getObject();
		if(map == null)
			return null;
		String key = map.keySet().iterator().next();
		return map.get(key);
	}

	@JsonIgnore
	@SuppressWarnings({ "unchecked", "hiding" })
	public <T> List<T> getValues(Class<T> type) {
		List<T> values = new ArrayList<>();
		for (Object row : data) {
			Map<String, Object> map = (Map<String, Object>) row;
			T value = (T) map.values().iterator().next();
			values.add(value);
		}
		return values;
	}

	public long longValue() {
		Object value = getValue();
		if (value instanceof String)
			return Long.valueOf((String) value);
		else if (value instanceof Number)
			return ((Number) value).longValue();
		else
			return 0;
	}
}
