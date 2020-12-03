package net.mycorp.jimin.base.domain;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.util.Helper;

public class OcMap extends LinkedHashMap<String, Object> implements ApplicationContextAware{

	private static final long serialVersionUID = 2388815315184995023L;
	
	private static ApplicationContext applicationContext;
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	public OcMap() {
		super();
	}
	
	public OcMap(Object row) {
		super(Helper.toMap(row));
	}

	public String getString(String name, String defaultVal) {
		String val = getString(name);
		if(val == null)
			val = defaultVal;
		return val;
	}
	
	public String getString(String name) {
		Object val = get(name);
		if(val == null) {
			return null;
		} else if(val instanceof String) {
			return (String) val;
		} else if(val instanceof List) {
			return (String) ((List<?>) val).get(0);			
		} else {
			return String.valueOf(val);
		}
	}

	public OcMap getMap(String name) {
		return getMap(name, false);
	}
	
	public OcMap getMap(String name, boolean create) {
		if(Helper.empty(name)) {
			return this;
		}		
		Object map = get(name);
		if(map == null && !create) {
			return null;
		} else if(map == null) {
			map = new OcMap();
			put(name, map);
			return (OcMap) map;
		} else if(map instanceof OcMap) {
			return (OcMap) get(name);
		} else if(map instanceof Map) {
			return new OcMap(get(name));			
		} else if(map instanceof String) {
			try {
				if(!Helper.empty(map))
					map = mapper.readValue((String) map, OcMap.class);
				else
					map = null;
				put(name, map);
				return (OcMap) map;
			} catch (IOException e) {
				throw new OccamException(e);
			}
		} else {
			throw new OccamException("value %s type must be Map or String", map);
		}
	}
	
	public int getInt(String name) {
		Object value = get(name);
		if (value instanceof String)
			return Integer.valueOf((String) value);
		else if (value instanceof Number)
			return ((Number) value).intValue();
		else
			return 0;
	}

	public long getLong(String name) {
		Object value = get(name);
		if (value instanceof String)
			return Long.valueOf((String) value);
		else if (value instanceof Number)
			return ((Number) value).longValue();
		else
			return 0;
	}

	public boolean getBool(String name) {
		Object value = get(name);
		if (value instanceof String)
			return Boolean.valueOf((String) value);
		else if (value instanceof Number)
			return (int) value != 0;
		else if (value == null)
			return false;
		else
			return (boolean) value;
	}

	public Date getDate(String name) {
		Object value = get(name);
		if (value instanceof Date) {
			return (Date)value;
		} else if(value instanceof String) {
			try {
				return mapper.readValue((String)value, Date.class);
			} catch (IOException e) {
				throw new OccamException(e);
			}
		} else {
			return null;
		}
	}
	
	public DateTime getDateTime(String name) {
		Object value = get(name);
		if (value instanceof DateTime) {
			return (DateTime)value;
		} else if (value instanceof Date) {
			return new DateTime((Date)value);
		} else if(value instanceof String) {
			try {
				return new DateTime(mapper.readValue((String)value, Date.class));
			} catch (IOException e) {
				throw new OccamException(e);
			}
		} else {
			return null;
		}
	}
	
	public OcMap addAll(Map<String, Object> row) {
		this.putAll(row);
		return this;
	}
	
	public OcMap add(String key, Object value) {
		put(key, value);
		return this;
	}

	public OcMap add(String key1, Object value1, String key2, Object value2) {
		put(key1, value1);
		put(key2, value2);
		return this;
	}

	public OcMap add(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
		put(key1, value1);
		put(key2, value2);
		put(key3, value3);
		return this;
	}

	public OcMap add(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4,
			Object value4) {
		put(key1, value1);
		put(key2, value2);
		put(key3, value3);
		put(key4, value4);
		return this;
	}
	
	@Override
	public Object get(Object key) {
		return super.get(key);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		applicationContext = ctx;
	}
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	public static Object getBean(Class<?> classType) {
	    ApplicationContext applicationContext = OcMap.getApplicationContext();
	    return applicationContext.getBean(classType);
	}

	public boolean eq(String key, Object target) {
		Object value = get(key);
		return value == target || (value != null && get(key).equals(target));
	}
	
	public String id() {
		return getString("id");
	}

	public OcMap sub(String ...keys) {
		OcMap subMap = new OcMap();
		for (String key : keys) {
			Object value = get(key);
			if(value != null)
			subMap.put(key, value);
		}
		return subMap;
	}

	@SuppressWarnings("unchecked")
	public List<String> getStrings(String key) {
		return (List<String>) get(key);
	}

	@SuppressWarnings("unchecked")
	public List<OcMap> getMaps(String key) {
		return (List<OcMap>) get(key);
	}

	public void putIfNull(String key, Object object) {
		if(get(key) != null)
			return;
		put(key, object);
	}

	public void putAllIfNull(Map<String, Object> source) {
		if(source == null)
			return;
		for(String key : source.keySet()) {
			putIfNull(key, source.get(key));
		}
	}
	
	public <T> T toBean(Class<T> beanClass) {
		return Helper.toBean(beanClass, this);
	}
}
