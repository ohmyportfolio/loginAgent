package net.mycorp.jimin.base.auth.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.common.services.Bases;
import net.mycorp.jimin.base.domain.OcContext;

@Service
public class AclTypes extends Bases {
	
	@Override
    @CacheEvict(value="permItem")
	public void preSave(OcContext ctx) {
		super.preSave(ctx);
	}
	
}
