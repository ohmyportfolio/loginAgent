package net.mycorp.jimin.base.controller;

import static net.mycorp.jimin.base.core.Global.command;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcColumn;
import net.mycorp.jimin.base.domain.OcMeta;
import net.mycorp.jimin.base.service.MetaService;

@Transactional
@Controller
@RequestMapping(path = "/api", produces = { "application/json", "text/plain" })
public class ApiController {

	protected static Logger log = LoggerFactory.getLogger(ApiController.class);
	
	@Autowired
	private MetaService metas;

	@RequestMapping(path = "meta", method = RequestMethod.GET)
	@ResponseBody
	public OcMeta meta() {
		return metas.getMeta();
	}
	
	@RequestMapping(path = "createDb", method = RequestMethod.GET)
	@ResponseBody
	public void createDb() {
		metas.createDb();
	}
	
	@RequestMapping(path = "loadData", method = RequestMethod.GET)
	@ResponseBody
	public void loadData() {
		metas.loadData();
	}
	
	@RequestMapping(path = "loadData/{resource}", method = RequestMethod.GET)
	@ResponseBody
	public void loadData(@PathVariable String resource) {
		metas.loadData(resource);
	}
	
	@RequestMapping(path = "meta/{key}", method = RequestMethod.GET)
	@ResponseBody
	public Object meta1(@PathVariable String key) {
		return metas.getMeta().get(key);
	}

	@RequestMapping(path = "meta/{key1}/{key2}", method = RequestMethod.GET)
	@ResponseBody
	public Object meta2(@PathVariable String key1, @PathVariable String key2) {
		return metas.getMeta().getMap(key1).get(key2);
	}
	
	@RequestMapping(path = "meta/views/{key1}", method = RequestMethod.GET, produces = "text/plain")
	@ResponseBody
	public Object views(@PathVariable String key1) {
		Object view = metas.getMeta().getMap("views").get(key1);
		if(view == null)
			throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
		return view;
	}

	@RequestMapping(path = "meta/columns", method = RequestMethod.GET)
	@ResponseBody
	public List<OcColumn> columns(@RequestParam String resource) {
		return meta().getResource(resource).getColumns();
	}

	@RequestMapping(path = "{resource}/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Object get(@PathVariable String resource, @PathVariable String id,
			@RequestParam Map<String, Object> requestParam) {
		Object result = command("get").resource(resource).id(id).addAll(requestParam).fromClient(true).execute();
		if(result == null)
			throw new OccamException("%s %s not found!", resource, id);
		return result;
	}

	@RequestMapping(path = "{resource}", method = RequestMethod.GET)
	@ResponseBody
	public Object select(@PathVariable String resource, @RequestParam Map<String, Object> requestParam) {
		return command("select").resource(resource).addAll(requestParam).fromClient(true).execute();
	}
	
	@RequestMapping(path = "{resource}/any/select", method = RequestMethod.POST)
	@ResponseBody
	public Object selectPost(@PathVariable String resource, @RequestBody Map<String, Object> ocContext) {
		return command("select").resource(resource).addAll(ocContext).fromClient(true).execute();
	}
	
	@RequestMapping(path = "{resource}", method = RequestMethod.POST)
	@ResponseBody
	public Object insert(@PathVariable String resource, @RequestParam Map<String, Object> requestParam,
			@RequestBody Object rows) {
		return command("insert").resource(resource).row(rows).addAll(requestParam).fromClient(true).execute();
	}
	
	@RequestMapping(path = "{resource}", method = RequestMethod.PUT)
	@ResponseBody
	public Object save(@PathVariable String resource, @RequestParam Map<String, Object> requestParam,
			@RequestBody Object rows) {
		return command("save").resource(resource).row(rows).addAll(requestParam).fromClient(true).execute();
	}

	@RequestMapping(path = "{resource}/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Object update(@PathVariable String resource, @PathVariable String id,
			@RequestParam Map<String, Object> requestParam, @RequestBody Map<String, Object> row) {
		return command("update").resource(resource).id(id).row(row).addAll(requestParam).fromClient(true).execute();
	}
	
	@RequestMapping(path = "{resource}", method = RequestMethod.DELETE)
	@ResponseBody
	public void delete(@PathVariable String resource, @RequestParam Map<String, Object> requestParam,
			@RequestBody List<Object> ids) {
		for (Object id : ids) {
			command("delete").resource(resource).id(id).addAll(requestParam).fromClient(true).execute();			
		}
	}
	
	@RequestMapping(path = "{resource}/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Object delete(@PathVariable String resource, @PathVariable String id,
			@RequestParam Map<String, Object> requestParam) {
		return command("delete").resource(resource).id(id).addAll(requestParam).fromClient(true).execute();
	}

	@RequestMapping(path = "{resource}/{id}/{command}")
	@ResponseBody
	public Object execute(@PathVariable String resource, @PathVariable String id, @PathVariable String command,
			@RequestParam Map<String, Object> requestParam, @RequestBody(required = false) Object row) {
		return command(command).resource(resource).id(id).row(row).addAll(requestParam).fromClient(true).execute();
	}
	
	
}