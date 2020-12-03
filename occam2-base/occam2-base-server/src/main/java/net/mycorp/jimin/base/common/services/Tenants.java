package net.mycorp.jimin.base.common.services;

import org.jooq.SQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.auth.services.Users;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcDatasource;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.service.MetaService;

@Service
public class Tenants extends Bases {

	@Autowired
	private Datasources datasources;
	
	@Autowired
	private Resources resources;

	@Autowired
	private Users users;
	
	@Autowired
	private Events events;

	@Autowired
	private MetaService metas;

	@Override
	public void postInsert(OcContext ctx) {
		super.postInsert(ctx);
		createAdminUser(ctx);
		createDatabase(ctx);
	}
	
	@Override
	public void preDelete(OcContext ctx) {
		super.preDelete(ctx);
		events.delete(m("tenant_id", ctx.id()));
		resources.delete(m("tenant_id", ctx.id()));
		datasources.delete(m("tenant_id", ctx.id()));
		users.delete(m("tenant_id", ctx.id()));
		dsl().dropSchema(ctx.id()).execute();
	}

	public boolean notExist(OcContext ctx) {
		return get(ctx.skipPermit(true)) == null;
	}
	
	public void create(OcContext ctx) {
		super.insert(ctx.skipPermit(true));
	}
	
	private void createDatabase(OcContext ctx) {
		OcMap row = ctx.getRow();
		OcDatasource defaultDs = metas.getMeta().getDatasource("default");
		OcMap datasource = new OcMap(defaultDs);
		datasource.putAll(row.sub("id", "name"));
		datasource.put("tenant_id", datasource.id());
		
		SQLDialect dialect = dsl().configuration().dialect().family();
		if(dialect == SQLDialect.ORACLE) {
			dsl().execute("alter session set \"_ORACLE_SCRIPT\"=true");
			dsl().execute("create user "+datasource.id()+" identified by \""+row.get("password")+"\"");
			dsl().execute("grant connect, resource to "+datasource.id());
			// TODO 테이블스페이스 지정해야
			dsl().execute("alter user "+datasource.id()+" quota unlimited on users");
			datasource.put("username", datasource.id());
			datasource.put("password", row.get("password"));
		} else {
			dsl().createSchema(row.id()).execute();
			String url = defaultDs.getUrl();
			datasource.put("url", url.substring(0, url.lastIndexOf("/") + 1) + datasource.id());
			
		}
		datasources.insert(ctx().row(datasource).skipPermit(ctx.isSkipPermit()));
	}

	private void createAdminUser(OcContext ctx) {
		OcMap row = ctx.getRow();
		users.insert(ctx().row(m("user_id", row.get("admin_id"), "user_name", row.get("admin_id"), "password",
				row.get("password"), "user_type", "admin", "tenant_id", row.get("id"))).skipPermit(ctx.isSkipPermit()));
	}
}
