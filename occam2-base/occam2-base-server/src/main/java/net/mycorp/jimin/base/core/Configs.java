package net.mycorp.jimin.base.core;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.mycorp.jimin.base.util.StringHelper;

public class Configs {

	private static Properties properties;

	private static boolean loaded;

	private static Logger log = LoggerFactory.getLogger(Configs.class);

	private static String propertiesPath = "/jimin/config.properties";
	
	static {
		init();		
		load();
	}

	private static void init() {
		URL propertiesResource = Configs.class.getResource(propertiesPath);
		if(propertiesResource == null) {
			throw new OccamException("Properties not found. %s", propertiesPath);
		}
		final File propertiesFile = new File(propertiesResource.getFile());
		if(!propertiesFile.isFile())
			throw new OccamException("Properties not found. %s", propertiesPath);
		File propertiesDir = propertiesFile.getParentFile();
        FileAlterationObserver observer = new FileAlterationObserver(propertiesDir);
        FileAlterationMonitor monitor = new FileAlterationMonitor(1000);
        observer.addListener(new FileAlterationListenerAdaptor() {			
			@Override
			public void onFileChange(File file) {
				if(file.equals(propertiesFile))
					reload();
			}
		});
        monitor.addObserver(observer);
        try {
			monitor.start();
		} catch (Exception e) {
			throw new OccamException(e);
		}
	}

	public static void load() {
		if (loaded)
			return;
		InputStream propIs = Configs.class.getResourceAsStream(propertiesPath);
		try {
			if (propIs == null)
				throw new OccamException("Properties not found. %s", propertiesPath);
			properties = System.getProperties();
			properties.load(propIs);
			String logMessage = "Configs loaded from " + propertiesPath;
			System.out.println(logMessage);
			log.info(logMessage);
		} catch (Exception e) {
			throw new OccamException(e);
		}
		loaded = true;
	}

	public static void reload() {
		loaded = false;
		load();
	}

	public static String get(String key) {
		return get(key, null);
	}

	public static String get(String key, String defaultValue) {
		String value = properties.getProperty(key);
		return value == null ? defaultValue : value;
	}

	public static void set(String key, String value) {
		properties.setProperty(key, value);
	}

	public static void set(String key, Object value) {
		properties.setProperty(key, String.valueOf(value));
	}

	public static Boolean getBoolean(String key) {
		return getBoolean(key, null);
	}

	public static Boolean getBoolean(String key, Boolean defaultValue) {
		return Boolean.valueOf(get(key, String.valueOf(defaultValue)));
	}

	public static Long getLong(String key) {
		return getLong(key, null);
	}

	public static Long getLong(String key, Long defaultValue) {
		return Long.valueOf(get(key, String.valueOf(defaultValue)));
	}

	public static Integer getInteger(String key) {
		return getInteger(key, null);
	}
	
	public static String[] getArray(String key) {
		String value = get(key);
		String[] list = value.split(",");
		for (int i=0 ; i < list.length ; i++) {
			list[i] = list[i].trim();
		}
		return list;
	}

	public static Integer getInteger(String key, Integer defaultValue) {
		return Integer.valueOf(get(key, String.valueOf(defaultValue)));
	}

	public static boolean isContainsComma(String key, String item) {
		return StringHelper.containsCommaList(get(key), item);
	}

	public static Properties getProperties() {
		return properties;
	}

}
