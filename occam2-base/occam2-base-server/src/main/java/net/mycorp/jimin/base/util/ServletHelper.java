package net.mycorp.jimin.base.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import net.mycorp.jimin.base.core.OccamException;

public class ServletHelper {

	public static Map<String, String> cookieToMap(Cookie[] cookies) {
		HashMap<String, String> cookieMap = new HashMap<String, String>();
		if (cookies == null)
			return cookieMap;
		for (Cookie cookie : cookies) {
			cookieMap.put(cookie.getName(), cookie.getValue());
		}
		return cookieMap;
	}

	public static Map<String, Object> sessionToMap(HttpSession session) {
		HashMap<String, Object> sessinMap = new HashMap<String, Object>();
		Enumeration<String> attrNames = session.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String name = attrNames.nextElement();
			sessinMap.put(name, session.getAttribute(name));
		}
		return sessinMap;
	}

	public static Map<String, String> getCookieMap() {
		return cookieToMap(getRequest().getCookies());
	}

	public static Map<String, Object> getSessionMap() {
		return sessionToMap(getRequest().getSession());
	}
	
	public static String getSessionId() {
		return getRequest().getSession().getId();
	}

	private static ServletRequestAttributes getRequestAttributes() {
		return (ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes();
	}

	public static HttpServletRequest getRequest() {
		return getRequestAttributes().getRequest();
	}

	public static HttpServletResponse getResponse() {
		try {
			return getRequestAttributes().getResponse();
		} catch (IllegalStateException e) {
			return null;
		}
	}

	public static String getRemoteAddr() {
		try {
			HttpServletRequest request = getRequest();
			String remoteAddr = request.getHeader("x-forwarded-for");
			if (remoteAddr == null)
				remoteAddr = request.getRemoteAddr();
			return remoteAddr;
		} catch (IllegalStateException e) {
			// batch에서 호출 시
			return "localhost";
		}
	}

	public static String getContextPath() {
		HttpServletRequest request = getRequest();
		String contextPath = request.getContextPath();
		return Helper.empty(contextPath) ? "/" : contextPath;
	}
	
	public static void download(InputStream in, long length, String fileName, String contentType, boolean inline) {
		download(getRequest(), getResponse(), in, length, fileName,
				contentType, inline, new ContentRange(-1, -1, length));
	}

	public static void download(File file, String fileName, String contentType,
			boolean inline) {
		download(file, fileName, contentType, inline, -1, -1);
	}

	public static void download(File file, String fileName, String contentType,
			boolean inline, long offset, long size) {
		FileInputStream in = null;
		try {
			try {
			in = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {					
				}
				in = new FileInputStream(file);
			}

			download(getRequest(), getResponse(), in, file.length(), fileName,
					contentType, inline, new ContentRange(offset, size, file.length()));
		} catch (IOException e) {
			throw new OccamException(e);
		} finally {
			IOHelper.close(in);
		}
	}

	private static void download(HttpServletRequest request,
			HttpServletResponse response, InputStream in, long length,
			String fileName, String contentType, boolean inline,
			ContentRange contentRange) {
		String userAgent = request.getHeader("User-Agent");
		if (contentType == null)
			contentType = "application/octet-stream";
		else if(contentType.contains(";"))
			contentType = contentType.substring(0, contentType.indexOf(";"));
		response.setContentType(contentType);
		try {
			if (userAgent != null
					&& (userAgent.indexOf("MSIE") > -1 || userAgent
							.indexOf("Trident") > -1)) {
				fileName = URLEncoder.encode(fileName, "UTF-8");
				fileName = fileName.replace('+', ' ');
			} else {
				fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
			}
			response.setHeader("Content-Disposition", (inline ? "inline"
					: "attachment") + "; filename=\"" + fileName + "\";");

			if (contentRange.isInvalid()) {
				response.setContentLength((int) length);
				IOUtils.copyLarge(in, response.getOutputStream());
			} else {
				response.setContentLength((int) contentRange.size);
				response.setHeader(HttpHeaders.CONTENT_RANGE, contentRange.toString());
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
				IOUtils.copyLarge(in, response.getOutputStream(), contentRange.offset, contentRange.size);
			}
			response.flushBuffer();
		} catch (IOException e) {
			throw new OccamException(e);
		} finally {
			IOHelper.close(in);
		}
	}

	public static void download(HttpServletRequest request,
			HttpServletResponse response, File file, String fileName,
			String contentType, boolean open) {
		try {
			FileInputStream in = new FileInputStream(file);
			download(request, response, in, file.length(), fileName,
					contentType, open, new ContentRange(-1, -1, file.length()));
		} catch (IOException e) {
			throw new OccamException(e);
		}
	}

	public static void addCookie(String name, String value, String path,
			int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		if (maxAge != -1)
			cookie.setMaxAge(maxAge);
		if(getResponse() != null)
			getResponse().addCookie(cookie);
	}

	public static void addCookie(String name, String value) {
		addCookie(name, value, "/", -1);
	}

	public static void removeCookie(String name) {
		addCookie(name, null, "/", 0);
	}
	
	static class ContentRange {
		private long offset;
		private long size;
		private long total;

		public ContentRange(long offset, long size, long total) {
			if (size < 1 || offset + size > total) {
				this.size = total - offset > 0 ? total - offset : 0;
			} else {
				this.size = size;
			}
			this.offset = offset;
			this.total = total;
		}
		
		public boolean isValidAndNotFull() {
			return offset >= 0 && size > 0 && total >= 0 && size != total;
		}
		
		public boolean isInvalid() {
			return !isValidAndNotFull();
		}

		public String toString() {
			long end = offset + size - 1;
			return "bytes " + offset + '-' + end + '/' + total;
		}
	}

	public static String getRealPath(String path) {
		HttpServletRequest request = getRequest();
		return request.getServletContext().getRealPath(path);
	}

}
