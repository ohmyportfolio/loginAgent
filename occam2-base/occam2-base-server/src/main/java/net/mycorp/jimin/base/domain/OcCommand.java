package net.mycorp.jimin.base.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("command")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = { "text" })
public class OcCommand extends OcNamed implements OcDatasourced {

	public enum Type {
		sql, script
	}

	public enum Result {
		object, // Map or Bean (Form 화면용)
		list, 	// List<Map>
		result, // OcResult (Paging 화면용)
		value, 	// Primitive Type (String, Integer...)
		values	// List<Primitive Type>
	}

	@XStreamAsAttribute
	private String datasource;

	@XStreamAsAttribute
	private Type type;

	@XStreamAsAttribute
	private Result result;
	
	@XStreamAsAttribute
	private Boolean noParameterize;
	
	private String text;

	public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Boolean getNoParameterize() {
		return noParameterize;
	}

	public void setNoParameterize(Boolean noParameterize) {
		this.noParameterize = noParameterize;
	}
}
