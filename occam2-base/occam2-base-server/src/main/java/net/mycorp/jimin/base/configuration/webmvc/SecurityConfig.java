package net.mycorp.jimin.base.configuration.webmvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

import net.mycorp.jimin.base.auth.services.AuthService;
import net.mycorp.jimin.base.core.Configs;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthService auths;

	@Autowired
	private AuthenticationEntryPoint authenticationEntryPoint;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		String[] excludePaths = Configs.getArray("security.excludePaths");
		web.ignoring().antMatchers(excludePaths);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		String[] permitPaths = Configs.getArray("security.permitPaths");
		http.csrf().disable();
		http.headers().frameOptions().sameOrigin();
		http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
		http.authorizeRequests().antMatchers(permitPaths).permitAll().anyRequest().fullyAuthenticated();
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(auths).passwordEncoder(passwordEncoder);
	}

}
