package net.mycorp.jimin.base.common.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.service.SqlService;

@Service
public class Views extends Bases {
	
	@Autowired
	private Props props;

	@Autowired
	private SqlService sqls;
	
	@Override
	public void preDelete(OcContext ctx) {
		super.preDelete(ctx);
		props.delete(m("view_id", ctx.id()));
	}
	
	@Override
	public void preInsert(OcContext ctx) {
		super.preInsert(ctx);
		OcMap row = ctx.getRow();
		
		if(row.id() == null && row.get("resource_id") != null) {
			OcMap source = get(row.getString("resource_id"));
			source.remove("id");
			row.putAllIfNull(source);
		}
		if(row.get("source_view_id") != null) {
			OcMap source = get(row.getString("source_view_id"));
			source.remove("id");
			source.remove("menu_id");
			row.putAllIfNull(source);
		}
	}
	
	public void postInsert(OcContext ctx) {
		super.postInsert(ctx);
		OcMap row = ctx.getRow();
		
		if(!row.id().equals(row.get("resource_id"))) {
			List<OcMap> propList = props.select(ctx().condition("view_id", row.get("resource_id"))).getData();
			for (OcMap prop : propList) {
				prop.put("view_id", row.id());
			}
			sqls.insert(meta.getResource("props"), propList);
		}
	}
	
	public void saveProps(OcContext ctx) {
		sqls.update(meta.getResource("props"), m("view_id", ctx.id()), m("use_yn", false));
		for(OcMap row : ctx.getRows()) {
			props.update(null, row);
		}
	}
}
