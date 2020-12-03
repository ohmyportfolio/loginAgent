package net.mycorp.jimin.base.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.core.Global;
import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcCommand;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.util.Helper;
import net.mycorp.jimin.base.util.StringHelper;

@Service
public class ScriptService {

	protected static Logger log = LoggerFactory.getLogger(ScriptService.class);

	private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

	private Invocable invocable = (Invocable) engine;

	private Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);

	@Autowired
	private SqlService sqls;
	
	@Autowired
	private GatewayDataService datas;

	@Autowired
	private MetaService metas;

	
	@Autowired
	private AutowireCapableBeanFactory beanFactory;

	public class GlobalContext {

		public Object execute(String commandPath, Map<String, Object> context) {
			return Global.execute(commandPath, context);
		}

		public void error(Exception e, String message, Object... params) {
			throw new OccamException(e, message, params);
		}

		public void error(String message, Object... params) {
			throw new OccamException(message, params);
		}

		public void error(Exception e) {
			throw new OccamException(e);
		}
		
		public void print(Object obj) {
			log.info(obj == null ? "null" : obj.toString());
		}
		private boolean skipAcl;
		public boolean getSkipAcl() {
			return this.skipAcl;
		}
		public void setSkipAcl(boolean skipAcl) {
			this.skipAcl = skipAcl;
		}
		
	}

	public class CallContext extends HashMap<String, Object> {

		private static final long serialVersionUID = 3630505429222707275L;

	}

	@PostConstruct
	private void init() throws ScriptException, NoSuchMethodException {
		addBinding("scripts", this);
		addBinding("sqls", sqls);
		addBinding("datas", datas);
		addBinding("metas", metas);
		addBinding("beanFactory", beanFactory);		
		addBinding("map", OcMap.class);
		addBinding("meta", metas.getMeta());
		addJavaType("Helper", Helper.class);
		addJavaType("DSL", DSL.class);
		addJavaType("Config", Configs.class);
		Object global = engine.eval("this");
		Object jsObject = engine.eval("Object");
		Invocable invocable = (Invocable) engine;
		invocable.invokeMethod(jsObject, "bindProperties", global, new GlobalContext());
	}

	public void addBinding(String name, Object value) {
		bindings.put(name, value);
	}

	public void addJavaType(String name, Class<?> javaType) {
		try {
			engine.eval("function makeJsClass(nativeClass) { return Java.type(nativeClass); };");
			Object jsJavaType = invocable.invokeFunction("makeJsClass", javaType.getName());
			addBinding(name, jsJavaType);
		} catch (ScriptException | NoSuchMethodException e) {
			throw new OccamException(e);
		}
	}
	
	public Object eval(String script, Map<String, Object> context) {
		try {
			if (script.startsWith("{"))
				script = "val = " + script;
			if (context != null) {
				Bindings callBindings = new SimpleBindings();		
				callBindings.putAll(bindings);		
				for (String key : context.keySet()) {
					callBindings.put(key, context.get(key));
				}
				return engine.eval(script, callBindings);
			} else {
				return engine.eval(script, bindings);
			}
		} catch (ScriptException e) {
			log.error(e.getMessage());
			log.error("Script:\n" + script);
			log.error("Context:\n" + StringHelper.toString(context));
			throw new OccamException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T toBean(String script, Class<T> type) {
		try {
			T bean = type.getDeclaredConstructor().newInstance();
			BeanUtils.populate(bean, (Map<String, ? extends Object>) eval(script, null));
			return bean;
		} catch (Exception e) {
			throw new OccamException(e);
		}
	}

	public Object execute(OcResource resource, OcCommand command, OcContext context) {
		CallContext scriptCtx = new CallContext();
		scriptCtx.put("context", context);
		scriptCtx.put("command", command);
		scriptCtx.put("resource", resource);
		return eval(command.getText(), scriptCtx);
	}

	public Object eval(File file) {
		try {
			return eval(FileUtils.readFileToString(file, "UTF-8"), null);
		} catch (IOException e) {
			throw new OccamException(e);
		}
	}

}
