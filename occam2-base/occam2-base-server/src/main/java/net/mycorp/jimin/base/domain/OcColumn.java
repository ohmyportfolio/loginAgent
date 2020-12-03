package net.mycorp.jimin.base.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("column")
public class OcColumn extends OcNamed {

	public enum Relationship {
		many_to_one, one_to_many, command, json
	}

	@XStreamAsAttribute
	private String dataType;

	@XStreamAsAttribute
	private Integer length;

	@XStreamAsAttribute
	private boolean nullable = true;

	@XStreamAsAttribute
	private Relationship relationship;

	@XStreamAsAttribute
	private String resource;

	@XStreamAsAttribute
	private String fk;
	
	@XStreamAsAttribute
	private String pk;
	
	@XStreamAsAttribute
	private String select;

	@XStreamAsAttribute
	private String where;

	@XStreamAsAttribute
	private String orderby;
	
	@XStreamAsAttribute
	private String sql;
	
	@XStreamAsAttribute
	private String joins;

	public Relationship getRelationship() {
		return relationship;
	}

	public void setRelationship(Relationship relationship) {
		this.relationship = relationship;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(String where) {
		this.where = where;
	}

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public String getSelect() {
		return select;
	}

	public void setSelect(String select) {
		this.select = select;
	}

	public String getFk() {
		return fk;
	}

	public void setFk(String fk) {
		this.fk = fk;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer size) {
		this.length = size;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getJoins() {
		return joins;
	}

	public void setJoins(String joins) {
		this.joins = joins;
	}

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
}
