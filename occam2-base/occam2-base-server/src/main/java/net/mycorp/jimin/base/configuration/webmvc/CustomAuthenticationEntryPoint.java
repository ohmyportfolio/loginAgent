package net.mycorp.jimin.base.configuration.webmvc;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private String loginPageUrl;

    private boolean returnParameterEnabled;

    private String returnParameterName;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException reason) throws IOException, ServletException {

        if(null == loginPageUrl || "".equals(loginPageUrl))
            throw new RuntimeException("loginPageUrl has not been defined");

        String redirectUrl = loginPageUrl;

        if(isReturnParameterEnabled()) {
            String redirectUrlReturnParameterName = getReturnParameterName();
            if(null == redirectUrlReturnParameterName || "".equals(redirectUrlReturnParameterName))
                throw new RuntimeException("redirectUrlReturnParameterName has not been defined");
            if(redirectUrl.indexOf("?") > -1) {
            	redirectUrl += "&" + redirectUrlReturnParameterName + "=" + request.getServletPath();
            } else {
            	redirectUrl += "?" + redirectUrlReturnParameterName + "=" + request.getServletPath();
            }
            Enumeration<String> paramNames = request.getParameterNames();
            String fullParameter = "";
            boolean check = false;
            while(paramNames.hasMoreElements()) {
                check = true;
                if(!fullParameter.equals("")) {
                    fullParameter += "&";
                }
                String paramName = paramNames.nextElement();
                String paramValue = request.getParameter(paramName);
                fullParameter = fullParameter + paramName + "=" + paramValue;
            }
            if(check)
                redirectUrl = redirectUrl + "?" + fullParameter;
        }
        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        redirectStrategy.sendRedirect(request, response, redirectUrl);
        return;
    }

    public String getLoginPageUrl() {
        return loginPageUrl;
    }

    public void setLoginPageUrl(String loginPageUrl) {
        this.loginPageUrl = loginPageUrl;
    }

    public boolean isReturnParameterEnabled() {
        return returnParameterEnabled;
    }

    public void setReturnParameterEnabled(boolean returnParameterEnabled) {
        this.returnParameterEnabled = returnParameterEnabled;
    }

    public String getReturnParameterName() {
        return returnParameterName;
    }

    public void setReturnParameterName(String returnParameterName) {
        this.returnParameterName = returnParameterName;
    }

}
