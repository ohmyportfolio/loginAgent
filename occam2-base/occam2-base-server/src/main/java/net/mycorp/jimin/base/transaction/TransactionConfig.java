package net.mycorp.jimin.base.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionConfig {

	@Bean
	public ChainedTransactionManager transactionManager() {
		return new ChainedTransactionManager();
	}

}
