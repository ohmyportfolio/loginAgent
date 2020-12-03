package net.mycorp.jimin.base.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.common.services.Events;
import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcCommand;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcResource;

@Service
public class CommandService {

	private static Logger log = LoggerFactory.getLogger(CommandService.class);
	
	@Autowired
	private GatewayDataService datas;

	@Autowired
	private ScriptService scripts;

	@Autowired
	private MetaService metas;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private Events events;
	
	public Object execute(OcContext context) {
		log.debug("Executing context: " + context);
		// 실제 command가 있는 상위 리소스 (eg. base)
		OcResource commandResource = metas.getMeta().getResource(context.getCommandResource(), true);
		// 호출된 리소스 (eg. /api/users => users)
		OcResource resource = metas.getMeta().getResource(context.getResource());
		if(commandResource == null)
			commandResource = resource;
		if (resource == null)
			throw new OccamException("Resource %s not found.", context.getResource());
		OcCommand command = commandResource.getCommand(context.getCommand());
		
		Object service = null;
		Method commandMethod = null;
		if(beanFactory.containsBean(commandResource.getName())) {
			service = beanFactory.getBean(commandResource.getName());
			commandMethod = getCommandMethod(service, context.getCommand(), false);
		}

		Object result;
		if(commandMethod != null && !context.getXmlOnly()) {
			// java(groovy) service
			result = executeService(service, context.getCommand(), context, false);
			events.logEvent(resource, context.getCommand(), context, result);
		}  else if(command != null) {
			// resource xml
			OcCommand preCommand = commandResource.getCommand("pre_" + context.getCommand());
			OcCommand postCommand = commandResource.getCommand("post_" + context.getCommand());
			if (preCommand != null) {
				executeXml(resource, preCommand, context);
			}
			result = executeXml(resource, command, context);	
			if (postCommand != null) {
				executeXml(resource, postCommand, context);
			}
			events.logEvent(resource, command.getName(), context, result);
		} else {
			throw new OccamException("Command %s.%s not found.", context.getResource(), context.getCommand());
		}
		return result;
	}
	
	private Object executeXml(OcResource resource, OcCommand command, OcContext context) {
		if (command.getType() == OcCommand.Type.sql) {
			if (command.getResult() == OcCommand.Result.result)
				return datas.select(resource, command, context);
			else if (command.getResult() == OcCommand.Result.list)
				return datas.select(resource, command, context).getData();
			else if (command.getResult() == OcCommand.Result.object)
				return datas.select(resource, command, context).getObject();
			else if (command.getResult() == OcCommand.Result.value)
				return datas.select(resource, command, context).getValue();
			else if (command.getResult() == OcCommand.Result.values)
				return datas.select(resource, command, context).getValues(Object.class);
			else
				return datas.execute(resource, command, context);
		} else if (command.getType() == OcCommand.Type.script) {
			return scripts.execute(resource, command, context);
		} else {
			throw new OccamException("illegal command type %s", command.getType());
		}
	}
	
	private Object executeService(Object service, String methodName, OcContext context, boolean exact) {
		Method serviceMethod = getCommandMethod(service, methodName, exact);
		if(serviceMethod == null)
			return null;
		try {
			return serviceMethod.invoke(service, context);
		} catch (IllegalArgumentException e) {
			try {
				return serviceMethod.invoke(service);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
				throw new OccamException(e1.getCause() != null ? e1.getCause() : e1);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new OccamException(e.getCause() != null ? e.getCause() : e);
		}
	}
	
	private Method getCommandMethod(Object service, String methodName, boolean exact) {
		Class<? extends Object> clazz = service.getClass();
		
		try {
			return clazz.getMethod(methodName, OcContext.class);
		} catch (NoSuchMethodException | SecurityException e1) {
			if(exact)
				return null;
			try {
				return clazz.getMethod(methodName, Map.class);
			} catch (NoSuchMethodException | SecurityException e2) {
				try {
					return clazz.getMethod(methodName, Object.class);
				} catch (NoSuchMethodException | SecurityException e3) {
					try {
						return clazz.getMethod(methodName);
					} catch (NoSuchMethodException | SecurityException e4) {
						return null;
					}
				}
			}
		}
	}
	
}
