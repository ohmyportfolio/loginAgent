package net.mycorp.jimin.base.core;

import java.util.Map;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.service.CommandService;
import net.mycorp.jimin.base.service.MetaService;
import net.mycorp.jimin.base.util.Maps;
import net.mycorp.jimin.base.util.Pair;

public class Global {

	public static MetaService metas;
	
	public static CommandService commands;

	public static OcContext select(String select) {
		return ctx().command("select").select(select);
	}
	
	public static OcContext command(String command) {
		return ctx().command(command);
	}
	
	public static OcContext ctx() {
		return new OcContext();
	}

	public static Object execute(String commandPath, Map<String, Object> contextMap) {
		OcContext context;
		if(contextMap instanceof OcContext)
			context = (OcContext) contextMap;
		else
			context = new OcContext(contextMap);
		context.setCommandPath(commandPath);
		return commands.execute(context);
	}
	
	public static Map<String, Object> m(Pair... pairs) {
		return Maps.map(pairs);
	}
	
	public static Map<String, Object> m(Object... items) {
		return Maps.map(items);
	}
	
	public static Pair p(String key, Object value) {
		return Maps.pair(key, value);
	}
}
