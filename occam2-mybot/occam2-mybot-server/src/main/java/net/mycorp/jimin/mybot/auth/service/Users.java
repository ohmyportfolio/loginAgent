package net.mycorp.jimin.mybot.auth.service;

import static net.mycorp.jimin.base.core.Global.p;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;

@Service
public class Users extends net.mycorp.jimin.base.auth.services.Users {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public void preSave(OcContext ctx) {
		super.preSave(ctx);
		OcMap row = ctx.getRow();
		if(ctx.id() != null && row.get("user_name") != null) {
			OcMap user = this.get(select("user_name, position, ocgroup__name")
						.condition(m(p("id", ctx.id()))).skipPermit(true));
			String name = user.get("user_name") +"/"+ user.get("position") +"/"+ user.get("ocgroup__name");
			row.put("name", name.replace("null", ""));
		}
	}
	
	
	public OcMap passwordInit(OcContext ctx) {
		OcMap result = new OcMap();
		OcMap user = null;
		if(ctx.get("old_password") != null) {
			user = auths.getUser();
			if(!passwordEncoder.matches(ctx.getString("old_password"), user.getString("encrypted_password")))
				throw new OccamException("INVAILD_OLD_PASSWORD");
		} else {
			user = get(ctx.skipPermit(true));
			if(!ctx.get("secure_key").equals(user.get("secure_key")))
				throw new OccamException("INVAILD_SECURE_KEY");
		}
		
		if(user.id() == ctx.get("password")) {
			throw new OccamException("INVAILD_PASSWORD");
		}
		
		OcMap userParams = new OcMap();
		userParams.add("id", user.id());
		userParams.add("password", ctx.get("password"));
		userParams.add("mod_user_id", user.id());
		if(ctx.get("secure_key") != null) {			
			userParams.add("secure_key", null);
			userParams.add("secure_key_expiration_date", null);
		}
		ctx.setRow(userParams);
		ctx.setId(user.id());
		update(ctx.skipPermit(true));
		result.put("message", "비밀번호가 성공적으로 변경되었습니다.");
		result.put("success", true);
		return result;
	}
}
