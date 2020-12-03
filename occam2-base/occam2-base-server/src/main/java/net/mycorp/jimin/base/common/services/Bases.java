package net.mycorp.jimin.base.common.services;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.auth.OcUser;
import net.mycorp.jimin.base.auth.services.Acls;
import net.mycorp.jimin.base.auth.services.AuthService;
import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcColumn;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResult;
import net.mycorp.jimin.base.service.BaseService;

@Service
public class Bases extends BaseService {

	@Autowired
    private CacheManager cacheManager;
	
	@Autowired
	protected AuthService auths;

	@Autowired
	protected Acls acls;

	@Override
	public void preInsert(OcContext ctx) {
		OcMap row = ctx.getRow();
		OcUser user = auths.getUser();
		if (user != null) {
			row.putIfNull("owner_id", user.getId());
			row.putIfNull("group_id", user.get("group_id"));
			row.putIfNull("reg_user_id", user.getId());
			if("default".equals(user.get("tenant_id")))
				row.putIfNull("tenant_id", user.get("tenant_id"));
			else
				row.put("tenant_id", user.get("tenant_id"));
		}
		row.putIfNull("reg_date", new Date());
		super.preInsert(ctx);
	}

	@Override
	public void preUpdate(OcContext ctx) {
		OcMap row = ctx.getRow();
		OcUser user = auths.getUser();
		if (user != null)
			row.putIfNull("mod_user_id", user.getId());
		row.putIfNull("mod_date", new Date());
		super.preUpdate(ctx);
	}
	
	@Override
	public void preSave(OcContext ctx) {
		OcMap row = ctx.getRow();
		if(row.containsKey("parent_id") && row.get("parent_id") == null) {
			throw new OccamException("parent_id can not be null");
		}		
		if(row.get("parent_id") != null && row.get("parent_id").equals(row.id())) {
			throw new OccamException("parent_id and id can not be same");
		}
		super.preSave(ctx);
	}

	public int delete(String id, boolean skipPermit) {
		return delete(ctx().id(id).skipPermit(skipPermit));
	}

	@Override
	public int delete(OcContext ctx) {
		if (!ctx.isSkipPermit())
			filterPermit(Permit.DELETE, ctx.getCondition());
		int result = super.delete(ctx);
		if(!ctx.isSkipPermit() && result == 0) {
			// throw new OccamException("권한이 부족하거나 데이터가 없어 삭제되지 않았습니다.");
			return 0;
		} else {
			return result;
		}		
	}
	
	public void deleteEach(OcContext ctx) {
		List<OcMap> list = select(ctx.select("id")).getData();
		for (OcMap row : list) {
			delete(row.id(), true);
		}
	}
	
	public List<String> insert(OcContext ctx) {
		preInsert(ctx);
		if (!ctx.isSkipPermit())
			checkPermit(Permit.WRITE, ctx.getRow());
		List<String> result = datas.insert(getResource(ctx), ctx.getRows());
		ctx.setId(result);
		postInsert(ctx);
		return result;
	}

	public int update(OcContext ctx) {
		if (!ctx.isSkipPermit())
			filterPermit(Permit.WRITE, ctx.getCondition());
		int result = super.update(ctx);
		if(!ctx.isSkipPermit() && result == 0) {
			// throw new OccamException("권한이 부족하거나 데이터가 없어 저장되지 않았습니다.");
			return 0;
		} else {
			return result;
		}
	}

	public OcMap get(String id, boolean skipPermit) {
		OcContext ctx = ctx().id(id).skipPermit(skipPermit);
		if (!ctx.isSkipPermit())
			filterPermit(Permit.READ, ctx.getCondition());
		return super.get(ctx);
	}
	
	@Override
	public OcMap get(OcContext ctx) {
		if (!ctx.isSkipPermit())
			filterPermit(Permit.READ, ctx.getCondition());
		if(!getResource().isCacheable())
			return super.get(ctx);
		Cache cache = cacheManager.getCache(resourceName);
		String key = ctx.toString();
		ValueWrapper value = cache.get(key);
		if (value == null) {
			OcMap result = super.get(ctx);
			cache.put(key, result);
			return result;
		} else {
			return (OcMap) value.get();
		}
	}

	public OcResult select(OcContext ctx) {
		if (!ctx.isSkipPermit())
			filterPermit(Permit.READ, ctx.getCondition());
		return super.select(ctx);
	}
	
	@Override
	public long count(OcContext ctx) {
		if (!ctx.isSkipPermit())
			filterPermit(Permit.READ, ctx.getCondition());
		return super.count(ctx);
	}

	private void filterPermit(Permit permit, Map<String, Object> condition) {
		String partOf = getResource().getPartOf();
		OcMap ace = getAce(partOf);
		if (ace == null)
			return;
		if (permit.getValue() <= ace.getInt("guest_permit"))
			return;
		OcUser user = auths.getUser();
		if (user == null)
			throw new OccamException("권한이 부족합니다.");
		String tenantId = user.getString("tenant_id");
		if (tenantId != null && !"default".equals(tenantId)) {
			String field = "tenant_id";
			if (partOf != null)
				field = partOf + "__" + field;
			OcMap tenantFilter = new OcMap();
			tenantFilter.put(field+"?1", tenantId);
			tenantFilter.put(field+"?2", "common");
			condition.put("or", tenantFilter);
		}
		if (permit.getValue() <= ace.getInt("other_permit"))
			return;
		if (permit.getValue() <= ace.getInt("admin_permit") && user.isAdmin())
			return;
		OcMap filter = new OcMap();
		if (permit.getValue() <= ace.getInt("group_permit")) {
			String field = "group_id";
			if (partOf != null)
				field = partOf + "__" + field;
			filter.put(field, user.getGroupIds());
		}
		if (permit.getValue() <= ace.getInt("owner_permit")) {
			String field = "owner_id";
			if (partOf != null)
				field = partOf + "__" + field;
			filter.put(field, user.getId());
		}
		if (permit.getValue() <= ace.getInt("owner_group_permit")) {
			// TODO partOf의 경우 2단계 이상 depth의 condition field가 있는 경우 join이 제대로 안되어서 에러 (eg. 자산점검 > 점검 조치)
			// if (partOf != null)
			//	field = partOf + "__" + field;
			if (partOf != null) {
				filter.put("1", 1);
			} else {
				String field = "owner__group_id";
				filter.put(field, user.getGroupIds());
			}
		}
		if(filter.size() == 0) {
			throw new OccamException("권한이 부족합니다.");
		}
		condition.put("or$acl", filter);
	}

	private void checkPermit(Permit permit, Map<String, Object> row) {
		String partOf = getResource().getPartOf();
		OcMap ace = getAce(partOf);
		if (ace == null)
			return;
		if (partOf != null) {
			OcColumn partOfCol = getResource().getColumn(partOf, true);
			row = get(resource(partOfCol.getResource()).id(row.get(partOfCol.getFk())));
		}
		if (permit.getValue() <= ace.getInt("guest_permit"))
			return;
		OcUser user = auths.getUser();
		if (user == null)
			throw new OccamException("권한이 부족합니다.");
		if (permit.getValue() <= ace.getInt("other_permit")) {
			return;
		} else if (permit.getValue() <= ace.getInt("group_permit") && row.get("group_id") != null
				&& user.getGroupIds().contains(row.get("group_id"))) {
			return;
		} else if (permit.getValue() <= ace.getInt("owner_permit") && row.get("owner_id") != null
				&& user.getId().equals(row.get("owner_id"))) {
			return;
		} else if (permit.getValue() <= ace.getInt("admin_permit") && user.isAdmin()) {
			return;
		} else {
			throw new OccamException("권한이 부족합니다.");
		}

	}

	private OcMap getAce(String partOf) {
		OcMap ace;
		if (partOf != null) {
			ace = acls.getAce("default", getResource().getColumn(partOf, true).getResource());
		} else {
			ace = acls.getAce("default", resourceName);
		}
		return ace;
	}

	protected File getDataDir(String path) {
		File dir = new File(Configs.get("base.dataPath"), path);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new OccamException("Directory %s not exist!", dir);
		}
		return dir;
	}
	
	protected OcMap getOld(OcContext ctx) {
		if(ctx.id() == null)
			return null;
		OcMap old = ctx.getMap("_old");
		if(old == null) {
			OcMap row = ctx.getRow();
			if(row != null && row.id() != null)
				old = get(ctx.id(row.id()));
			else
				old = get(ctx);
		}
		ctx.put("_old", old);
		return old;
	}
	
	protected OcMap getComplete(OcContext ctx) {
		OcMap old = getOld(ctx);
		OcMap complete = new OcMap();
		if(old != null) {
			complete.putAll(old);
		}
		complete.putAll(ctx.getRow());
		return complete;
	}

	public OcResult selectCache(OcContext ctx) {
		Cache cache = cacheManager.getCache(resourceName);
		String key = ctx.toString();
		ValueWrapper value = cache.get(key);
		if (value == null) {
			OcResult result = select(ctx);
			cache.put(key, result);
			return result;
		} else {
			return (OcResult) value.get();
		}
	}

	public void clearCache(OcContext ctx) {
		if(!getResource().isCacheable())
			return;
		Cache cache = cacheManager.getCache(resourceName);
		cache.clear();
	}
	
	@Override
	public void postSave(OcContext ctx) {
		super.postSave(ctx);
		clearCache(ctx);
	}
	
	@Override
	public void postDelete(OcContext ctx) {
		super.postDelete(ctx);
		clearCache(ctx);
	}
	
}
