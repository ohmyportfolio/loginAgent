package net.mycorp.jimin.base.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.common.services.Bases;
import net.mycorp.jimin.base.domain.OcMap;

@Service
public class Acls extends Bases {

	@Autowired
	private AclTypes aclTypes;
	
	@Cacheable(value = "acls")
	public OcMap getAce(String aclId, String resourceName) {
		OcMap ace = aclTypes.get(ctx().condition("acl_id", aclId, "type_id", resourceName).skipPermit(true));
		if (ace == null)
			ace = get(aclId, true);
		return ace;
	}
}
