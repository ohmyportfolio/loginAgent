package net.mycorp.jimin.base.service;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcMeta;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.domain.OcResult;
import net.mycorp.jimin.base.util.Maps;
import net.mycorp.jimin.base.util.Pair;

@Service
public class BaseService {

	@Autowired
	protected GatewayDataService datas;

	@Autowired
	protected MetaService metas;

	@Autowired
	protected CommandService commands;

	protected OcMeta meta;

	protected String resourceName;

	@Autowired
	protected ApplicationContext applicationContext;

	@PostConstruct
	public void init() {
		String[] beanNames = applicationContext.getBeanNamesForType(getClass());
		resourceName = beanNames[0];
		setResourceName(resourceName);
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
		meta = metas.getMeta();
	}

	public OcResource getResource() {
		return meta.getResource(resourceName);
	}
	
	protected OcResource getResource(OcContext ctx) {
		if(ctx.getResource() == null)
			return getResource();
		else
			return meta.getResource(ctx.getResource());
	}

	public Object executeXml() {
		return executeXml(null);
	}
	
	public Object executeXml(Map<String, Object> context) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for (int i = 2; i < stackTraceElements.length; i++) {
			if (stackTraceElements[i].getClassName() == getClass().getName()
					&& stackTraceElements[i].getMethodName() != "executeXml") {
				String commandName = stackTraceElements[i].getMethodName();
				
				OcContext ocContext;
				if(context == null) {
					ocContext = new OcContext();
					ocContext.setResource(applicationContext.getBeanNamesForType(getClass())[0]);
				} else if(context instanceof OcContext) {
					ocContext = (OcContext) context;
				} else {
					ocContext = new OcContext(context);
				}
				ocContext.setXmlOnly(true);
				return execute(commandName, ocContext);
			}
		}
		throw new OccamException("can't find command name");
	}

	public Object execute(String commandName, Map<String, Object> context) {
		return execute(commandName, new OcContext(context));
	}

	public Object execute(String commandName, OcContext ctx) {
		if (commandName.contains(".")) {
			String[] commandNames = commandName.split(".");
			ctx.setResource(commandNames[0]);
			ctx.setCommand(commandNames[1]);
		} else {
			ctx.setResource(getResource(ctx).getName());
			ctx.setCommand(commandName);
		}
		return commands.execute(ctx);
	}

	public OcMap get(OcContext ctx) {
		return datas.get(getResource(ctx), ctx);
	}
	
	public OcMap get(String id) {
		return get(ctx().id(id));
	}
	
	public OcMap getMap(String id) {
		return new OcMap((Map<String, Object>) get(id));
	}

	public OcResult select(OcContext ctx) {
		return datas.select(getResource(ctx), ctx);
	}

	@Deprecated
	public OcResult select(Map<String, Object> ctx) {
		return select(new OcContext(ctx));
	}

	public OcContext ctx() {
		return new OcContext();
	}
	
	public OcContext condition(Object... conditionItems) {
		return ctx().condition(conditionItems);
	}
	
	public OcContext select() {
		return select("*");
	}
	
	public OcContext resource(String resource) {
		return ctx().resource(resource);
	}
	
	public OcContext select(String select) {
		return ctx().command("select").select(select);
	}

	public List<String> insert(OcContext ctx) {
		preInsert(ctx);
		List<String> result = datas.insert(getResource(ctx), ctx.getRows());
		ctx.setId(result);
		postInsert(ctx);
		return result;
	}

	public String insert(Map<String, Object> row) {
		return insert(ctx().row(row)).get(0);
	}

	public int update(OcContext ctx) {
		preUpdate(ctx);
		int result = datas.update(getResource(ctx), ctx.getId(), ctx.getCondition(), ctx.getRow());		
		postUpdate(ctx);
		return result;
	}
	
	public int update(Object id, Map<String, Object> row) {
		return update(ctx().id(id).row(row));
	}

	public int delete(OcContext ctx) {
		preDelete(ctx);
		int result = datas.delete(getResource(ctx), ctx.getId(), ctx.getCondition());
		postDelete(ctx);
		return result;
	}

	public int delete(String id) {
		return delete(ctx().id(id));
	}

	public int delete(Map<String, Object> condition) {
		return delete(ctx().condition(condition));
	}

	public void deleteEach(Map<String, Object> condition) {
		List<String> list = select(ctx().select("id").condition(condition)).getValues(String.class);
		for (String id : list) {
			delete(id);
		}
	}
	
	public long count(OcContext ctx) {
		return datas.count(getResource(ctx), ctx);
	}
	
	public OcMap m(Pair... pairs) {
		return Maps.map(pairs);
	}
	
	public OcMap m(Object... items) {
		return Maps.map(items);
	}
	
	public DSLContext dsl() {
		return ((SqlService) datas.getConcrete(getResource())).dsl(getResource(), null);
	}

	protected Field<Object> field(String sql) {
		return DSL.field(sql);
	}
	
	protected Table<Record> table(String sql) {
		return DSL.table(sql);
	}
	
	public void preSave(OcContext ctx) {
	}

	public void preInsert(OcContext ctx) {
		preSave(ctx);
	}

	public void preUpdate(OcContext ctx) {
		preSave(ctx);
	}

	public void preDelete(OcContext ctx) {
	}

	public void postSave(OcContext ctx) {
	}

	public void postInsert(OcContext ctx) {
		postSave(ctx);
	}

	public void postUpdate(OcContext ctx) {
		postSave(ctx);
	}

	public void postDelete(OcContext ctx) {
	}

}
