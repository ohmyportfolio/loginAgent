package net.mycorp.jimin.mybot.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResult;
import net.mycorp.jimin.base.service.BaseService;
import net.mycorp.jimin.mybot.util.AppHelper;

@Service
public class Sites extends BaseService{
	
	@Autowired
	private Occupieds occupids;
	
	@Autowired
	private Accounts accounts;
	
	@Autowired
	private Pcs pcs;
	
	private static Logger log = LoggerFactory.getLogger(Sites.class);
	
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
	
	@SuppressWarnings("unchecked")
	public OcMap selectAvailableAccountSeq(OcContext ctx) throws Exception {
		OcMap result = super.get(ctx);
		
		OcMap registeredPc = pcs.get(condition("ip" , ctx.getString("pc_ip")));
		if(registeredPc == null) {
			log.info("denied IP : " + ctx.getString("pc_ip"));
			return null;
		}
		
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
		
		Long minCount = result.getLong("allow_login_count");
		
		for (OcMap row : (List<OcMap>) result.get("accounts")) {
			Long count = occupids.count(condition("site_id" , ctx.getId(),"user_id" , row.get("user_id")));
			if(count < minCount) {
				result.put("user_id", row.get("user_id"));
				result.put("user_password", row.get("user_password"));
				result.put("account_id" , row.get("id"));
				result.put("saveOccupied" , "true");
				minCount = count;
			}
		}
		
		// need encrypt password 
		result.remove("accounts");
		//계정이 모두 사용중이면 result.get("user_password") 이 null 인경우가 있음 null 처리 해줘야함
		result.put("user_password", AppHelper.Encrypt((String) result.get("user_password"), AppHelper.M_K));
		return result;
	}
}
