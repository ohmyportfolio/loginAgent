package net.mycorp.jimin.base.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

import net.mycorp.jimin.base.core.OccamException;

public class StringHelper {

	public static String toString(Object obj) {
		return toString(obj, 0);
	}

	public static String toString(Object obj, int indent) {
		if(obj == null)
			return null;
		StringBuilder s = new StringBuilder();
		Class<?> c = obj.getClass();
		if (c.isArray()) {
			Object[] array = (Object[]) obj;
			s.append('[').append("\n");
			for (Object object : array) {
				s.append(StringUtils.repeat("\t", indent));
				s.append(toString(object, indent++)).append("\n");
			}
			s.append(']').append("\n");
		} else if (obj instanceof Collection) {
			Collection<?> coll = (Collection<?>) obj;
			s.append('(').append("\n");
			for (Object object : coll) {
				s.append(toString(object, indent++)).append("\n");
			}
			s.append(')').append("\n");
		} else if (obj instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) obj;
			s.append('{').append("\n");
			for (Object key : map.keySet()) {
				s.append(key).append(':')
						.append(toString(map.get(key), indent++)).append("\n");
			}
			s.append('}').append("\n");
		} else {
			s.append(obj.toString());
		}
		return s.toString();
	}

	public static String substring(String content, String startToken,
			String endToken, boolean includeToken) {
		return substring(content, startToken, endToken, includeToken, null);
	}
	
	public static String substring(String content, String startToken,
			String endToken, boolean includeToken, String fromToken) {
		int fromIndex = 0;
		if(fromToken != null) {
			fromIndex = content.indexOf(fromToken);
		}
		if (startToken != null) {
			int startIndex = content.indexOf(startToken, fromIndex);
			if (startIndex == -1)
				return "";
			content = content.substring(startIndex
					+ (includeToken ? 0 : startToken.length()));
		}
		if (endToken != null) {
			int endIndex = content.indexOf(endToken);
			if (endIndex != -1)
				content = content.substring(0, endIndex
						+ (includeToken ? endToken.length() : 0));
		}
		return content;
	}

	public static String headToLowerCase(String name) {
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}

	public static String headToUpperCase(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}
	
	public static boolean isStringDouble(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}


	public static String covertOperator(String comparation) {
		if (Helper.empty(comparation) || "eq".equals(comparation)) {
			return "=";
		} else if ("lt".equals(comparation)) {
			return "<";
		} else if ("gt".equals(comparation)) {
			return ">";
		} else if ("le".equals(comparation)) {
			return "<=";
		} else if ("ge".equals(comparation)) {
			return ">=";
		} else if ("ne".equals(comparation)) {
			return "!=";
		} else {
			throw new IllegalArgumentException(comparation);
		}
	}

	private static boolean checkCommaList(String commaList, String item,
			String method, Class<?> argType) {
		if (commaList == null)
			return false;
		List<String> items = Arrays.asList(commaList.split(","));
		for (String listItem : items) {
			listItem = listItem.trim();
			try {
				if ((Boolean) item.getClass().getMethod(method, argType)
						.invoke(item, listItem))
					return true;
			} catch (Exception e) {
				throw new OccamException(e);
			}
		}
		return false;
	}

	public static boolean containsCommaList(String commaList, String item) {
		return checkCommaList(commaList, item, "equals", Object.class);
	}

	public static boolean equalsIgnoreCaseCommaList(String commaList,
			String item) {
		return checkCommaList(commaList, item, "equalsIgnoreCase", Object.class);
	}

	public static boolean startWithCommaList(String commaList, String item) {
		return checkCommaList(commaList, item, "startWith", String.class);
	}

	public static String readFirstLine(String myString) {
		Scanner scanner = new Scanner(myString);
		try {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				return line;
			}
			return null;
		} finally {
			scanner.close();
		}
	}

}
