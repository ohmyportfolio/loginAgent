package net.mycorp.jimin.base.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("datasource")
public class OcDatasource extends OcNamed {

	@XStreamAsAttribute
	private String driver;

	@XStreamAsAttribute
	private String url;

	@XStreamAsAttribute
	private String database;

	@XStreamAsAttribute
	private String dialect;

	@XStreamAsAttribute
	private String username;

	@XStreamAsAttribute
	private String password;

	@XStreamAsAttribute
	private String initSql;

	private boolean fromDb;

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getInitSql() {
		return initSql;
	}

	public void setInitSql(String initSql) {
		this.initSql = initSql;
	}

	public boolean isFromDb() {
		return fromDb;
	}

	public void setFromDb(boolean fromDb) {
		this.fromDb = fromDb;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public boolean isSql() {
		return !"MONGO".equalsIgnoreCase(dialect);
	}

}
