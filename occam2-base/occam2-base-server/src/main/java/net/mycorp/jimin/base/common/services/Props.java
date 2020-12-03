package net.mycorp.jimin.base.common.services;

import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.impl.SQLDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcColumn;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.domain.OcColumn.Relationship;
import net.mycorp.jimin.base.service.MetaService;
import net.mycorp.jimin.base.service.SqlService;

@Service
public class Props extends Bases {

	@Autowired
	private SqlService sqls;
	
	@Autowired
	private Views views;
	
	@Autowired
	private MetaService metas;
	
	@Override
	public void preUpdate(OcContext ctx) {
		super.preUpdate(ctx);
		OcMap row = ctx.getRow();
		OcMap old = getOld(ctx);
		OcMap prop = getComplete(ctx);
		boolean hasView = bindPropDetail(prop);
		if(hasView) {
			if(old != null) {
				if (row.get("prop_type") != null && !old.get("prop_type").equals(row.get("prop_type"))) {
	
					DSLContext dsl = sqls.dsl(prop.getString("datasource_id"));
					DataType<?> dataType = getDataType(row.getString("prop_type"));
					dsl.alterTable(prop.getString("table_name")).alterColumn(old.getString("prop_id")).set(dataType)
							.execute();
				}
				if (row.get("prop_id") != null && !old.get("prop_id").equals(row.get("prop_id"))) {
	
					DSLContext dsl = sqls.dsl(prop.getString("datasource_id"));
					dsl.alterTable(prop.getString("table_name")).renameColumn(old.getString("prop_id"))
							.to(row.getString("prop_id")).execute();
				}
			}
			updateOcColumn(prop);
		}
	}

	private void updateOcColumn(OcMap prop) {
		OcColumn ocColumn = toOcColumn(prop);
		OcResource ocResource = metas.getMeta().getResource(prop.getString("resource_id"));
		ocResource.setDbColumns(null);
		if(ocColumn.getName() != null) {
			ocResource.addColumn(ocColumn);
		}
	}
	
	public OcColumn toOcColumn(OcMap prop) {
		OcColumn ocColumn = new OcColumn();
		if (prop.getString("prop_type") != null && prop.getString("prop_type").startsWith("resource")) {
			ocColumn.setRelationship(Relationship.many_to_one);
			ocColumn.setResource(prop.getString("type_detail"));
			ocColumn.setFk(prop.getString("prop_id"));
			ocColumn.setName(prop.getString("prop_id") + "_t");
		} else if (prop.getString("sql") != null) {
			ocColumn.setSql(prop.getString("sql"));
			ocColumn.setName(prop.getString("prop_id"));
		}
		return ocColumn;
	}

	@Override
	public void preInsert(OcContext ctx) {
		super.preInsert(ctx);
		OcMap prop = ctx.getRow();
		if("id".equals(prop.get("prop_id")) || prop.getBool("_exist"))
			return;
		boolean hasView = bindPropDetail(prop);
		if(hasView) {
			DSLContext dsl = sqls.dsl(prop.getString("datasource_id"));
			DataType<?> dataType = getDataType(prop.getString("prop_type"));
			dsl.alterTable(prop.getString("table_name")).addColumn(prop.getString("prop_id"), dataType).execute();
			updateOcColumn(ctx.getRow());
		}
	}

	private boolean bindPropDetail(OcMap prop) {
		OcMap view = views.get(ctx().select("resource_id, ocresource__datasource_id, ocresource__table_name").condition("id", prop.getString("view_id")));
		if(view != null) {
			prop.put("datasource_id", view.get("ocresource__datasource_id"));
			prop.put("table_name", view.get("ocresource__table_name"));
			prop.put("resource_id", view.get("resource_id"));
			return true;
		}
		return false;
	}

	private DataType<?> getDataType(String propType) {
		DataType<?> dataType;
		if (propType.equals("integer") || propType.equals("float") || propType.startsWith("fileSize")
				|| propType.startsWith("percent")) {
			dataType = SQLDataType.NUMERIC;
		} else if (propType.startsWith("code") || propType.startsWith("resource")) {
			dataType = SQLDataType.VARCHAR.length(50);
		} else if (propType.startsWith("date") || propType.startsWith("time")) {
			dataType = SQLDataType.DATE;
		} else if (propType.startsWith("list") || propType.startsWith("string")) {
			dataType = SQLDataType.VARCHAR(200);
		} else if (propType.startsWith("textArea")) {
			dataType = SQLDataType.VARCHAR(4000);
		} else if (propType.startsWith("checkBox")) {
			dataType = SQLDataType.BOOLEAN;
		} else {
			throw new OccamException("%s은 알 수 없는 속성 유형입니다.", propType);
		}
		return dataType;
	}
	
	public void postSave(OcContext ctx) {
		super.postSave(ctx);
		views.clearCache(ctx);
	}
	
	public void postDelete(OcContext ctx) {
		super.postDelete(ctx);
		views.clearCache(ctx);
	}
}
