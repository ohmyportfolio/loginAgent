package net.mycorp.jimin.base.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.OcCommand;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcDatasource;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.domain.OcResult;

@Service
public class GatewayDataService implements DataService {

	@Autowired
	private MetaService metas;

	@Autowired
	private ApplicationContext applicationContext;
	
	public DataService getConcrete(OcResource resource) {
		OcDatasource datasource = metas.getMeta().getDatasource(resource.getDatasource());
		if (datasource.isSql()) {
			return applicationContext.getBean(SqlService.class);
		} else {
			return applicationContext.getBean(MongoDataService.class);
		}
	}

	@Override
	public OcMap get(OcResource resource, OcContext ctx) {
		return getConcrete(resource).get(resource, ctx);
	}

	@Override
	public OcResult select(OcResource resource, OcContext ctx) {
		return getConcrete(resource).select(resource, ctx);
	}

	@Override
	public OcResult select(OcResource resource, OcCommand command, OcContext ctx) {
		return getConcrete(resource).select(resource, command, ctx);
	}

	@Override
	public int execute(OcResource resource, OcCommand command, Map<String, Object> params) {
		return getConcrete(resource).execute(resource, command, params);
	}

	@Override
	public List<String> insert(OcResource resource, List<? extends Map<String, Object>> rows) {
		return getConcrete(resource).insert(resource, rows);
	}

	@Override
	public int update(OcResource resource, Object id, Map<String, Object> condition, Map<String, Object> row) {
		return getConcrete(resource).update(resource, id, condition, row);
	}

	@Override
	public int delete(OcResource resource, Object id, Map<String, Object> condition) {
		return getConcrete(resource).delete(resource, id, condition);
	}

	@Override
	public long count(OcResource resource, OcContext ctx) {
		return getConcrete(resource).count(resource, ctx);
	}

	@Override
	public boolean existsColumn(OcResource resource, String column) {
		return getConcrete(resource).existsColumn(resource, column);
	}

}
