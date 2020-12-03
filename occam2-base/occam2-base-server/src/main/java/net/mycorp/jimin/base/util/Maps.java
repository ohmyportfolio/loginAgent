package net.mycorp.jimin.base.util;

import net.mycorp.jimin.base.domain.OcMap;

public class Maps {
	
	public static Pair pair(String key, Object value) {
		Pair pair = new Pair();
		pair.setKey(key);
		pair.setValue(value);
		return pair;
	}

	public static Pair p(String key, Object value) {
		return pair(key, value);
	}

	public static OcMap map(Pair... pairs) {
		OcMap map = new OcMap();
		for (Pair pair : pairs) {
			map.put(pair.getKey(), pair.getValue());
		}
		return map;
	}

	public static OcMap map(Object... items) {
		OcMap map = new OcMap();
		for (int i = 0; i < items.length; i = i+2) {
			String key = (String) items[i];
			Object value = items[i+1];
			map.put(key, value);
		}
		return map;
	}
	
	public static OcMap m(Pair... pairs) {
		return map(pairs);
	}

}
