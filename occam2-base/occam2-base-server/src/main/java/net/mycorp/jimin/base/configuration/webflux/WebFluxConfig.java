package net.mycorp.jimin.base.configuration.webflux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.mycorp.jimin.base.auth.services.AuthService;
import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.misc.ScriptObjectMirrorSerializer;
import reactor.core.publisher.Mono;

@SuppressWarnings("restriction")
@Configuration
public class WebFluxConfig {

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		JavaTimeModule module = new JavaTimeModule();
		module.addSerializer(ScriptObjectMirror.class, new ScriptObjectMirrorSerializer());
		mapper.registerModule(module);
		return mapper;
	}

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		String[] permitPaths = Configs.getArray("security.permitPaths");
		String[] excludePaths = Configs.getArray("security.excludePaths");
		
        http.csrf().disable();
        
		http.authorizeExchange().pathMatchers(permitPaths).permitAll();
		http.authorizeExchange().pathMatchers(excludePaths).permitAll();
		http.authorizeExchange().anyExchange().authenticated();
		http.formLogin();
		
		return http.build();
	}
	
	@Bean
	public ReactiveUserDetailsService userDetailsService() {
		return new ReactiveUserDetailsService() {

			@Autowired
			private AuthService auths;

			@Override
			public Mono<UserDetails> findByUsername(String username) {
				return Mono.just(auths.loadUserByUsername(username));
			}

		};
	}

}