package net.mycorp.jimin.base.auth.services;

import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.mycorp.jimin.base.common.services.Bases;
import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;

public class Users extends Bases {

	@Autowired
	protected GroupMembers groupMembers;

	@Autowired
	protected PasswordEncoder passwordEncoder;

	@Autowired
	protected AuthService auths;
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	public void preSave(OcContext ctx) {
		OcMap row = ctx.getRow();
		if (row.get("password") != null) {
			row.put("encrypted_password", passwordEncoder.encode(row.getString("password")));
			row.put("password_expiration_date", new Timestamp(DateTime.now().plusMonths(
					Configs.getInteger("login.passwordExpirationMonth", 3)).getMillis()));
		}
		if(row.get("user_name") != null || row.get("group_id") != null) {
			String groupId = row.get("group_id") != null ? row.getString("group_id") : getComplete(ctx).getString("group_id");
			String groupName = (String) select(select("name").resource("groups").condition(m("id", groupId))).getValue();
			String userName = row.get("user_name") != null ? row.getString("user_name") : getComplete(ctx).getString("user_name"); 
			row.put("name", userName + " / " + groupName);
		}
		super.preSave(ctx);
	}

	@Override
	public void postSave(OcContext ctx) {
		OcMap row = ctx.getRow();
		if (row.get("group_ids") != null) {
			groupMembers.deleteByUserId(m("user_id", ctx.id()));
			for (String group_id : row.getStrings("group_ids"))
				groupMembers.insert(m("user_id", ctx.id(), "group_id", group_id));
		}
		super.postSave(ctx);
	}
	
	public String updatePassword(OcContext ctx) {
		ctx.noLogRow(true);
		OcMap row = ctx.getRow();
		OcMap user = get(auths.getUser().getId());
		if(user.getString("encrypted_password") != null && 
				!passwordEncoder.matches(row.getString("old_password"), user.getString("encrypted_password")))
			throw new OccamException("기존 비밀번호가 일치하지 않습니다.");
		update(ctx().id(user.id()).row(row).skipPermit(true));
		return user.id();
	}
}
