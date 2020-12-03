package net.mycorp.jimin.base.auth.controllers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mycorp.jimin.base.auth.OcUser;
import net.mycorp.jimin.base.auth.services.AuthService;
import net.mycorp.jimin.base.common.services.Events;
import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.util.ServletHelper;
import net.mycorp.jimin.base.util.StringHelper;

@Transactional
@RequestMapping(value = "api")
public class AuthController {

	protected static Logger log = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	protected AuthService auths;
	
	@Autowired
	protected Events events;
	
	@Autowired
	protected ApplicationContext context;
	
	@RequestMapping(value = "login", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> login(@RequestBody Map<String, String> body) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			OcUser user;
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(body.get("id"), body.get("password"));
			AuthenticationManager authenticationManager = context.getBean(AuthenticationManager.class);
			try {
				user = auths.login(authenticationManager.authenticate(token));
			} catch (DisabledException e) {
				throw new OccamException("차단된 사용자입니다.");
			} catch (IllegalArgumentException | BadCredentialsException e) {
				auths.loginFailure(body.get("id"));
				throw new OccamException("존재하지 않는 사용자이거나 비밀번호가 일치하지 않습니다.");
			}
			result.put("success", true);
			result.put("user", user);
		} catch (InternalAuthenticationServiceException e) {
			result.put("success", false);
			result.put("message", e.getMessage());
			log.error("Exception login: ");
			log.error(e.getMessage());
			log.error("Exception stacktrace: ", e);
		} catch (OccamException e) {
			result.put("success", false);
			result.put("message", e.getMessage());
		}
		return result;
	}

	@RequestMapping(value = "logout", method = RequestMethod.GET)
	@ResponseBody
	public void logout() {
		events.logEvent("users", "logout");
		auths.logout();
	}

	@RequestMapping(value = "loginForce", method = RequestMethod.GET)
	public void loginForce(@RequestParam String username) {
		if (!StringHelper.containsCommaList(Configs.get("security.adminIp"), ServletHelper.getRemoteAddr()))
			throw new OccamException("잘못된 접근입니다.");
		events.logEvent("users", "loginForce");
		auths.loginById(username);
	}

	@RequestMapping(value = "user", method = RequestMethod.GET)
	@ResponseBody
	public Object user() {
		// Mono<Principal> principal
		// if (principal != null)
		//	return principal.map(item -> ((UsernamePasswordAuthenticationToken) item).getPrincipal());
		return auths.getUser();
	}

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@ResponseBody
	@RequestMapping(value = "encodePassword", method = RequestMethod.GET)
	public String encodePassword(@RequestParam String password) {
		return passwordEncoder.encode(password);
	}
}
