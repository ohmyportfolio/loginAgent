package net.mycorp.jimin.base.service;

import static net.mycorp.jimin.base.util.Maps.m;
import static net.mycorp.jimin.base.util.Maps.p;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jooq.Condition;
import org.jooq.CreateTableColumnStep;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.DeleteConditionStep;
import org.jooq.ExecuteContext;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertSetStep;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.RecordMapperProvider;
import org.jooq.RecordType;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOffsetStep;
import org.jooq.SelectSelectStep;
import org.jooq.SortField;
import org.jooq.TransactionProvider;
import org.jooq.UpdateConditionStep;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.exception.IOException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.SQLDataType;
import org.jooq.tools.jdbc.JDBCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.DbColumn;
import net.mycorp.jimin.base.domain.OcColumn;
import net.mycorp.jimin.base.domain.OcCommand;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcDatasource;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcMeta;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.domain.OcResult;
import net.mycorp.jimin.base.util.Helper;
import oracle.sql.TIMESTAMP;

@SuppressWarnings("restriction")
@Service
public class SqlService implements DataService {

	private static final String COLUMN_SEPARATOR = "__";

	@Autowired
	private MetaService metas;
	
	@Autowired
	private CommandService commands;

	@Autowired
	private TemplateService templates;
	
	@Autowired
	private ScriptService scripts;
	
	private OcMeta meta;

	private Map<String, DefaultConfiguration> configurations = new LinkedHashMap<>();

	private Map<String, PlatformTransactionManager> transactionManagers = new LinkedHashMap<>();

	protected Logger log = LoggerFactory.getLogger(getClass());
	
	public void init() {
		meta = metas.getMeta();
		for(OcDatasource datasource : meta.getDatasources().values()) {
			if(datasource.isSql())
				getConfiguration(datasource.getName());
		}
	}
	
	public Collection<PlatformTransactionManager> getTransactionManagers() {
		return transactionManagers.values();
	}
	
	private String getSql(OcResource resource, OcCommand command, Map<String, Object> context) {
		return templates.evaluate(command.getText(), resource, command, context);		
	}
	
	private ResultQuery<Record> getQuery(OcResource resource, OcCommand command, Map<String, Object> context) {
		DSLContext dsl = dsl(resource, command);
		String sql = getSql(resource, command, context);
		return dsl.resultQuery(escapeSqlParams(sql, command), extractSqlParams(sql, context, command));
	}

	public int execute(OcResource resource, OcCommand command, Map<String, Object> context) {
		return getQuery(resource, command, context).execute();
	}
	
	public OcMap get(OcResource resource, OcContext context) {
		if(context.id() != null && !context.id().equals("any")) {
			context.setCondition(buildPkMap(resource, context.id()));
		}
		if(context.getCondition().isEmpty())
			throw new OccamException("id and condition is empty");
		List<OcMap> data = select(resource, null, context).getData();
		if (data.size() == 0)
			return null;
		else
			return data.get(0);
	}

	public OcMap get(OcResource resource, Object id) {
		if(id == null)
			throw new OccamException("%s id can not be null.", resource.getName());
		OcContext ctx = new OcContext();
		return select(resource, ctx.condition(buildPkMap(resource, id.toString())).limit(1)).getObject();
	}
	
	private Map<String, Object> buildPkMap(OcResource resource, Object id) {
		List<String> pks = getPks(resource);
		Map<String, Object> condition = new HashMap<>();
		Object[] ids;
		if(id instanceof String) {
			ids = ((String)id).split(",");
		} else {
			ids = new Object[]{id};
		}
		for (int i = 0; i < ids.length && i < pks.size(); i++) {
			condition.put(pks.get(i), ids[i]);
		}
		return condition;
	}

	public long count(OcResource resource, OcContext params) {
		resource.getCommand("count");
		return select(resource, null, params).getTotal();
	}

	public OcResult select(OcResource resource, OcContext params) {
		return select(resource, null, params);
	}
	
	public OcResult select(OcResource resource, OcCommand command, OcContext context) {
		// init
		DSLContext dsl = dsl(resource, command);
		
		String query = command != null ? command.getText() : null;
		String select = context.getSelect();
		String table = resource.getTable();
		String where = context.getWhere();
		Map<String, Object> condition = context.getCondition();
		int offset = context.getOffset();
		int limit = context.getLimit();
		String orderby = context.getOrderby();
		Set<OcColumn> manyToOnes1 = new LinkedHashSet<OcColumn>(); // join for each column
		Set<OcColumn> manyToOnes2 = new HashSet<OcColumn>(); // query for all column
		Set<OcColumn> oneToManys = new HashSet<OcColumn>();
		Set<OcColumn> commandColumns = new HashSet<OcColumn>();
		Set<String> jsonColumns = new HashSet<String>();
		boolean needSublize = false;
		
		// query
		boolean isQuery = false;
		if (query != null) {
			query = "(" + templates.evaluate(query, resource, command, context) + ")";
			isQuery = true;
		}
		
		// field
		Set<SelectFieldOrAsterisk> fields = new LinkedHashSet<>();
		if (select != null && !select.equals("*")) {
			select = templates.evaluate(select, resource, command, context);
			String[] columns = select.split(",");			

			Set<String> columnSet = new LinkedHashSet<String>();
			// columnSet.addAll(getPks(resource)); // pk를 무조건 select에 추가할 필요는 없을듯. 필요하면 client에서 넘기도록
			
			for (String column : columns) {
				if ("".equals(column.trim()))
					continue;
				columnSet.add(column.trim());
			}
			
			for (String column : columnSet) {
				String alias = column;
				if (column.contains(COLUMN_SEPARATOR) && !column.startsWith(COLUMN_SEPARATOR)) {
					String[] columnNames = column.split(COLUMN_SEPARATOR);
					if("this".equals(columnNames[0])) {
						fields.add(field(convertFieldName(column)).as(alias));
					} else {
						OcColumn nextColumn = null;
						int joinStep = 1;
						do {
							int level = joinStep++;
							OcColumn column2 = null;
							if(columnNames.length > level) {
								OcResource resource2 = level == 1 ? resource : meta.getResource(nextColumn.getResource());
								column2 = (OcColumn) Helper.cloneBean(resource2.getColumn(columnNames[level - 1], true));
								if(level > 1) {
									column2.setFk(nextColumn.getName() + "." + column2.getFk());
									column2.setName(nextColumn.getName() + "__" + column2.getName());
								}
								manyToOnes1.add(column2);
								if(columnNames.length == level + 1) {
									fields.add(field(column2.getName()+"."+columnNames[level]).as(alias));
									OcResource resource3 = meta.getResource(column2.getResource());
									OcColumn column3 = resource3.getColumn(columnNames[level]);
									if(column3 != null && column3.getRelationship() == OcColumn.Relationship.json) {
										jsonColumns.add(alias);
									}
								}
							}
							nextColumn = column2;
						} while(nextColumn != null);
					}
				} else {
					OcColumn ocColumn = resource.getColumn(column);
					
					if (ocColumn != null && ocColumn.getRelationship() == OcColumn.Relationship.json) {
						jsonColumns.add(ocColumn.getName());
					}
					if (ocColumn != null && ocColumn.getRelationship() == OcColumn.Relationship.one_to_many) {
						oneToManys.add(ocColumn);
					} else if (ocColumn != null && ocColumn.getRelationship() == OcColumn.Relationship.many_to_one) {
						manyToOnes2.add(ocColumn);
						fields.add(field("this." + ocColumn.getFk()).as(ocColumn.getFk()));
					} else if (ocColumn != null && ocColumn.getRelationship() == OcColumn.Relationship.command) {
						commandColumns.add(ocColumn);
					} else if (ocColumn != null && ocColumn.getSql() != null) {
						String joins = ocColumn.getJoins();
						if(joins != null ) {
							String[] joinArr = joins.split(",");
							for (String join : joinArr) {
								manyToOnes1.add(resource.getColumn(join.trim(), true));
							}
						}
						fields.add(field(ocColumn.getSql()).as(column));
						needSublize = true;
					} else {
						String fieldName = convertFieldName(column);
						Field<Object> field = field(fieldName);
						if(fieldName.contains("*")) {
							String joinAlias = fieldName.split("\\.")[0];
							fields.add(table(joinAlias).asterisk());
							if(!joinAlias.equals("this")) {
								manyToOnes1.add(resource.getColumn(joinAlias, true));
							} else {
								for(OcColumn col : resource.getColumns()) {
									if (col.getRelationship() == OcColumn.Relationship.json) {
										jsonColumns.add(col.getName());
									}
								}
							}
						} else {
							if(!fieldName.contains("("))
								field = field.as(column);
							fields.add(field);
						}
					}
				}
			}
		}

		// select
		SelectJoinStep<Record> dslQuery;
		if (isQuery) { // query
			dslQuery = dsl.select(fields)
					.from(table(escapeSqlParams(query, command), extractSqlParams(query, context, command)).as("this"));
		} else if(resource.getParent() != null && resource.isJoinParent()) { // has parent
			Set<Field<Object>> subFields = new LinkedHashSet<Field<Object>>();
			Set<String> fieldNames = new HashSet<String>();
			fieldParent(resource, subFields, fieldNames);
			SelectJoinStep<Record> subQuery = dsl.select(subFields).from(table(table).as(resource.getName()));
			joinParent(resource, subQuery);
			dslQuery = dsl.select(fields).from(subQuery.asTable().as("this"));
		} else {  // table
			SelectSelectStep<Record> selectStep;
			if(fields.isEmpty())
				selectStep = dsl.select(table("this").asterisk());
			else
				selectStep = dsl.select(fields);
			dslQuery = selectStep.from(table(table).as("this"));
		}

		// prepare condition
		Condition[] conditions = null;
		if(condition != null) {
			conditions = buildCondition(resource, condition, false, manyToOnes1);
			dslQuery.where(conditions[0]);
		}
		
		// prepare order by
		SortField<Object>[][] sortFields = null;
		if (orderby != null) {
			sortFields = parseOrderbyParam(resource, orderby, manyToOnes1);
		} else if (command == null && resource.getOrderby() != null) {
			sortFields = parseOrderbyParam(resource, resource.getOrderby(), manyToOnes1);
		}
		if(sortFields != null)
			dslQuery.orderBy(sortFields[0]);
		
		// may_to_one 1
		for (OcColumn column : manyToOnes1) {
			OcResource columnResource = meta.getResource(column.getResource(), false);
			
			String pk = column.getPk() != null ? column.getPk() : getPk(columnResource);
			Condition on = field(convertFieldName(column.getFk())).eq(field(column.getName() + "." + pk));
			if (column.getWhere() != null)
				on.and(escapeSqlParams(column.getWhere(), command), extractSqlParams(column.getWhere(), context, command));

			dslQuery = dslQuery.leftJoin(table(columnResource.getTable()).as(column.getName())).on(on);
		}
		
		// sublize
		if(needSublize)
			dslQuery = dsl.select().from(dslQuery.asTable().as("this"));
		
		// where
		if (where != null) {
			if(where.startsWith("$")) {
				where = getSql(resource, resource.getCommand(where), condition);
			} else {
				where = templates.evaluate(where, resource, command, context).trim();
			}
			if (where.length() > 0)
				dslQuery.where(escapeSqlParams(where, command), extractSqlParams(where, context, command));
		}
		if (resource.getWhere() != null) {
			String resourceWhere = templates.evaluate(resource.getWhere(), resource, command, context).trim();
			if(Helper.notEmpty(resourceWhere))
				dslQuery.where(escapeSqlParams(resourceWhere, command), extractSqlParams(resourceWhere, context, command));
		}
		
		// condition
		if(condition != null) {
			OcCommand conditionCommand = resource.getCommand("condition");
			if(conditionCommand == null) {
				dslQuery.where(conditions[1]);
			} else {
				String conditionSql = getSql(resource, conditionCommand, condition);
				if(conditionSql.endsWith("and") || conditionSql.endsWith("AND"))
					conditionSql = conditionSql.substring(0, conditionSql.length() - 3);
				if(Helper.notEmpty(conditionSql))
					dslQuery.where(escapeSqlParams(conditionSql, command), extractSqlParams(conditionSql, condition, command));
			}
		}
		
		// count
		long total = 0;

		if ("count".equals(context.getCommand())) {
			total = dsl.fetchCount(dslQuery);
			return new OcResult(total);
		}

		// order by
		if(sortFields != null && sortFields[1].length > 0)
			dslQuery.orderBy(sortFields[1]);
		
		// paging
		if (limit > 0) {
			if(dsl.configuration().dialect().family() == SQLDialect.ORACLE 
					&& sortFields != null)
				dslQuery = dsl.select().from(dslQuery.asTable().as("this"));
			total = dsl.fetchCount(dslQuery);
			SelectOffsetStep<Record> offsetStep = dslQuery.limit(limit);
			if (offset > 0) {
				offsetStep.offset(offset);
			}
		}

		// fetch
		List<OcMap> rows = dslQuery.fetch(recordMapper);
		if (limit == 0)
			total = rows.size();

		// array
		for (String column : jsonColumns) {
			for (Map<String, Object> row : rows) {
				String value = (String) row.get(column);
				row.put(column, Helper.parseJson(value));
			}
		}
		
		// one_to_many
		for (OcColumn column : oneToManys) {
			for (Map<String, Object> row : rows) {
				OcResource target = meta.getResource(column.getResource());
				OcContext ctx = new OcContext();
				ctx.putAll(row);
				ctx.setSelect(column.getSelect());
				String pk = column.getPk();
				if(pk == null)
					pk = getPk(resource);
				String columnWhere = "this." + column.getFk() + " = :" + pk;
				if (column.getWhere() != null)
					columnWhere += " and " + column.getWhere();
				ctx.setWhere(columnWhere);
				ctx.setOrderby(column.getOrderby());
				List<?> result = select(target, null, ctx).getData();
				row.put(column.getName(), result);
			}
		}

		// commands
		for (OcColumn column : commandColumns) {
			for (Map<String, Object> row : rows) {
				OcContext ctx = new OcContext();
				ctx.setSelect(column.getSelect());
				ctx.setWhere(column.getWhere());
				ctx.setOrderby(column.getOrderby());
				for(String key : getPks(resource))
					ctx.put(key, row.get(key));
				ctx.setResource(resource.getName());
				ctx.setCommand(column.getName());
				Object result = commands.execute(ctx);
				if(result instanceof OcResult)
					result = ((OcResult)result).getData();
				row.put(column.getName(), result);
			}
		}		
		
		// may_to_one 2
		for (OcColumn column : manyToOnes2) {
			for (Map<String, Object> row : rows) {
				if (row.get(column.getFk()) == null)
					continue;
				OcResource target = meta.getResource(column.getResource());
				OcContext ctx = new OcContext();
				ctx.putAll(row);
				ctx.setSelect(column.getSelect());
				String columnWhere = "this." + getPk(target) + " = :" + column.getFk();
				if (column.getWhere() != null)
					columnWhere += " and " + column.getWhere();
				ctx.setWhere(columnWhere);
				List<?> data = select(target, null, ctx).getData();
				if (data.size() != 0)
					row.put(column.getName(), data.get(0));
			}
		}
		
		List<OcMap> resultRows = convertToBean(resource, rows);
		
		return new OcResult(resultRows, total);
	}

	private Condition[] buildCondition(OcResource resource, Map<String, Object> condition, boolean subQuery, Set<OcColumn> manyToOne) {
		return buildCondition(resource, condition, "and", subQuery, manyToOne);
	}

	@SuppressWarnings("unchecked")
	private Condition[] buildCondition(OcResource resource, Map<String, Object> condition, String logic,
			boolean needSubQuery, Set<OcColumn> manyToOne) {
		Condition pre = DSL.noCondition();
		Condition post = DSL.noCondition();
		Map<String, SelectConditionStep<Record1<Object>>> subQueries = new HashMap<String, SelectConditionStep<Record1<Object>>>();
		for (String field : condition.keySet()) {
			String operator = "=";
			Object value = condition.get(field);
			if(field.contains(" ") ) {
				String[] fields = field.trim().split("\\s+");
				field = fields[0];
				operator = fields[1];
			}
			if(field.contains("$")) {
				field = field.split("\\$")[0];
			}
			if (field.equals("and") || field.equals("or")) {
				Condition[] conditions = buildCondition(resource, (Map<String, Object>) value, field, needSubQuery, manyToOne);
				pre = addConditionOther(pre, conditions[0], logic);
				post = addConditionOther(post, conditions[1], logic);
			} else {
				if(field.contains("?")) {
					field = field.split("\\?")[0];
					if(!existsColumn(resource, field))
						continue;
				}
				field = convertFieldName(field);
				Condition other = null;
				if (value instanceof List) {
					if(operator.equals("=")|| operator.equals("eq") || operator.equals("in")) {
						other = field(field).in(value);
					} else if(operator.equals("<>") || operator.equals("!=")) {
						other = field(field).notIn(value);
					}
				} else {
					value = convertValue(value);
					if(operator.equals("=") || operator.equals("eq")) {
						if(value == null)
							other = field(field).isNull();
						else
							other = field(field).eq(value);
					} else if(operator.equals("eqi")) {
						other = field(field).equalIgnoreCase((String) value);
					} else if(operator.equals("<>") || operator.equals("!=")) {
						if(value == null)
							other = field(field).isNotNull();
						else
							other = field(field).notEqual(value);
					} else if(operator.equals("<")) {
						other = field(field).lessThan(value);
					} else if(operator.equals("<=")) {
						other = field(field).lessOrEqual(value);
					} else if(operator.equals(">")) {
						other = field(field).greaterThan(value);
					} else if(operator.equals(">=")) {
						other = field(field).greaterOrEqual(value);
					} else if(operator.equals("notcontains")) {
						other = field(field).notContains(value);
					} else if(operator.equals("notcontainsi")) {
						other = field(field).notContainsIgnoreCase(value);
					} else if(operator.equals("contains")) {
						other = field(field).contains(value);
					} else if(operator.equals("containsi")) {
						other = field(field).containsIgnoreCase(value);
					} else if(operator.equals("startswith")) {
						other = field(field).startsWith(value);
					} else if(operator.equals("endswith")) {
						other = field(field).endsWith(value);
					} else if(operator.equals("like")) {
						other = field(field).like((String)value);
					} else if(operator.equals("notlike")) {
						other = field(field).notLike((String)value);
					} else if(operator.equals("between")) {
						List<String> values = (List<String>)value;
						other = field(field).between(values.get(0), values.get(1));
					} else {
						throw new OccamException("operator is not supported operator", operator);
					}
				}
				String[] fieldArr = field.split("\\.");
				if(StringUtils.isNumeric(field)) {
					pre = addConditionOther(pre, other, logic);
				} else if (!field.startsWith("this.")) {
					OcColumn rColumn = resource.getColumn(fieldArr[0], true);
					if(rColumn.getSql() != null) {
						
					}
					OcResource rResource = meta.getResource(rColumn.getResource());
					if (manyToOne != null)
						manyToOne.add(rColumn);
					if(needSubQuery) {
						SelectConditionStep<Record1<Object>> subQuery = subQueries.get(rColumn.getFk());
						if (subQuery == null) {
							subQueries.put(rColumn.getFk(), DSL.select(field(rResource.getPk()))
									.from(table(rResource.getTable()).as(fieldArr[0]))
									.where(field(rResource.getPk()).eq(field("this." + rColumn.getFk())).and(other)));
						} else {
							subQuery = subQuery.and(other);
						}
					} else {
						pre = addConditionOther(pre, other, logic);
					}
				} else {
					OcColumn col = resource.getColumn(fieldArr[1]);
					if(col != null && col.getSql() != null)
						post = addConditionOther(post, other, logic);
					else
						pre = addConditionOther(pre, other, logic);
				}
			}
		}
		for (String field : subQueries.keySet()) {
			SelectConditionStep<Record1<Object>> subQuery = subQueries.get(field);
			Condition other = field("this." + field).eq(subQuery);
			pre = addConditionOther(pre, other, logic);
		}
		return new Condition[] {pre, post};
	}

	private Condition addConditionOther(Condition dsl, Condition other, String logic) {
		if (logic.equals("and")) {
			dsl = dsl.and(other);
		} else if (logic.equals("or")) {
			dsl = dsl.or(other);
		}
		return dsl;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<OcMap> convertToBean(OcResource resource, List<? extends Map<String, Object>> rows) {
		List resultRows;
		if(resource.getBean() == null) {
			resultRows = rows;
		} else {
			try {
				resultRows = Helper.toBean(Class.forName(resource.getBean()), rows);
			} catch (ClassNotFoundException e) {
				throw new OccamException(e);
			}
		}
		return resultRows;
	}

	private void fieldParent(OcResource resource, Set<Field<Object>> subFields, Set<String> fieldNames) {
		if (resource == null)
			return;
		for (DbColumn column : getDbColumns(resource)) {
			if (fieldNames.contains(column.getColumnName()))
				continue;
			subFields.add(field(resource.getName() + "." + column).as(column.getColumnName()));
			fieldNames.add(column.getColumnName());
		}
		fieldParent(meta.getResource(resource.getParent()), subFields, fieldNames);
	}

	private void joinParent(OcResource resource, SelectJoinStep<Record> dslQuery) {
		if(resource.getParent() == null)
			return;
		OcResource parent = meta.getResource(resource.getParent());

		Condition on = field(resource.getName() + "." + getPk(resource)).eq(field(parent.getName() + "." + getPk(parent)));
		dslQuery = dslQuery.leftJoin(table(parent.getTable()).as(parent.getName())).on(on);
		
		joinParent(parent, dslQuery);
	}

	public String insert(OcResource resource, Map<String, Object> row) {
		return insert(resource, Lists.newArrayList(row)).get(0);
	}

	public List<String> insert(OcResource resource, List<? extends Map<String, Object>> rows) {
		if(resource.getParent() != null) {
			OcResource parentResource = meta.getResource(resource.getParent());
			if(!parentResource.getTable().equals(resource.getTable()))
				insert(parentResource, rows);
		}
		
		DSLContext dsl = dsl(resource, null);
		ArrayList<Query> queries = new ArrayList<>();
		List<String> result = new LinkedList<String>();
		
		for (Map<String, Object> row : rows) {
			String pk = getPk(resource);
			String id = null;
			if (pk != null) {
				id = String.valueOf(row.get(pk));
				if (id == null || "".equals(id) || "null".equals(id)) {
					id = generatePk(resource);
					row.put(pk, id);
				}
			}
			result.add(id);

			InsertSetStep<Record> step = dsl.insertInto(table(resource.getTable()));
			InsertSetMoreStep<Record> build = null;
			for (Entry<String, Object> entry : row.entrySet()) {
				if (entry.getKey().contains(COLUMN_SEPARATOR) && !entry.getKey().startsWith(COLUMN_SEPARATOR)) {
					String[] splitedKey = entry.getKey().split(COLUMN_SEPARATOR);
					OcColumn column = resource.getColumn(splitedKey[0], true);
					OcResource targetResource = meta.getResource(column.getResource());
					if (splitedKey[1].equals(getPk(targetResource)) && existsColumn(resource, column.getFk()))
						build = step.set(field(column.getFk()), convertValue(entry.getValue()));
				} else {
					OcColumn column = resource.getColumn(entry.getKey());
					if (column != null) {
						if(column.getRelationship() == OcColumn.Relationship.json) {
							build = step.set(field(entry.getKey()), Helper.toJson(entry.getValue()));
						} else {
							saveRelation(resource, m(p(pk, id)), entry, column);
						}
					} else {
						if(getDbColumns(resource).contains(new DbColumn(entry.getKey())))
							build = step.set(field(entry.getKey()), convertValue(entry.getValue()));
					}
				}
			}
			queries.add(build);
		}

		if(queries.size() < 100) {
			for (Query query : queries) {
				dsl.execute(query);
			}
		} else {
			dsl.batch(queries).execute();
		}
		return result;
	}
	
	private Object convertValue(Object value) {
		if(value == null) {
			return null;
		} if(value instanceof String) {
			String str = (String) value;
			if (str.indexOf("T") == 10) {
				try {
					return new java.sql.Timestamp(new DateTime(str).toDate().getTime());
				} catch (IllegalArgumentException e) {
					return value;
				}
			} else {
				return value;
			}
		} else if(value instanceof java.util.Date) {
			return new java.sql.Timestamp(((java.util.Date)value).getTime());
		} else if(value instanceof ScriptObjectMirror) {
			ScriptObjectMirror mirror = (ScriptObjectMirror) value;
			if(mirror.hasMember("getTime")) {
				long timestampLocalTime = (long) (double) mirror.callMember("getTime"); 
				return new java.sql.Timestamp(timestampLocalTime);
			} else {
				return value;
			}
		} else if(value instanceof List || value.getClass().isArray()) {
			return Helper.toJson(value);
		} else {
			return value;
		}
	}
	
	@SuppressWarnings("unchecked")
	public int update(OcResource resource, Object ids, Map<String, Object> condition, Map<String, Object> row) {
		if(condition == null)
			condition = new HashMap<String, Object>();
		if(ids == null) {
			for(String pk : getPks(resource)) {
				if(row.get(pk) != null)
					condition.put(pk, row.get(pk));
			}
			return update(resource, condition, row);
		} else if(ids instanceof List) {
			int result = 0;
			for(Object id : (List<?>)ids) {
				condition.putAll(buildPkMap(resource, id.toString()));
				result += update(resource, condition, row);
			}
			return result;
		} else if(ids instanceof Map) {
			condition.putAll((Map<String, Object>)ids);
			return update(resource, condition, row);
		} else {
			condition.putAll(buildPkMap(resource, ids.toString()));
			return update(resource, condition, row);
		}
	}
	
	public int update(OcResource resource, Map<String, Object> condition, Map<String, Object> row) {
		if(condition == null || condition.size() == 0)
			throw new OccamException("condition can not be empty for update");
		
		if(resource.getParent() != null) {
			OcResource parentResource = meta.getResource(resource.getParent());
			if(!parentResource.getTable().equals(resource.getTable()))
				update(meta.getResource(resource.getParent()), condition, row);			
		}
		
		DSLContext dsl = dsl(resource, null);
		UpdateSetFirstStep<Record> step = dsl.update(table(resource.getTable()).as("this"));
		UpdateSetMoreStep<Record> build = null;
		if(row == null)
			throw new OccamException("update row can't be null");
		for (Entry<String, Object> entry : row.entrySet()) {
			if (entry.getKey().contains(COLUMN_SEPARATOR) && !entry.getKey().startsWith(COLUMN_SEPARATOR)) {
				String[] splitedKey = entry.getKey().split(COLUMN_SEPARATOR);
				OcColumn column = resource.getColumn(splitedKey[0], true);
				OcResource targetResource = meta.getResource(column.getResource());
				if (splitedKey[1].equals(getPk(targetResource)) && existsColumn(resource, column.getFk()))
					build = step.set(field(column.getFk()), convertValue(entry.getValue()));
			} else {
				OcColumn column = resource.getColumn(entry.getKey());
				if (column != null) {
					if(column.getRelationship() == OcColumn.Relationship.json) {
						build = step.set(field(entry.getKey()), Helper.toJson(entry.getValue()));
					} else {
						saveRelation(resource, condition, entry, column);
					}
				} else {
					if(existsColumn(resource, entry.getKey()))
						build = step.set(field(entry.getKey()), convertValue(entry.getValue()));
				}
			}
		}
		
		if(build == null)
			return 0;
		UpdateConditionStep<Record> query = build.where(buildCondition(resource, condition, true, null));
		return query.execute();
	}

	@SuppressWarnings("unchecked")
	private void saveRelation(OcResource resource, Map<String, Object> ids, Entry<String, Object> entry, OcColumn column) {
		if(column.getResource() == null)
			return;
		OcResource targetResource = meta.getResource(column.getResource());

		if (column.getRelationship() == OcColumn.Relationship.many_to_one) {
			save(targetResource, (Map<String, Object>) entry.getValue());
		} else if (column.getRelationship() == OcColumn.Relationship.one_to_many) {
			delete(targetResource, m(p(column.getFk(), ids.get(getPk(resource)))));
			List<Map<String, Object>> rows = (List<Map<String, Object>>) entry.getValue();
			for (Map<String, Object> row : rows) {
				// TODO composite key 지원
				row.put(column.getFk(), ids.get(getPk(resource)));
			}
			insert(targetResource, rows);
		}
	}
	
	@SuppressWarnings("unchecked")
	public int delete(OcResource resource, Object ids, Map<String, Object> condition) {
		Map<String, Object> newCondition = new HashMap<String, Object>();
		if(condition != null)
			newCondition.putAll(condition);
		if(ids instanceof List) {
			int result = 0;
			for(Object id : (List<?>) ids) {
				if(id instanceof Map) {
					newCondition.putAll((Map<String, Object>) id);
					result += delete(resource, newCondition);
				} else {
					newCondition.putAll(buildPkMap(resource, id.toString()));
					result += delete(resource, newCondition);
				}
			}
			return result;
		} else if(ids instanceof Map) {
			newCondition.putAll((Map<String, Object>) ids);
			return delete(resource, newCondition);
		} else {
			if(ids != null)
				newCondition.putAll(buildPkMap(resource, ids.toString()));
			return delete(resource, newCondition);
		}
	}
	
	public int delete(OcResource resource, Map<String, Object> condition) {
		if(condition == null || condition.size() == 0)
			throw new OccamException("condition can not be empty for delete");
		
		DSLContext dsl = dsl(resource, null);
		
		DeleteConditionStep<Record> query = dsl.delete(table(resource.getTable()).as("this")).where(buildCondition(resource, condition, true, null));
		int result = query.execute();
		if(resource.getParent() != null) {
			OcResource parentResource = meta.getResource(resource.getParent());
			if(!parentResource.getTable().equals(resource.getTable()))
				delete(meta.getResource(resource.getParent()), condition);
		}
		return result;
	}
	
	public void save(OcResource resource, List<? extends Map<String, Object>> rows) {
		for (Map<String, Object> row : rows) {
			save(resource, row);
		}
	}
	
	private static final String _OPERATION_KEY = "__operation";
	private static final String _ID_KEY = "__id";

	public void save(OcResource resource, Map<String, Object> row) {
		String operation = (String) row.get(_OPERATION_KEY);
		row.remove(_OPERATION_KEY);
		Object id = row.get(_ID_KEY);
		row.remove(_ID_KEY);
		
		if(id == null) {
			Map<String, Object> idMap = new HashMap<>();
			List<String> pks = getPks(resource);
			for (String key : pks) {
				idMap.put(key, row.get(key));
			}
			if(idMap.size() != 0)
				id = idMap;
		}
		
		if (operation != null && "DELETE".equals(operation)) {
			delete(resource, id, null);
		} else if (id != null) {
			if(update(resource, id, null, row) == 0)
				insert(resource, row);
		} else {
			insert(resource, row);
		}
	}

	private String generatePk(OcResource ocSql) {
		String pkGenerator = ocSql.getPkGenerator();
		if ("uuid".equals(pkGenerator)) {
			return Helper.getUuid();
		} else if("rand16c".equals(pkGenerator)) {
			return Helper.getRand16c();
		} else {
			throw new OccamException("%s is an unsupported pkGenerator.", pkGenerator);
		}
	}

	private RecordMapper<Record, OcMap> recordMapper = new RecordMapper<Record, OcMap>() {
		@Override
		public OcMap map(Record record) {
			OcMap map = new OcMap();
			Field<?>[] fields = record.fields();
			int size = fields.length;
			for (int i = 0; i < size; i++) {
				Field<?> field = fields[i];
				String fieldName = field.getName().toLowerCase().replaceAll("\"", "");
				Object value = record.getValue(i);
				if (value instanceof java.util.Date && !value.getClass().equals(java.util.Date.class)) {
					value = new Date(((java.util.Date) value).getTime());
				} else if(value instanceof oracle.sql.TIMESTAMP) {
					oracle.sql.TIMESTAMP timestamp = (oracle.sql.TIMESTAMP) value;
					try {
						value = TIMESTAMP.toDate(timestamp.getBytes());
					} catch (SQLException e) {
						throw new OccamException(e);
					}
				}
				map.put(fieldName, value);
			}
			return map;
		}
	};

	public DSLContext dsl(OcResource resource, OcCommand command) {
		if(command != null && command.getDatasource() != null) {
			return dsl(command.getDatasource());
		} else {
			return dsl(resource.getDatasource());
		}
	}
	
	public DSLContext dsl(String datasourceName) {
		return DSL.using(getConfiguration(datasourceName));
	}

	@Autowired
	private TransactionProvider transactionProvider;

	public synchronized DefaultConfiguration getConfiguration(String datasourceName) {
		OcDatasource datasource = meta.getDatasource(datasourceName);
		if (datasource == null)
			throw new OccamException("Datasource %s not found.", datasourceName);

		DefaultConfiguration configuration = configurations.get(datasource.getName());
		if (configuration == null) {
			DataSource txDataSource = buildDataSource(datasource);
			DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(txDataSource);
			PlatformTransactionManager oldTm = transactionManagers.get(datasource.getName());
			if(oldTm != null) {
				transactionManagers.put(datasource.getName()+"__old", oldTm);
			}
			transactionManagers.put(datasource.getName(), transactionManager);
			
			configuration = new DefaultConfiguration();
			
			Settings settings = new Settings();
			settings.setRenderNameStyle(RenderNameStyle.AS_IS);
			configuration.setSettings(settings);
			
			configuration.setRecordMapperProvider(new RecordMapperProvider() {

				@SuppressWarnings("unchecked")
				@Override
				public <R extends Record, E> RecordMapper<R, E> provide(RecordType<R> recordType,
						Class<? extends E> type) {
					return (RecordMapper<R, E>) recordMapper;
				}
			});
			configuration.setDataSource(txDataSource);
			configuration.setSQLDialect(SQLDialect.valueOf(datasource.getDialect()));
			configuration.setTransactionProvider(transactionProvider);
			configuration.setExecuteListener(new DefaultExecuteListener () {
				private static final long serialVersionUID = 2494016765931766472L;

				@Override
				public void exception(ExecuteContext ctx) {
					SQLException e = ctx.sqlException();
					if(e == null) {
						throw new OccamException(ctx.exception(), "알 수 없는 데이터베이스 오류입니다.");
					} else if("23000".equals(e.getSQLState())) {
						if(e.getErrorCode() == 1) {
							// ORA-00001
							throw new OccamException(e, "데이터가 중복으로 입력되었습니다.");
						} else if(e.getErrorCode() == 2292) {
							// ORA-02292: 무결성 제약조건이 위배되었습니다- 자식 레코드가 발견되었습니다
							throw new OccamException(e, "관련된 데이터가 있습니다.");
						} else {
							throw new OccamException(e, "무결성 제약조건에 위배됩니다.");
						}
					} else if("72000".equals(e.getSQLState())) {
						if(e.getErrorCode() == 1461) {
							// ORA-01461: LONG 값은 LONG 열에 삽입할 때만 바인드할 수 있습니다.
							throw new OccamException(e, "입력된 문자열이 너무 깁니다.");
						} else if(e.getErrorCode() == 1407) {
							//  ORA-01407: NULL로 업데이트할 수 없습니다
							throw new OccamException(e, "필수 입력 값이 누락되었습니다.");
						}
					} else if(e.getSQLState() == null) {
						if(e.getErrorCode() == 17081) {
							throw new OccamException(e, "데이터 일괄 입력 중 오류가 발생했습니다.");
						}
					}
					throw new OccamException(e, "알 수 없는 데이터베이스 오류입니다.");
				}
			});
			
			configurations.put(datasource.getName(), configuration);
		}
		return configuration;
	}

	public DataSource buildDataSource(OcDatasource datasource) {
		if (datasource.getDialect() == null)
			datasource.setDialect(JDBCUtils.dialect(datasource.getUrl()).name());
		if (datasource.getDriver() == null)
			datasource.setDriver(JDBCUtils.driver(datasource.getUrl()));
		
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(datasource.getUrl());
		config.setUsername(datasource.getUsername());
		config.setPassword(datasource.getPassword());
		config.setDriverClassName(datasource.getDriver());
		config.setConnectionInitSql(datasource.getInitSql());
		config.setAutoCommit(false);
		config.setLeakDetectionThreshold(60000);
		config.setMaximumPoolSize(20);
		config.setMinimumIdle(0);
		config.setInitializationFailTimeout(-1);
		
		DataSource txDataSource = new TransactionAwareDataSourceProxy(
				new LazyConnectionDataSourceProxy(new HikariDataSource(config)));
		return txDataSource;
	}
	
	public void resetDatasource(String datasourceName) {
		configurations.remove(datasourceName);
	}
	
	private static final Pattern paramsPattern1 = Pattern.compile("(?<=\\W)'[^\\\\']*'(?=\\W)|(?<=\\W)\\d+(?=\\W)|(:\\w+)");
	

	private static final Pattern paramsPattern2 = Pattern.compile("(:\\w+)");

	
	/** binding parameter ? 로 치환 */
	private String escapeSqlParams(String sql, OcCommand command) {
		Matcher matcher = (command != null && command.getNoParameterize() != null && command.getNoParameterize() ? paramsPattern2 : paramsPattern1).matcher(sql+ " ");
		return matcher.replaceAll("?");
	}
	
	/** binding parameter 추출  
	 * @param boolean1 */
	private Object[] extractSqlParams(String sql, Map<String, Object> paramMap, OcCommand command) {
		Matcher matcher = (command != null && command.getNoParameterize() != null && command.getNoParameterize() ? paramsPattern2 : paramsPattern1).matcher(sql+ " ");
		List<Object> allMatches = new ArrayList<Object>();
		while (matcher.find()) {
			String match = matcher.group();
			if (match.startsWith("'")) {
				allMatches.add(convertValue(match.substring(1, match.length() - 1)));
			} else if (match.startsWith(":")) {
				allMatches.add(convertValue(paramMap.get(match.substring(1))));
			} else {
				try {
					allMatches.add(Long.parseLong(match));
				} catch (Exception e) {
					allMatches.add(Double.parseDouble(match));
				}
			}
		}
		return allMatches.toArray();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private SortField<Object>[][] parseOrderbyParam(OcResource resource, final String orderby, Set<OcColumn> manyToOnes1) {
		if (orderby.equals("")) {
			return new SortField[][] {};
		} else {
			String[] parts = orderby.split(",");
			List<SortField> pre = new ArrayList<SortField>();
			List<SortField> post = new ArrayList<SortField>();
			for (String part : parts) {
				if(part.trim().isEmpty())
					continue;
				String[] subparts = part.trim().split(" ");
				String field = convertFieldName(subparts[0]);
				String[] fieldArr = field.split("\\.");
				if(!fieldArr[0].equals("this")) {
					OcColumn column = resource.getColumn(fieldArr[0]);
					if(column != null)
						manyToOnes1.add(column);
				}
				SortField<Object> sortField = null;
				if (subparts.length == 1) {
					sortField = field(field).asc();
				} else if (subparts.length == 2) {
					if (subparts[1].equalsIgnoreCase("ASC")) {
						sortField = field(field).asc();
					} else {
						sortField = field(field).desc();
					}
				}
				
				if(fieldArr[0].equals("this")) {
					OcColumn col = resource.getColumn(fieldArr[1]);
					if(col != null && col.getSql() != null) {
						post.add(sortField);
					} else {
						pre.add(sortField);
					}
				} else {
					pre.add(sortField);
				}

			}
			return new SortField[][] { pre.toArray(new SortField[] {}), post.toArray(new SortField[] {}) };
		}
	}

	private String convertFieldName(String fieldName) {
		if(StringUtils.countMatches(fieldName, ".") != 1)
			fieldName = Helper.replaceLast(fieldName, COLUMN_SEPARATOR, ".");
		if(!fieldName.contains(".") && !fieldName.contains("(") && !StringUtils.isNumeric(fieldName))
			fieldName = "this." + fieldName;
		return fieldName;
	};
	
	private Connection getConnection(OcResource resource) {
		DSLContext dsl = dsl(resource, null);
		return dsl.parsingConnection();
	}
	
	public String getPk(OcResource resource) {
		List<String> pks = getPks(resource);
		if(pks.size() > 0)
			return pks.get(0);
		else
			return null;
	}
	
	public List<String> getPks(OcResource resource) {
		if (resource.getPks() != null && resource.getPks().size() > 0)
			return resource.getPks();
		Connection conn = getConnection(resource);
		OcDatasource datasource = meta.getDatasource(resource.getDatasource());
		List<String> columns = new ArrayList<String>();
		try {
			DatabaseMetaData dmd = conn.getMetaData();
			String schema, table;
			if (dmd.storesUpperCaseIdentifiers()) {
				schema = datasource.getUsername().toUpperCase();
				table = resource.getTable().toUpperCase();
			} else if (dmd.storesLowerCaseQuotedIdentifiers()) {
				schema = datasource.getUsername().toLowerCase();
				table = resource.getTable().toLowerCase();
			} else {
				schema = datasource.getUsername();
				table = resource.getTable();
			}
			ResultSet resultSet = dmd.getPrimaryKeys(null, schema, table);
			while (resultSet.next()) {
				String columnName = resultSet.getString("COLUMN_NAME").toLowerCase();
				if(Helper.notEmpty(columnName))
					columns.add(columnName);
			}
			resource.setPks(columns);
			return columns;
		} catch (Exception e) {
			throw new OccamException(e);
		} finally {
			JdbcUtils.closeConnection(conn);
		}
	}
	
	public boolean existsColumn(OcResource resource, String columnName) {
		columnName = columnName.replace("this.", "").toLowerCase();
		return getDbColumns(resource).contains(new DbColumn(columnName));
	}
	
	public Set<DbColumn> getDbColumns(OcResource resource) {
		if (resource.getDbColumns() != null)
			return resource.getDbColumns();
		Set<DbColumn> columns = new LinkedHashSet<DbColumn>();

		Connection conn = getConnection(resource);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement("select * from " + resource.getTable() + " where 1 = 2");
			rs = stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			for (int i=1; i <= rsmd.getColumnCount(); i++) {
				DbColumn dbColumn = new DbColumn();
				dbColumn.setColumnName(rsmd.getColumnName(i).toLowerCase());
				dbColumn.setColumnType(rsmd.getColumnType(i));
				dbColumn.setColumnTypeName(rsmd.getColumnTypeName(i));
				dbColumn.setColumnClassName(rsmd.getColumnClassName(i));
				dbColumn.setColumnLabel(rsmd.getColumnLabel(i));
				dbColumn.setColumnDisplaySize(rsmd.getColumnDisplaySize(i));
				dbColumn.setSchemaName(rsmd.getSchemaName(i));
				dbColumn.setTableName(rsmd.getTableName(i));
				dbColumn.setCatalogName(rsmd.getCatalogName(i));
				dbColumn.setNullable(rsmd.isNullable(i));
				dbColumn.setPrecision(rsmd.getPrecision(i));
				dbColumn.setScale(rsmd.getScale(i));
				columns.add(dbColumn);
			}
			resource.setDbColumns(columns);
			return columns;
		} catch (Exception e) {
			throw new OccamException(e);
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(stmt);
			JdbcUtils.closeConnection(conn);
		}
	}
	
	public List<OcMap> getDbTables(String datasourceName) {
		OcDatasource datasource = meta.getDatasource(datasourceName);
		DSLContext dsl = dsl(datasourceName);
		Connection conn = dsl.parsingConnection();
		try {
			DatabaseMetaData dmd = conn.getMetaData();
			String schema = null;
			if (dmd.storesUpperCaseIdentifiers())
				schema = datasource.getUsername().toUpperCase();
			else if (dmd.storesLowerCaseQuotedIdentifiers())
				schema = datasource.getUsername().toLowerCase();
			else
				schema = datasource.getUsername();

			ResultSet rs = dmd.getTables(null, schema, "%", new String[] { "TABLE", "VIEW" });
			List<OcMap> list = new ArrayList<>();
			while (rs.next()) {
				OcMap table = new OcMap();
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					table.put(rsmd.getColumnName(i).toLowerCase(), rs.getObject(rsmd.getColumnName(i)));
				}
				list.add(table);
			}
			return list;
		} catch(Exception e) {
			throw new OccamException(e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void createDb(OcResource resource) {
		DSLContext dsl = dsl(resource, null);
		if(resource.getTable() == null)
			return;
		CreateTableColumnStep q = dsl.createTable(resource.getTable()).columns();
		for (OcColumn column : resource.getColumns()) {
			DataType dataType;
			try {
				dataType = (DataType) SQLDataType.class.getField(column.getDataType()).get(null);
			} catch (Exception e) {
				throw new OccamException(e);
			}
			if(column.getLength() != null)
				dataType = dataType.length(column.getLength());
			q = q.column(column.getName(), dataType);			
		}
		q.execute();
	}
	
	@SuppressWarnings("unchecked")
	public void loadData(OcResource resource) {
		String data = resource.getData();
		if(data == null)
			return;
		Map<String, Object> rows = (Map<String, Object>) scripts.eval(data, null);		
		for (Object row : rows.values()) {
			save(resource, (Map<String, Object>) row);
		}
	}	
	
	public String getColumnDataType(List<Map<String,Object>> list , String columnName) {
		for(Map<String, Object> row : list) {
			if(columnName.equals(row.get("COLUMN_NAME").toString())) {
				return (String) row.get("DATA_TYPE");
			}
		}
		return null;
	}
	
	public void exportCSV(String datasource, String table, String filePath) {
		DSLContext dsl = dsl(datasource);
		Cursor<Record> reslt = dsl.select().from(table).fetchLazy();
		FileWriter out = null;
		try {
			out = new FileWriter(filePath);
			reslt.formatCSV(out);
		} catch (IOException | java.io.IOException e) {
			throw new OccamException(e);
		} finally {
			try {
				out.close();
			} catch (java.io.IOException e) {
			}
		}
	}
	
	public void importCSV(String datasource, String table, String filePath) {
		DSLContext dsl = dsl(datasource);
		File csvFile = new File(filePath);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(csvFile));
			String[] fieldNames = in.readLine().split(",");
			ArrayList<Field<?>> fields = new ArrayList<Field<?>>();
			for (String fieldName : fieldNames) {
				fields.add(field(fieldName));
			}
			dsl.loadInto(table(table)).loadCSV(csvFile).fields(fields).execute();
		} catch (IOException | java.io.IOException e) {
			throw new OccamException(e);
		} finally {
			try {
				in.close();
			} catch (java.io.IOException e) {
			}
		}
	}
	
}
