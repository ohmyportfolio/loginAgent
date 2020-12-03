package net.mycorp.jimin.base.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.OcCommand;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.domain.OcResult;

@Service
public class MongoDataService implements DataService {

	@Autowired
	private MongoFluxService mfs;

	@Override
	public OcResult select(OcResource resource, OcContext ctx) {
		OcResult result = new OcResult();
		result.setTotal(mfs.count(resource, ctx).block());
		result.setData(mfs.select(resource, ctx).map(doc -> new OcMap(doc)).collectList().block());
		return result;
	}

	@Override
	public long count(OcResource resource, OcContext ctx) {
		return mfs.count(resource, ctx).block();
	}

	@Override
	public OcMap get(OcResource resource, OcContext ctx) {
		return new OcMap(mfs.get(resource, ctx).block());
	}

	@Override
	public List<String> insert(OcResource resource, List<? extends Map<String, Object>> rows) {
		return mfs.insert(resource, rows).map(success -> success.toString()).collectList().block();
	}

	@Override
	public int update(OcResource resource, Object id, Map<String, Object> condition, Map<String, Object> row) {
		return (int) mfs.update(resource, id, condition, row).block().getModifiedCount();
	}

	@Override
	public int delete(OcResource resource, Object id, Map<String, Object> condition) {
		return (int) mfs.delete(resource, id, condition).block().getDeletedCount();
	}

	@Override
	public OcResult select(OcResource resource, OcCommand command, OcContext ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int execute(OcResource resource, OcCommand command, Map<String, Object> params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean existsColumn(OcResource resource, String column) {
		throw new UnsupportedOperationException();
	}

}