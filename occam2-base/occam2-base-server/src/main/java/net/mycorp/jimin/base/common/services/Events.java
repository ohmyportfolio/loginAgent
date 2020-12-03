package net.mycorp.jimin.base.common.services;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcResource;
import net.mycorp.jimin.base.domain.OcResult;
import net.mycorp.jimin.base.domain.OcResource.LogLevel;
import net.mycorp.jimin.base.util.Helper;
import net.mycorp.jimin.base.util.ServletHelper;
import net.mycorp.jimin.base.util.StringHelper;

@Service
public class Events extends Bases {

	@SuppressWarnings("rawtypes")
	public void logEvent(OcResource resource, String command, OcContext ctx, Object result) {
		if (!ctx.isFromClient())
			return;
		LogLevel logLevel = resource.getLogLevel(); // all, list, write, none;
		if (logLevel == null || logLevel == OcResource.LogLevel.none)
			return;
		if (logLevel == OcResource.LogLevel.write && StringHelper.containsCommaList("get,list", command))
			return;
		if (logLevel == OcResource.LogLevel.list && StringHelper.containsCommaList("get", command))
			return;

		int resultCount = 0;
		if (result instanceof OcResult) {
			if (logLevel == OcResource.LogLevel.write)
				return;
			resultCount = ((OcResult) result).getData().size();
		} else if (result instanceof List) {
			if (logLevel == OcResource.LogLevel.write && !command.equals("insert"))
				return;
			resultCount = ((List<?>) result).size();
		} else if (result instanceof Integer) {
			resultCount = (Integer) result;
		} else if (result != null) {
			resultCount = 1;
		}
		String rowVal = ctx.isNoLogRow() ? null : Helper.toJson(ctx.getRow());
		if(rowVal != null && rowVal.length() > 2000)
			rowVal = rowVal.substring(0, 2000);
		String condition = ctx.getCondition().keySet().size() == 0 ? null : Helper.toJson(ctx.getCondition());
		if(condition != null && condition.length() > 2000)
			condition = condition.substring(0, 2000);
		String hostAddr = null;
		String requestUrl = null;
		String requestMethod = null;
		try {
			hostAddr = InetAddress.getLocalHost().getHostAddress();
			HttpServletRequest request = ServletHelper.getRequest();
			if(request.getQueryString() != null)
				requestUrl = request.getRequestURL().append('?').append(request.getQueryString()).toString();
			else
				requestUrl = request.getRequestURL().toString();
			if(requestUrl.length() > 2000)
				requestUrl = requestUrl.toString().substring(0, 2000);
			requestMethod  = request.getMethod();
		} catch (Exception e) {
		}
		Date regDate = new Date();
		long elapsedMs = regDate.getTime() - ctx.getCreated().getTime();
		String dataId = ctx.id();
		if(dataId != null && dataId.length() > 255)
			dataId = dataId.substring(0, 255);
		
		String projectId = null, taskId = null;
		// task_id 얻기
		if("tasks".equals(resource.getName())) {
			taskId = dataId;
		}
		if(taskId == null && ctx.getRow() != null) {
			taskId = ctx.getRow().getString("task_id");
		}
		if(taskId == null && ctx.getId() instanceof Map) {
			taskId = (String)((Map)ctx.getId()).get("task_id");
		}
		if(taskId == null && ctx.getCondition() != null) {
			taskId = ctx.getCondition().getString("task_id");
		}
		// project_id 얻기
		if(ctx.getRow() != null) {
			projectId = ctx.getRow().getString("project_id");
		}
		if(taskId != null && projectId == null) {
			projectId = (String) select("project_id").resource("tasks").condition(m("id", taskId)).value();
		}
		
		insert(ctx().row(m("resource_name", resource.getName(), "command_name", command, "data_id", dataId,
				"remote_addr", ServletHelper.getRemoteAddr(), "row_val", rowVal, "condition_val", condition, "result_count",
				resultCount, "host_addr", hostAddr, "request_url", requestUrl, "request_method", requestMethod,
				"elapsed_ms", elapsedMs, "reg_date", regDate, "project_id", projectId, "task_id", taskId)).skipPermit(true));
	}

	public void logEvent(String resourceName, String commandName) {
		insert(ctx().row(m("resource_name", resourceName, "command_name", commandName, "ip_addr",
				ServletHelper.getRemoteAddr())).skipPermit(true));
	}
	
	public void logEventAtBatch(String batchName, String result, String note) {
		if(note != null && note.length() > 4000)
			note = note.substring(0, 4000);
		insert(ctx().row(m("resource_name", "batch", "command_name", batchName, "row_val", result,
				"note", note, "ip_addr", ServletHelper.getRemoteAddr())).skipPermit(true));
	}

}
