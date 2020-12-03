package net.mycorp.jimin.base.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.core.OccamException;

@JsonIgnoreProperties({ "datasources", "resources" })
public class OcMeta extends OcMap {
	
	private static final long serialVersionUID = -4804420569920760522L;
	
	{
		put("config", new OcConfig());
		put("datasources", new LinkedHashMap<>());
		put("resources", new LinkedHashMap<>());
	}
	
	public OcConfig getConfig() {
		return (OcConfig) get("config");
	}

	@SuppressWarnings("unchecked")
	public Map<String, OcDatasource> getDatasources() {
		return (Map<String, OcDatasource>) get("datasources");
	}

	@SuppressWarnings("unchecked")
	public Map<String, OcResource> getResources() {
		return (Map<String, OcResource>) get("resources");
	}
	
	public OcDatasource getDatasource(String name) {
		if (name == null)
			name = Configs.get("sql.datasource", "default");
		return getDatasources().get(name);
	}

	public OcResource getResource(String name, boolean allowNull) {
		OcResource resource = getResources().get(name);
		if(!allowNull && resource == null)
			throw new OccamException("resource %s not found!", name);
		return resource;
	}

	public OcResource getResource(String name) {
		return getResource(name, false);
	}

	public void add(OcLoad load) {
		addConfig(load.getConfig());
		if (load.getDatasources() != null)
			for (OcDatasource datasource : load.getDatasources()) {
				addDatasource(datasource);
			}
		if (load.getResources() != null)
			for (OcResource resource : load.getResources()) {
				addResource(resource);
			}
	}

	public void init() {
		for (OcResource resource : getResources().values()) {
			resource.init(this);
		}
	}

	public void addConfig(OcConfig config) {
		if (config == null)
			return;
		try {
			BeanUtils.copyProperties(getConfig(), config);
		} catch (Exception e) {
			throw new OccamException(e);
		}
	}

	public void addDatasource(OcDatasource datasource) {
		if (datasource.getName() == null)
			throw new OccamException("Null datasource name not allowed");
		getDatasources().put(datasource.getName(), datasource);
	}

	public void addResource(OcResource resource) {
		if (resource.getName() == null)
			throw new OccamException("Null resource name not allowed");
		resource.init(this);
		getResources().put(resource.getName(), resource);
	}

}
