package net.mycorp.jimin.mybot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import net.mycorp.jimin.base.auth.OcUser;
import net.mycorp.jimin.base.auth.services.AuthService;
import net.mycorp.jimin.base.common.services.Events;


/**
 스케줄러용 서비스를 한 곳에 몰아넣거나 하나하나씩 생성하면 됨
 */
@EnableScheduling
@EnableCaching
@Configuration
@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class, MongoReactiveAutoConfiguration.class })
@ComponentScan(basePackages = { "net.mycorp.jimin.base.*", "net.mycorp.jimin.mybot.*" }, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.mycorp.jimin.base.configuration.webflux.*") })
@Import({ net.mycorp.jimin.base.core.Initializer.class })
public class SchedulerApp implements CommandLineRunner {
    
	protected static Logger log = LoggerFactory.getLogger(SchedulerApp.class);
	
	
	@Autowired
	private Events events;
	
	@Autowired
	private AuthService auths;
    

	@Autowired
	private PlatformTransactionManager tm;
	
    public static void main(String[] args) {
		SpringApplication.run(SchedulerApp.class, args);
	}

    @Override
    public void run(String... strings) throws Exception {	
    }
    
	private void login() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("org_tr");
		def.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);

		TransactionStatus status = tm.getTransaction(def);
		try {
			OcUser root = auths.loginById("kingsman");
			if (root == null) {
				throw new RuntimeException("root 유져가 존재하지 않습니다");
			}
			tm.commit(status);
		} catch (Exception e) {
			tm.rollback(status);
			throw e;
		}
	}

	
	@Transactional
	@Scheduled(cron="${scheduling.job.testJob}")
	public void importItgsEscort() {
		try {
			login();
			events.logEventAtBatch("testJob", "success", "");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			events.logEventAtBatch("testJob", "fail", e.getMessage());
		}
	}
}
