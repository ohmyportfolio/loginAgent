package net.mycorp.jimin.base.domain;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("occam")
public class OcLoad {

	private OcConfig config = new OcConfig();
	
	private List<OcDatasource> datasources = new ArrayList<>();

	private List<OcResource> resources = new ArrayList<>();

	private List<OcMessages> messages = new ArrayList<>();
	
	public List<OcDatasource> getDatasources() {
		return datasources;
	}

	public void setDatasources(List<OcDatasource> datasources) {
		this.datasources = datasources;
	}

	public List<OcResource> getResources() {
		return resources;
	}

	public void setResources(List<OcResource> resources) {
		this.resources = resources;
	}

	public OcConfig getConfig() {
		return config;
	}

	public void setConfig(OcConfig config) {
		this.config = config;
	}

	public List<OcMessages> getMessages() {
		return messages;
	}

	public void setMessages(List<OcMessages> messages) {
		this.messages = messages;
	}

}
