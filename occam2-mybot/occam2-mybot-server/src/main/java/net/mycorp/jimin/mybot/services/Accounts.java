package net.mycorp.jimin.mybot.services;

import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResult;
import net.mycorp.jimin.base.service.BaseService;

@Service
public class Accounts extends BaseService{
	
	public OcResult select(OcContext ctx) {
		OcResult result = super.select(ctx);
		
		for(OcMap row : result.getData()) {
			row.put("user_password","******");
		}
		return result;
	}
}
