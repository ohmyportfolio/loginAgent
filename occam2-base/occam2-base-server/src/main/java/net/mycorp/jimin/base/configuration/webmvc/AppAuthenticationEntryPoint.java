package net.mycorp.jimin.base.configuration.webmvc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.DispatcherServlet;

import net.mycorp.jimin.base.auth.OcUser;
import net.mycorp.jimin.base.auth.services.AuthService;

@Component
public class AppAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Autowired
	private AuthService auths;

	@Autowired
	private DispatcherServlet dispatcherServlet;
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
		OcUser user = auths.getUser();
		if(user == null) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
		} else {
			dispatcherServlet.service(request, response);
		}
	}

}
