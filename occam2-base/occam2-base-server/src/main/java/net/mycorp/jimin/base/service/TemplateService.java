package net.mycorp.jimin.base.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.auth.services.AuthService;
import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcCommand;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.misc.IfandDirevtive;
import net.mycorp.jimin.base.misc.IfneDirevtive;
import net.mycorp.jimin.base.util.Helper;
import net.mycorp.jimin.base.util.IOHelper;

@Service
public class TemplateService {

	private VelocityEngine engine;

	private Helper h = new Helper();
	
	@Autowired
	private MetaService metas;

	@Autowired
	private AuthService auths;

	@PostConstruct
	private void init() {
		engine = new VelocityEngine();
		engine.loadDirective(IfneDirevtive.class.getName());
		engine.loadDirective(IfandDirevtive.class.getName());
	}
	
	private VelocityEngine getEngine() {
		return engine;
	}

	public String merge(String templateLocation, OcResource resource, OcCommand command, Map<String, Object> context) {
		Map<String, Object> templateContext = createContext(resource, command, context);

		StringWriter out = new StringWriter();
		try {
			getEngine().getTemplate(templateLocation).merge(new VelocityContext(templateContext), out);
			return out.getBuffer().toString();
		} catch (Exception e) {
			throw new OccamException(e);
		} finally {
			IOHelper.close(out);
		}
	}

	public String evaluate(String template, OcResource resource, OcCommand command, Map<String, Object> context) {
		Map<String, Object> templateContext = createContext(resource, command, context);

		if (template.indexOf("$") == -1 && template.indexOf("#") == -1)
			return template;
		StringWriter out = new StringWriter();
		StringReader in = new StringReader(template);
		try {
			getEngine().evaluate(new VelocityContext(templateContext), out, "", in);
			return out.getBuffer().toString().trim();
		} catch (Exception e) {
			throw new OccamException(e);
		} finally {
			IOHelper.close(out);
			IOHelper.close(in);
		}
	}

	private Map<String, Object> createContext(OcResource resource, OcCommand command, Map<String, Object> context) {
		Map<String, Object> templateContext = new HashMap<>(context);
		templateContext.put("h", h);
		templateContext.put("auths", auths);
		templateContext.put("command", command);
		templateContext.put("resource", resource);
		templateContext.put("meta", metas.getMeta());
		return templateContext;
	}

}
