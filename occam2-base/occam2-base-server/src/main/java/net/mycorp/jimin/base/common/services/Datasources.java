package net.mycorp.jimin.base.common.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcDatasource;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResult;
import net.mycorp.jimin.base.service.MetaService;
import net.mycorp.jimin.base.service.SqlService;
import net.mycorp.jimin.base.transaction.ChainedTransactionManager;

@Service
public class Datasources extends Bases {

	@Autowired
	private MetaService metas;

	@Autowired
	private SqlService sqls;

	@Autowired
	private ChainedTransactionManager transactionManager;

	@Override
	public void preSave(OcContext ctx) {
		super.preSave(ctx);
	}

	@Override
	public void postSave(OcContext ctx) {
		super.postSave(ctx);
		sqls.resetDatasource(ctx.id());
		OcMap datasource = getComplete(ctx);
		loadOne(datasource);
	}

	@Override
	public void postDelete(OcContext ctx) {
		super.postDelete(ctx);
		sqls.resetDatasource(ctx.id());
	}

	public void loadAll() {
		List<OcMap> datasourceList = select(ctx().skipPermit(true)).getData();
		for (OcMap datasource : datasourceList) {
			loadOne(datasource);
		}
	}

	private void loadOne(OcMap datasource) {
		String datasourceName = datasource.id();
		datasource.put("name", datasourceName);
		OcDatasource ocDatasource = datasource.toBean(OcDatasource.class);
		ocDatasource.setFromDb(true);
		metas.getMeta().addDatasource(ocDatasource);
		if(ocDatasource.isSql()) {
			sqls.getConfiguration(datasourceName);
			transactionManager.init(sqls.getTransactionManagers());
		}
	}

	public boolean test(OcContext ctx) throws SQLException {
		OcDatasource datasource = ctx.getRow().toBean(OcDatasource.class);
		DataSource ds = sqls.buildDataSource(datasource);
		Connection conn = ds.getConnection();
		try {
			return conn.isValid(0);
		} finally {
			conn.close();
		}
	}

	public List<OcMap> tables(OcContext ctx) {
		return sqls.getDbTables(ctx.id());
	}

	@Override
	public OcResult select(OcContext ctx) {
		OcResult result = super.select(ctx);
		for (OcDatasource ocDatasource : metas.getMeta().getDatasources().values()) {
			if (ocDatasource.isFromDb())
				continue;
			OcMap datasource = new OcMap(ocDatasource);
			datasource.put("id", ocDatasource.getName());
			result.getData().add(datasource);
			result.setTotal(result.getTotal() + 1);
		}
		return result;
	}

}
