package net.mycorp.jimin.base.core;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.mycorp.jimin.base.common.services.Datasources;
import net.mycorp.jimin.base.common.services.Resources;
import net.mycorp.jimin.base.service.CommandService;
import net.mycorp.jimin.base.service.MetaService;
import net.mycorp.jimin.base.service.SqlService;
import net.mycorp.jimin.base.transaction.ChainedTransactionManager;

@Component
public class Initializer {

	@Autowired
	private MetaService metas;
	
	@Autowired
	private CommandService commands;
	
	@Autowired
	private SqlService sqls;
	
	@Autowired
	private ChainedTransactionManager transactionManager;
	
	@Autowired
	private Datasources datasources;
	
	@Autowired
	private Resources resources;
	
	@PostConstruct
	public void init() throws Exception {
		Global.commands = commands;
		
		Configs.load();
		metas.init();
		sqls.init();
		transactionManager.init(sqls.getTransactionManagers());
		
		if (Configs.getBoolean("base.loadMetaDb")) {
			datasources.loadAll();
			resources.loadAll();
		}
	}
	
	public MetaService getMetas() {
		return metas;
	}

	public CommandService getCommands() {
		return commands;
	}

	public SqlService getSqls() {
		return sqls;
	}
}
