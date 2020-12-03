package net.mycorp.jimin.base.domain;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class OcNamed {

	@XStreamAsAttribute
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
