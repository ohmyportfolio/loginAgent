package net.mycorp.jimin.base.service;

import java.util.List;
import java.util.Map;

import net.mycorp.jimin.base.domain.OcCommand;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.domain.OcResult;

public interface DataService {

	public OcMap get(OcResource resource, OcContext ctx);

	public OcResult select(OcResource resource, OcContext ctx);

	public OcResult select(OcResource resource, OcCommand command, OcContext ctx);

	public int execute(OcResource resource, OcCommand command, Map<String, Object> params);

	public List<String> insert(OcResource resource, List<? extends Map<String, Object>> rows);

	public int update(OcResource resource, Object id, Map<String, Object> condition, Map<String, Object> row);

	public int delete(OcResource resource, Object id, Map<String, Object> condition);

	public long count(OcResource resource, OcContext ctx);

	public boolean existsColumn(OcResource resource, String column);

}