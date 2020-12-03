package net.mycorp.jimin.mybot.auth.controller;

import static net.mycorp.jimin.base.core.Global.ctx;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mycorp.jimin.base.auth.OcUser;
import net.mycorp.jimin.base.auth.services.Users;
import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcMap;

@Controller
@Transactional
@RequestMapping(value = "api")
public class AuthController extends net.mycorp.jimin.base.auth.controllers.AuthController {
	
	@Autowired
	private Users users;
	
	@RequestMapping(value = "password/init", method = RequestMethod.POST)
	@ResponseBody
	public OcMap passwordInit(@RequestBody Map<String, String> body) {
		OcMap result = new OcMap();
		Map<String, Object> mailParams = new HashMap<String, Object>();
		List<OcMap> userList = users.select(ctx().condition("user_id", body.get("id"), "email", body.get("email"))).getData();
		if(userList.size() != 1)
			throw new OccamException("UNKNOWN_USER");
		OcMap selectUser = userList.get(0);
		
		OcUser user = (OcUser) users.get((String) selectUser.get("id"));
		if(user != null){
			String secureKey =  UUID.randomUUID().toString();
			user.put("secure_key", secureKey);
			user.put("secure_key_expiration_date",new Timestamp(DateTime.now().plusMinutes(30).getMillis()));
			mailParams.put("from",user.get("email"));
			mailParams.put("to",user.get("email"));
			mailParams.put("subject",user.get("패스워드 초기화"));
			mailParams.put("template","Password_Init_Template");
			mailParams.put("secure_key",secureKey);
			users.update(user.getId(), user);
			try {
				//intfMail.sendMail(mailParams);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		result.put("message", "비밀번호 초가화 메일이 발송되었습니다.");
		result.put("success", true);
		return result;
	}
	@RequestMapping(value = "password/init/{secureKey}", method = RequestMethod.POST)
	@ResponseBody
	public OcMap passwordInit(@RequestBody Map<String, String> body, @PathVariable String secureKey) {
		OcMap result = new OcMap();
		List<OcMap> userList = users.select(ctx().condition("secure_key", secureKey)).getData();
		if(userList.size() != 1)
			throw new OccamException("INVAILD_SECURE_KEY");
		OcMap selectUser = userList.get(0);
		OcUser user = (OcUser) users.get((String) selectUser.get("id"));
		
		if(!secureKey.equals(user.get("secure_key")))
			throw new OccamException("INVAILD_SECURE_KEY");
		if(user != null){
			if(user.getId().equals(body.get("password")))
				throw new OccamException("INVAILD_PASSWORD");
			
			user.put("password", body.get("password"));
			user.put("secure_key", null);
			user.put("secure_key_expiration_date",null);
			users.update(user.getId(), user);
			result.put("message", "비밀번호가 성공적으로 변경되었습니다.");
			result.put("success", true);
		}
		return result;
	}
}
