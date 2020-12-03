package net.mycorp.jimin.mybot.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResult;
import net.mycorp.jimin.base.service.BaseService;

@Service
public class Sites extends BaseService{
	
	@Autowired
	private Occupieds occupids;
	
	@Autowired
	private Accounts accounts;
	
	@SuppressWarnings("unchecked")
	public OcMap selectAvailableAccount(OcContext ctx) {
		OcMap result = super.get(ctx);
		
		OcMap used = occupids.get(condition("site_id" , ctx.getId(),"pc_ip" , ctx.getString("pc_ip")));
		if(used != null) {
			OcMap account = accounts.get(used.getString("account_id"));
			if(account != null) {
				result.put("user_id", account.get("user_id"));
				result.put("user_password", account.get("user_password"));
				result.put("account_id" , account.get("id"));
				result.put("saveOccupied" , "false");
				return result;
			}
		}
		
		for (OcMap row : (List<OcMap>) result.get("accounts")) {
			Long count = occupids.count(condition("site_id" , ctx.getId(),"user_id" , row.get("user_id")));
			if(count < result.getLong("allow_login_count")) {
				result.put("user_id", row.get("user_id"));
				result.put("user_password", row.get("user_password"));
				result.put("account_id" , row.get("id"));
				result.put("saveOccupied" , "true");
				break;
			}
		}
		return result;
	}
	
}
