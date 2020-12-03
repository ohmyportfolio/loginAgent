package net.mycorp.jimin.base.common.services;

import static org.jooq.impl.DSL.constraint;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.jooq.DSLContext;
import org.jooq.impl.SQLDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.DbColumn;
import net.mycorp.jimin.base.domain.OcColumn;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.domain.OcResult;
import net.mycorp.jimin.base.service.MetaService;
import net.mycorp.jimin.base.service.SqlService;

@Service
public class Resources extends Bases {

	@Autowired
	private MetaService metas;

	@Autowired
	private SqlService sqls;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private Props props;
	
	@Autowired
	private Views views;

	@Override
	public void preSave(OcContext ctx) {
		super.preSave(ctx);
		OcMap row = ctx.getRow();
		String tableName = row.getString("table_name");
		if(tableName != null) {
			OcResource resource = getOcResource(getComplete(ctx));
			if(resource.getPk() == null) {
				List<String> pks = sqls.getPks(resource);
				row.put("pk", StringUtils.join(pks, ","));
			}
		}
	}
	
	@Override
	public void postSave(OcContext ctx) {
		super.postSave(ctx);
		
		OcMap row = ctx.getRow();
		OcMap view = row.sub("name");
		view.put("id", ctx.id());
		view.put("resource_id", ctx.id());
		if(row.containsKey("ocview__menu_id"))
			view.put("menu_id", row.get("ocview__menu_id"));
		if(row.containsKey("ocview__enabled_functions"))
			view.put("enabled_functions", row.get("ocview__enabled_functions"));
		if(row.containsKey("ocview__options"))
			view.put("options", row.get("ocview__options"));
		if(views.update(ctx.id(), view) == 0)
			views.insert(view);

		loadPropsFromDb(ctx);
		loadOne(getComplete(ctx));
	}

	@Override
	public void preDelete(OcContext ctx) {
		super.preDelete(ctx);
		views.deleteEach(m("resource_id", ctx.id()));
	}

	public void loadAll() {
		for (OcMap resource : select(ctx().skipPermit(true)).getData()) {
			loadOne(resource);
		}
	}

	private OcResource loadOne(OcMap resource) {
		OcResource ocResource = getOcResource(resource);
		ocResource.setFromDb(true);
		List<OcMap> propList = props.select(ctx().condition("view_id", resource.id()).skipPermit(true)).getData();
		for (OcMap prop : propList) {
			OcColumn ocColumn = props.toOcColumn(prop);
			if(ocColumn.getName() != null)
				ocResource.addColumn(ocColumn);
		}
		
		metas.getMeta().addResource(ocResource);

		if (!applicationContext.containsBean(resource.id())) {
			Customs customs = (Customs) applicationContext.getBean("customs");
			customs.setResourceName(resource.id());
			ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext)
					.getBeanFactory();
			beanFactory.registerSingleton(resource.id(), customs);
		}

		return ocResource;
	}

	private OcResource getOcResource(OcMap resource) {
		resource.put("name", resource.id());
		resource.put("datasource", resource.get("datasource_id"));
		resource.put("table", resource.get("table_name"));
		OcResource ocResource = resource.toBean(OcResource.class);
		return ocResource;
	}

	public void loadPropsFromDb(OcContext ctx) {
		OcResource resource = getOcResource(getOld(ctx));
		List<OcMap> propList = props.select(ctx().select("prop_id").condition("view_id", ctx.id())).getData();
		Set<DbColumn> dbColumns;
		try {
			dbColumns = sqls.getDbColumns(resource);
		} catch (Exception e) {
			DSLContext dsl = sqls.dsl(resource.getDatasource());
			dsl.createTable(resource.getTable()).column("id", SQLDataType.VARCHAR.length(50))
					.column("name", SQLDataType.VARCHAR.length(255))
					.constraints(constraint("PK_" + resource.getTable()).primaryKey("id")).execute();
			dbColumns = sqls.getDbColumns(resource);
		}
		
		long seq = 1;
		if (propList.size() > 0) {
			Object maxSeq = props.select(ctx().select("max(seq)").condition("view_id", ctx.id())).getValue();
			seq = ((Number) maxSeq).longValue();
			seq++;
		}

		for (DbColumn dbColumn : dbColumns) {
			boolean exist = false;
			for (OcMap prop : propList) {
				if (dbColumn.getColumnName().equals(prop.get("prop_id"))) {
					exist = true;
					break;
				}
			}
			if (!exist) {
				String propType = "string";
				if ("java.math.BigDecimal".equals(dbColumn.getColumnClassName())) {
					propType = "float";
				} else if ("java.lang.Long".equals(dbColumn.getColumnClassName())) {
					propType = "integer";
				} else if ("java.sql.Timestamp".equals(dbColumn.getColumnClassName())) {
					propType = "datetime";
				}

				String name = WordUtils.capitalize(dbColumn.getColumnName().replace("_", " "));
				props.insert(m("view_id", ctx.id(), "prop_id", dbColumn.getColumnName(), "name", name, "prop_type",
						propType, "seq", seq++, "use_yn", false, "width", 160, "_exist", true));
			}
		}
	}
	
	@Override
	public OcResult select(OcContext ctx) {
		OcResult result = super.select(ctx);
		/*
		for (OcResource ocResource : metas.getMeta().getResources().values()) {
			if (ocResource.isFromDb())
				continue;
			OcMap resource = new OcMap();
			resource.put("id", ocResource.getName());
			resource.put("name", ocResource.getName());
			resource.put("fromXml", true);
			result.getData().add(resource);
			result.setTotal(result.getTotal() + 1);
		}
		*/
		return result;
	}
}
