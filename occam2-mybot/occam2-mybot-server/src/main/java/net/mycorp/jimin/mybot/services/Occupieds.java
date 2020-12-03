package net.mycorp.jimin.mybot.services;

import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.service.BaseService;
import net.mycorp.jimin.base.util.Helper;

@Service
public class Occupieds extends BaseService{
	
	
	@Override
	public void preInsert(OcContext ctx) {
		super.preInsert(ctx);
		for(OcMap row : ctx.getRows()) {
			row.put("id", Helper.getUuid());
			row.put("start_date",new Date());
		}
	}
	
	
	public void useSite(OcContext ctx) {
		Map<String, Object> useInfo = ctx.getRow();
		String ip = (String) useInfo.get("ip");
	
		//{ip=192.168.219.103, netflix=true, uflix=false, tving=false, wavve=false}
		
		OcMap userParams = new OcMap();
		userParams.add("pc_ip", ip);

		delete(userParams);
		
	}
}
