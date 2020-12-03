package net.mycorp.jimin.base.auth.services;

import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.common.services.Bases;
import net.mycorp.jimin.base.domain.OcMap;

@Service
public class GroupMembers extends Bases {

	public Object deleteByUserId(OcMap ctx) {
		return executeXml(ctx);
	}
}
