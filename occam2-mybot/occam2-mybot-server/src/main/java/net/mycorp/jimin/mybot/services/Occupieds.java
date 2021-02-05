package net.mycorp.jimin.mybot.services;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.service.BaseService;
import net.mycorp.jimin.base.util.Helper;

@Service
public class Occupieds extends BaseService{
	
	@Autowired
	private Pcs pcs;
	
	@Autowired
	private Denieds denieds;
	
	private static Logger log = LoggerFactory.getLogger(Sites.class);
	
	@Override
	public void preInsert(OcContext ctx) {
		super.preInsert(ctx);
		for(OcMap row : ctx.getRows()) {
			row.put("id", Helper.getUuid());
			row.put("start_date",new Date());
		}
	}
	
	
	public String useSite(OcContext ctx) {
		Map<String, Object> useInfo = ctx.getRow();
		String ip = (String) useInfo.get("ip");
		
		useInfo.put("reg_date" , new Date());
	
		OcMap registeredPc = pcs.get(condition("ip" , ip));
		if(registeredPc == null) {
			log.info("denied IP : " + ip);
			denieds.insert(useInfo);
			return "false";
		}
		
		//{ip=192.168.219.103, netflix=true, uflix=false, tving=false, wavve=false}
		
		OcMap userParams = new OcMap();
		userParams.add("pc_ip", ip);

		delete(userParams);
		
		return "true";
		
	}
}
