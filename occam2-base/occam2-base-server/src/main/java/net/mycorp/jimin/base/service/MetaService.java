package net.mycorp.jimin.base.service;

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcConfig;
import net.mycorp.jimin.base.domain.OcDatasource;
import net.mycorp.jimin.base.domain.OcLoad;
import net.mycorp.jimin.base.domain.OcMeta;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.util.StringHelper;

@Service
public class MetaService {

	private static Logger log = LoggerFactory.getLogger(MetaService.class);

	private static final int MONITOR_INTERVAL = 1000;

	private XStream xstream = new XStream();

	private OcMeta meta = new OcMeta();

	@Autowired
	private SqlService sqls;

	@Autowired
	private ScriptService scripts;

	public void init() throws Exception {
		Reflections reflections = new Reflections(OcMeta.class.getPackage().getName(), new SubTypesScanner(false));
		Class<?>[] metaClasses = reflections.getSubTypesOf(Object.class).toArray(new Class[] {});

		XStream.setupDefaultSecurity(xstream);
		xstream.processAnnotations(metaClasses);
		xstream.allowTypes(metaClasses);
		
		File occamDir = new File(getClass().getResource("/jimin").getFile());
		File occambaseDir = new File(getClass().getResource("/jiminbase").getFile());

		// monitorDir(occambaseDir, MONITOR_INTERVA);
		monitorDir(occamDir, MONITOR_INTERVAL);

		loadDir(occambaseDir);
		loadDir(occamDir);

		File baseDir = new File(getClass().getResource("/net/mycorp/jimin/base").getFile());
		monitorDir(baseDir, MONITOR_INTERVAL * 5);
		loadDir(baseDir);
		
		String applicationPackage = Configs.get("base.applicationPackage");
		if(applicationPackage != null) {
			String applicationPath = "/" + applicationPackage.replace(".", "/");
			File applicationDir = new File(getClass().getResource(applicationPath).getFile());
			monitorDir(applicationDir, MONITOR_INTERVAL);
			loadDir(applicationDir);
		}
		
		meta.init();
		bindToScript();
		
		log.info("MetaService init.");
	}
	
	public OcMeta getMeta() {
		return meta;
	}
	
	private void loadDir(File dir) {
		for (final File file : dir.listFiles()) {
			if(!file.isDirectory())
				load(file);
		}
		for (final File file : dir.listFiles()) {
			if(file.isDirectory())
				loadDir(file);
		}		
	}

	private void monitorDir(File metaDir, long monitorInterval) throws Exception {
		FileAlterationObserver observer = new FileAlterationObserver(metaDir);
		FileAlterationMonitor monitor = new FileAlterationMonitor(monitorInterval);
		observer.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileChange(File file) {
				log.info("reload " + file.getName());
				load(file);
				
				meta.init();
				bindToScript();
			}
			
		    @Override
		    public void onFileCreate(final File file) {
		    	onFileChange(file);
		    }
		});
		monitor.addObserver(observer);
		monitor.start();
	}

	private void load(File file) {
		try {
			String extension = FilenameUtils.getExtension(file.getName());

			if (extension.equals("xml")) {
				Object loaded = xstream.fromXML(file);

				if (loaded instanceof OcLoad) {
					meta.add((OcLoad) loaded);
				} else if (loaded instanceof OcResource) {
					OcResource resource = (OcResource)loaded;
					if(resource.getName() == null)
						resource.setName(FilenameUtils.getBaseName(StringHelper.headToLowerCase(file.getName())));					
					meta.addResource(resource);
				} else if (loaded instanceof OcDatasource) {
					meta.addDatasource((OcDatasource) loaded);
				} else if (loaded instanceof OcConfig) {
					meta.addConfig((OcConfig) loaded);
				}
			} else {
				String groupName = StringUtils.substringBetween(file.getAbsolutePath(), "classes" + File.separator + "jimin" + File.separator, File.separator);				
				Map<String, Object> group = meta.getMap(groupName, true);
				Object result = null;
				
				if (extension.equals("js")) {
					result = scripts.eval(file);
				} else if (extension.equals("properties")) {
					Properties props = new Properties();
					props.load(new FileReader(file));
					result = props;
				} else {
					return;
					//result = FileUtils.readFileToString(file, "utf-8");
				}

				String fileName = file.getName();
				String nodeName;
				if(fileName.endsWith("vue"))
					nodeName = fileName;
				else
					nodeName = FilenameUtils.getBaseName(fileName);
				if(result != null)
					group.put(nodeName, result);
			}
		} catch (Exception e) {
			throw new OccamException(e, "Error whild load %s", file.getName());
		}
	}

	private void bindToScript() {
		for(OcResource resource : meta.getResources().values()) {
			scripts.eval("bindResource('"+resource.getName()+"')", null);
		}
	}
	
	public void createDb() {
		for (OcResource resource : meta.getResources().values()) {
			sqls.createDb(resource);
		}
	}

	public void loadData() {
		for (OcResource resource : meta.getResources().values()) {
			loadData(resource);
		}
	}

	@Autowired
    private CacheManager cacheManager;
	
	public void loadData(String resource) {
		loadData(meta.getResource(resource));
		Cache cache = cacheManager.getCache(resource);
		cache.clear();
	}
	
	private void loadData(OcResource resource) {
		for(String depend : resource.getDependList()) {
			loadData(meta.getResource(depend));
		}
		sqls.loadData(resource);
	}	
}
