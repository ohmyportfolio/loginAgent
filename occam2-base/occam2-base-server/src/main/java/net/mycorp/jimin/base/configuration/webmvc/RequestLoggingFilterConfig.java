package net.mycorp.jimin.base.configuration.webmvc;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {

	private static final String START_MS = "_START_MS";

	@Bean
	public CommonsRequestLoggingFilter logFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter() {
			@Override
			protected void beforeRequest(HttpServletRequest request, String message) {
				request.setAttribute(START_MS, System.currentTimeMillis());
				logger.debug(message);
				logger.debug("query string: " + request.getQueryString());
				/*
				try {
					String body = IOUtils.toString(request.getReader()); 
					logger.debug("body: " + body);
				} catch (IOException e) {					
				}
				*/
			}

			@Override
			protected void afterRequest(HttpServletRequest request, String message) {
				long startMs = (long) request.getAttribute(START_MS);
				long elapsed = System.currentTimeMillis() - startMs;
				logger.debug(elapsed + " ms elapsed. "+message);				
			}
		};
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(100);
		return filter;
	}
}