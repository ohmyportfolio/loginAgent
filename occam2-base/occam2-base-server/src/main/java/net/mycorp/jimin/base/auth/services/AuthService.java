package net.mycorp.jimin.base.auth.services;

import static net.mycorp.jimin.base.core.Global.ctx;
import static net.mycorp.jimin.base.core.Global.m;
import static net.mycorp.jimin.base.core.Global.p;
import static net.mycorp.jimin.base.core.Global.select;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.auth.OcUser;
import net.mycorp.jimin.base.common.services.Events;
import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.util.Helper;
import net.mycorp.jimin.base.util.ServletHelper;

@Service
public class AuthService implements org.springframework.security.core.userdetails.UserDetailsService {

	private static final String SESSION_ID = "session_id";
	
	@Autowired
	private Users users;

	@Autowired
	protected Events events;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		OcUser user = getUser(username);
		if (user == null)
			throw new UsernameNotFoundException("Username not found. " + username);
		return user;
	}

	private OcUser getUser(String username) {
		String[] usernames = username.split("\\|");
		Map<String, Object> condition;
		if(usernames.length == 2) {
			condition = m(p("tenant_id", usernames[0]), p("user_id", usernames[1]));
		} else {
			condition = m(p("user_id", username));
		}
		return (OcUser) users.get(ctx().select("*,group_ids,all_group_ids").condition(condition).skipPermit(true));
	}

	public OcUser login(Authentication auth) {
		SecurityContextHolder.getContext().setAuthentication(auth);
		OcUser user = getUser();
		if (user != null) {
			try {
				if (user.get("user_status") != null && !user.get("user_status").equals("normal"))
					throw new OccamException("차단된 사용자입니다.");
				if (user.get("blocked_date") != null && user.getDateTime("blocked_date").isBeforeNow())
					throw new OccamException("차단된 사용자입니다.");
				if (user.get("expiration_date") != null && user.getDateTime("expiration_date").isBeforeNow())
					throw new OccamException("사용기간이 만료되었습니다.");
				if (user.get("password_expiration_date") != null && user.getDateTime("password_expiration_date").isBeforeNow())
					throw new OccamException("비밀번호 유효기간이 만료되었습니다.");
				if (user.get("login_failure_count") != null &&
						user.getInt("login_failure_count") > Configs.getInteger("login.maxFailureCount", 5))
					throw new OccamException("로그인 시도 횟수를 초과하였습니다.");
			} catch (OccamException e) {
				logout();
				throw e;
			}
			events.logEvent("users", "login");
		}
		String sessionId = getSessionId(true);
		user.put(SESSION_ID, sessionId);
		ServletHelper.addCookie(SESSION_ID, sessionId);
		users.update(ctx().id(user.getId()).row(m(SESSION_ID, sessionId, "last_login_date", new Date(), "login_failure_count", 0)).skipPermit(true));
		return user;
	}

	public OcUser getUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			return loginBySessionId();
		Object principal = auth.getPrincipal();
		if (principal instanceof OcUser) {
			return (OcUser) principal;
		} else {
			return loginBySessionId();
		}
	}

	private OcUser loginBySessionId() {
		String sessionId = getSessionId(false);
		if (sessionId != null) {
			OcUser user = (OcUser) users.get(select("id,user_id").condition(m(p(SESSION_ID, sessionId))).skipPermit(true));
			if (user != null) {
				return loginById(user.getString("user_id"));
			}
		}
		return null;
	}

	private String getSessionId(boolean create) {
		try {
			String sessionId = ServletHelper.getCookieMap().get(SESSION_ID);
			if(create && sessionId == null)
				sessionId = Helper.getRand16c();
			return sessionId;
		}  catch (IllegalStateException e) {
			return Helper.getRand16c();
		}
	}

	public OcUser loginById(String username) {
		OcUser user = getUser(username);
		if (user == null)
			throw new OccamException("존재하지 않는 사용자입니다.");
		return login(new UsernamePasswordAuthenticationToken(user, null, null));
	}
	
	public void loginFailure(String username) {
		OcUser user = (OcUser) getUser(username);
		if (user != null) {
			users.update(ctx().id(user.getId()).row(m(p("login_failure_count",
					user.getInt("login_failure_count") + 1))).skipPermit(true));
		}
	}

	public void logout() {
		SecurityContextHolder.getContext().setAuthentication(null);
		ServletHelper.getRequest().getSession().invalidate();
		ServletHelper.removeCookie(SESSION_ID);
	}

}
