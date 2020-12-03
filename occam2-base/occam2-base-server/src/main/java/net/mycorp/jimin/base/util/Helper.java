package net.mycorp.jimin.base.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcMap;

public class Helper {

	public static boolean exist(Object object) {
		return !empty(object);
	}

	public static boolean eq(Object left, Object right) {
		return (left != null && left.equals(right))
				|| (left == null && right == null);
	}

	public static boolean empty(Object object) {
		return object == null
				|| (object instanceof String && ((String) object).length() == 0)
				|| (object instanceof Double && ((Double) object).isNaN())
				|| (object instanceof Float && ((Float) object).isNaN());
	}
	
	public static boolean notEmpty(Object object) {
		return !empty(object);
	}
	
	public static Object altNull(Object value, Object nullAlt) {
		if(value == null)
			return nullAlt;
		else
			return value; 
	}
	
	public static Object altNull(Object value, Object nullAlt, Object notNullAlt) {
		if(value == null)
			return nullAlt;
		else
			return notNullAlt; 
	}

	public static boolean isListType(Class<?> type) {
		return type.isArray() || type.isAssignableFrom(Iterable.class);
	}

	public static boolean isBasicType(Class<?> type) {
		return isNumberType(type) || type == String.class
				|| type == boolean.class || type == Boolean.class
				|| type == Date.class;
	}

	public static boolean isNumberType(Class<?> type) {
		return Number.class.isAssignableFrom(type) || type == byte.class
				|| type == int.class || type == long.class
				|| type == float.class || type == double.class;
	}
	
	public static String getUuid() {
		return UUID.randomUUID().toString();
	}
	
	public static String getRandomAlphanumeric(int length) {
		return RandomStringUtils.randomAlphanumeric(length);
	}

	public static String getRand16c() {
		return getRandomAlphanumeric(16);
	}
	
	public static String formatDate(Date date , String format) {
		return DateTimeFormatter.ofPattern(format,Locale.KOREA).format(Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate());
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> findParentItem(final List<Map<String, Object>> sourceList, String parentId) {
		Map<String, Object> resultParentItem = null;
		
		for(int i=0; i<sourceList.size() && resultParentItem == null; i++) {
			Map<String, Object> item = sourceList.get(i);
			if(item.get("id").toString().equals(parentId)) {
				resultParentItem = item;
			} else if(item.get("items") != null) {
				resultParentItem = findParentItem((List<Map<String, Object>>) item.get("items"), parentId);
			}
		}
		return resultParentItem;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> sortHierarchicalList(final List<? extends Map<String, Object>> originalList,
			String parentField , String childField) {
	    final List<Map<String, Object>> copyList = new ArrayList<>(originalList);
	    
	    originalList.forEach(original -> {
	    	String parentId = (String) original.get(parentField);
	    	String childId = (String) original.get(childField);
	    	Map<String, Object> child = new HashMap<String, Object>();
	    	
	    	if(!StringUtils.isBlank(parentId) && !"null".equals(parentId)) {
	    		for(int i=0; i<copyList.size(); i++) {
		    		Map<String, Object> copyItem = copyList.get(i);
		    		if(copyItem.get("id").toString().equals(childId)) {
		    			child = copyItem;
		    		}
		    	}
		    	
		    	Map<String, Object> parent = findParentItem(copyList, parentId);
		    	if(parent != null) {
		    		if(parent.get("items") == null) {
		    			List<Map<String, Object>> items = new ArrayList<>();
		    			items.add(new HashMap<String, Object>(child));
		    			parent.put("items", items);
		    		} else {
		    			List<Map<String, Object>> items = (List<Map<String, Object>>) parent.get("items");
		    			items.add(new HashMap<String, Object>(child));
		    		}
		    		copyList.remove(child);
		    	}
	    	}
	    });
	    return copyList;
	}

	private static ObjectMapper mapper = new ObjectMapper();
	{
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	public static String toJson(Object obj) {
		if(obj == null)
			return null;
		if(obj instanceof String)
			return (String) obj;
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new OccamException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(Object source) {
		if(source instanceof Map)
			return (Map<String, Object>) source;
		else
			return mapper.convertValue(source, Map.class);
	}

	public static Object parseJson(String value) {
		try {
			if (value == null || value.equals(""))
				return value;
			else if (value.startsWith("["))
				return mapper.readValue(value, List.class);
			else if (value.startsWith("{"))
				return mapper.readValue(value, Map.class);
			else
				return mapper.readValue(value, Number.class);
		} catch (Exception e) {
			throw new OccamException(e);
		}
	}
	
	public static List<?> toBean(Class<?> beanClass, List<? extends Map<String, Object>> maps) {
		try {
			ArrayList<Object> resultRows = new ArrayList<Object>();
			for (Map<String, Object> map : maps) {
				resultRows.add(toBean(beanClass, map));
			}
			return resultRows;
		} catch(Exception e) {
			throw new OccamException(e);
		}
	}

	public static <T> T toBean(Class<T> beanClass, Map<String, Object> map) {
		T bean = mapper.convertValue(map, beanClass);
		if(bean instanceof OcMap) {
			OcMap ocMap = (OcMap)bean;
			ocMap.putAll(map);
		}
		return bean;
	}
	
	public static Object cloneBean(Object source) {
		if(source == null)
			return null;
		try {
			return BeanUtils.cloneBean(source);
		} catch (Exception e) {
			throw new OccamException(e);
		}
	}
	
	public static String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}
}