package net.mycorp.jimin.base.controller;

import static net.mycorp.jimin.base.core.Global.command;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.Success;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMeta;
import net.mycorp.jimin.base.service.MetaService;
import net.mycorp.jimin.base.service.MongoFluxService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "flux")
public class FluxController {

	@Autowired
	private MongoFluxService mfs;

	@Autowired
	private MetaService metas;

	private OcMeta meta() {
		return metas.getMeta();
	}

	@RequestMapping(path = "{resource}/{id}/watch", method = RequestMethod.GET)
	public Flux<ChangeStreamDocument<Document>> watch(@PathVariable String resource, @PathVariable String id,
			@RequestParam Map<String, Object> requestParam) {
		OcContext ctx = command("watch").resource(resource).id(id).addAll(requestParam).fromClient(true);
		return mfs.watch(meta().getResource(resource), ctx);
	}

	@RequestMapping(path = "{resource}/{id}", method = RequestMethod.GET)
	public Mono<Document> get(@PathVariable String resource, @PathVariable String id,
			@RequestParam Map<String, Object> requestParam) {
		OcContext ctx = command("get").resource(resource).id(id).addAll(requestParam).fromClient(true);
		return mfs.get(meta().getResource(resource), ctx);
	}

	@RequestMapping(path = "{resource}", method = RequestMethod.GET)
	public Flux<Document> select(@PathVariable String resource, @RequestParam Map<String, Object> requestParam) {
		OcContext ctx = command("select").resource(resource).addAll(requestParam).fromClient(true);
		return mfs.select(meta().getResource(resource), ctx);
	}

	@RequestMapping(path = "{resource}", method = RequestMethod.POST)
	public Flux<Success> insert(@PathVariable String resource, @RequestParam Map<String, Object> requestParam,
			@RequestBody Object rows) {
		OcContext ctx = command("insert").resource(resource).row(rows).addAll(requestParam).fromClient(true);
		return mfs.insert(meta().getResource(resource), ctx.getRows());
	}

	@RequestMapping(path = "{resource}/{id}", method = RequestMethod.PUT)
	public Mono<UpdateResult> update(@PathVariable String resource, @PathVariable String id,
			@RequestParam Map<String, Object> requestParam, @RequestBody Map<String, Object> row) {
		OcContext ctx = command("update").resource(resource).id(id).row(row).addAll(requestParam).fromClient(true);
		return mfs.update(meta().getResource(resource), ctx.getId(), ctx.getCondition(), ctx.getRow());
	}

	@RequestMapping(path = "{resource}", method = RequestMethod.DELETE)
	public void delete(@PathVariable String resource, @RequestParam Map<String, Object> requestParam,
			@RequestBody List<Object> ids) {
		for (Object id : ids) {
			OcContext ctx = command("delete").resource(resource).id(id).addAll(requestParam).fromClient(true);
			mfs.delete(meta().getResource(resource), ctx.getId(), ctx.getCondition());
		}
	}

	@RequestMapping(path = "{resource}/{id}", method = RequestMethod.DELETE)
	public Mono<DeleteResult> delete(@PathVariable String resource, @PathVariable String id,
			@RequestParam Map<String, Object> requestParam) {
		OcContext ctx = command("delete").resource(resource).id(id).addAll(requestParam).fromClient(true);
		return mfs.delete(meta().getResource(resource), ctx.getId(), ctx.getCondition());
	}
}
