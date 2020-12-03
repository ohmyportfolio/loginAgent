package net.mycorp.jimin.mybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@EnableCaching
@SpringBootApplication(exclude = { MongoAutoConfiguration.class, MongoReactiveAutoConfiguration.class })
@ComponentScan(basePackages = { "net.mycorp.jimin.base.*", "net.mycorp.jimin.mybot.*" }, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.mycorp.jimin.base.configuration.webflux.*") })
@Import({ net.mycorp.jimin.base.core.Initializer.class })
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(Application.class);
	}

}
