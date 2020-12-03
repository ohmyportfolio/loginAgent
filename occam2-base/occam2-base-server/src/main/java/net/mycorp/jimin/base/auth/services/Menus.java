package net.mycorp.jimin.base.auth.services;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.auth.OcUser;
import net.mycorp.jimin.base.common.services.Bases;
import net.mycorp.jimin.base.common.services.Views;
import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.util.Helper;

@Service
public class Menus extends Bases {
	
	@Autowired
	private MenuGroups menuGroups;
	
	@Autowired
	private AuthService auths;
	
	@Autowired
	private Views views;
	
	@Override
	public void preDelete(OcContext ctx) {
		super.preDelete(ctx);
		menuGroups.delete(m("menu_id", ctx.id()));
	}
	
	public List<Map<String, Object>> tree(OcContext ctx) {
		List<OcMap> menus;
		OcUser user = auths.getUser();
		String userTenant = user.getString("tenant_id");
		
		OcMap condition = ctx.getCondition();
		condition.add("use_yn", "y", "id <>", "root");
		
		OcMap tenantCondition = new OcMap();
		if(userTenant != null) {
			tenantCondition.add("or", m("tenant_id$!", "common", "tenant_id$2", userTenant));
			condition.addAll(tenantCondition);
		}
		
		if(user != null && "admin".equals(user.get("user_type"))) {
			menus = super.select(ctx.condition(condition)).getData();
		} else {
			List<String> groupIds = auths.getUser().getGroupIds();
			List<String> menuIds = menuGroups.select(
					select("menu_id").condition("group_id", groupIds).skipPermit(true)).getValues(String.class);
			menus = super.select(ctx().condition(condition.add("id", menuIds)).orderby("seq")).getData();
		}
		addCustoms(menus, tenantCondition);
		return Helper.sortHierarchicalList(menus,"parent_id","id");
	}
	
	private void addCustoms(List<OcMap> menus, OcMap condition) {
		if (!Configs.getBoolean("base.loadMetaDb"))
			return;
		List<OcMap> viewMenus = views.select(ctx().select("id,name,menu_id").condition(condition.add("menu_id !=", null))).getData();
		for (OcMap viewMenu : viewMenus) {
			if(!viewMenu.getBool("fromXml")) {
				viewMenu.put("parent_id", viewMenu.getString("menu_id", "root"));
				viewMenu.put("path", "/" + viewMenu.getString("id"));
				viewMenu.put("seq", new Date().getTime());
				menus.add(viewMenu);
			}
		}
	}

	@Override
	public void postSave(OcContext ctx) {
		OcMap row = ctx.getRow();
		if (row.get("group_ids") != null) {
			menuGroups.delete(m("menu_id", ctx.id()));
			for (String group_id : row.getStrings("group_ids"))
				menuGroups.insert(m("menu_id", ctx.id(), "group_id", group_id));
		}
		super.postSave(ctx);
	}
	
}
