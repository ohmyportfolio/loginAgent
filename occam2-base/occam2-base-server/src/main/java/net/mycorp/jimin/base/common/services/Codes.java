package net.mycorp.jimin.base.common.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcResult;

@Service
public class Codes extends Bases {

	@Cacheable(value = "codes", key="#ctx.toString()")
	@Override
	public OcResult select(OcContext ctx) {
		return super.select(ctx);
	}
	
}
