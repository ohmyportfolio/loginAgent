package net.mycorp.jimin.base.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.ChangeStreamPublisher;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.Success;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcDatasource;
import net.mycorp.jimin.base.domain.OcMeta;
import net.mycorp.jimin.base.domain.OcResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MongoFluxService {

	@Autowired
	private MetaService metas;

	private Map<String, MongoClient> clients = new HashMap<>();

	public OcMeta meta() {
		return metas.getMeta();
	}

	public MongoClient getClient(OcDatasource datasource) {
		MongoClient client = clients.get(datasource.getName());
		if (client == null) {
			client = MongoClients.create(datasource.getUrl());
			clients.put(datasource.getName(), client);
		}
		return client;
	}

	public MongoDatabase getDatabase(OcResource resource) {
		OcDatasource datasource = meta().getDatasource(resource.getDatasource());
		MongoClient client = getClient(datasource);
		return client.getDatabase(datasource.getDatabase());
	}

	public MongoCollection<Document> getCollection(OcResource resource) {
		MongoDatabase database = getDatabase(resource);
		return database.getCollection(resource.getTable());
	}

	private List<Bson> toPipeline(OcContext ctx) {
		Bson filters;
		if (ctx.getId() == null || "any".equals(ctx.getId())) {
			filters = toFilter(ctx.getCondition());
		} else {
			filters = toFilter(ctx.getId(), ctx.getCondition());
		}
		List<Bson> pipeline = Collections.singletonList(Aggregates.match(filters));
		return pipeline;
	}

	private Bson toFilter(Map<String, Object> condition) {
		if (condition == null || condition.size() == 0)
			return new Document();

		List<Bson> filterList = new ArrayList<>();
		for (Map.Entry<String, Object> entry : condition.entrySet()) {
			filterList.add(Filters.eq(entry.getKey(), entry.getKey()));
		}
		Bson filters = Filters.and(filterList);
		return filters;
	}

	private Bson toFilter(Object id, Map<String, Object> condition) {
		Map<String, Object> filter = new HashMap<>();
		filter.put("id", id);
		filter.putAll(condition);
		return toFilter(filter);
	}

	private Bson toSort(String orderby) {
		if (orderby == null)
			return null;
		Map<String, Object> sort = new HashMap<>();
		String[] parts = orderby.split(",");
		for (String part : parts) {
			String[] subparts = part.split(" ");
			sort.put(subparts[0].trim(), subparts.length > 0 && subparts[1].trim().equalsIgnoreCase("desc") ? -1 : 1);
		}
		return new Document(sort);
	}

	public Flux<ChangeStreamDocument<Document>> watch(OcResource resource, OcContext ctx) {
		ChangeStreamPublisher<Document> publisher = getCollection(resource).watch(toPipeline(ctx));
		return Flux.from(publisher);
	}

	public Mono<Long> count(OcResource resource, OcContext ctx) {
		Publisher<Long> publisher = getCollection(resource).countDocuments(toFilter(ctx.getCondition()));
		return Mono.from(publisher);
	}

	public Flux<Document> select(OcResource resource, OcContext ctx) {
		FindPublisher<Document> publisher = getCollection(resource).find(toFilter(ctx.getCondition()))
				.sort(toSort(ctx.getOrderby())).skip(ctx.getOffset()).limit(ctx.getLimit());
		return Flux.from(publisher);
	}

	public Mono<Document> get(OcResource resource, OcContext ctx) {
		FindPublisher<Document> publisher = getCollection(resource).find(toFilter(ctx.getId(), ctx.getCondition()))
				.limit(1);
		return Flux.from(publisher).next();
	}

	public Flux<Success> insert(OcResource resource, List<? extends Map<String, Object>> rows) {
		List<Document> docs = rows.stream().map(row -> new Document(row)).collect(Collectors.toList());
		Publisher<Success> publisher = getCollection(resource).insertMany(docs);
		return Flux.from(publisher);
	}

	public Mono<UpdateResult> update(OcResource resource, Object id, Map<String, Object> condition,
			Map<String, Object> row) {
		Publisher<UpdateResult> publisher = getCollection(resource).updateMany(toFilter(id, condition),
				new Document(row));
		return Mono.from(publisher);
	}

	public Mono<DeleteResult> delete(OcResource resource, Object id, Map<String, Object> condition) {
		Publisher<DeleteResult> publisher = getCollection(resource).deleteMany(toFilter(id, condition));
		return Mono.from(publisher);
	}

}