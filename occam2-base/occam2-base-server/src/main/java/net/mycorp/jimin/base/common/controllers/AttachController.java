package net.mycorp.jimin.base.common.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.mycorp.jimin.base.common.services.Attachs;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.util.ServletHelper;

@Controller
@Transactional
@RequestMapping(value = "api")
public class AttachController {

	protected static Logger log = LoggerFactory.getLogger(AttachController.class);

	@Autowired
	private Attachs attachs;
    
	@RequestMapping(path = "attachs", method = RequestMethod.POST)
	@ResponseBody
	public void upload(@RequestParam("files[]") MultipartFile[] files,
			@RequestParam Map<String, Object> attach) throws Exception {
		String realPath = ServletHelper.getRealPath(".");
		attachs.uploadAttach(files, new OcMap(attach), realPath);
	}
	
	@RequestMapping(path = "attachs/{id}/download", method = RequestMethod.GET)
	@ResponseBody
	public  ResponseEntity<InputStreamResource> download(@RequestHeader HttpHeaders headers, @PathVariable String id) throws IOException {
		File file = attachs.getAttachFile(id);
		OcMap attach = attachs.get(id);
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		headers.add("Content-Transfer-Encoding", "binary");
		headers.setContentDispositionFormData("attachment", attach.getString("name"));
		InputStreamResource  resource = new InputStreamResource(new FileInputStream(file));
		return ResponseEntity.ok().headers(headers).contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
	}

	@RequestMapping(path = "attachs/{id}/open", method = RequestMethod.GET)
	@ResponseBody
	public  ResponseEntity<InputStreamResource> open(@RequestHeader HttpHeaders headers, @PathVariable String id) throws IOException {
		File file = attachs.getAttachFile(id);
		OcMap attach = attachs.get(id);
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
		return ResponseEntity.ok().contentLength(attach.getLong("file_size"))
				.contentType(MediaType.parseMediaType(attach.getString("content_type"))).body(resource);
	}
}
