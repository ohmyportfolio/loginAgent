package net.mycorp.jimin.base.common.services;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.mycorp.jimin.base.core.Configs;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.util.Helper;

@Service
public class Attachs extends Bases {

	public void uploadAttach(MultipartFile[] files, OcMap attach, String realPath) throws IllegalStateException, IOException {
		File tempDir = new File(realPath + File.separatorChar + "/attach");
		if(!tempDir.exists())
			tempDir.mkdirs();
		for (MultipartFile multipartFile : files) {
			attach.put("name", Normalizer.normalize(FilenameUtils.getName(multipartFile.getOriginalFilename()), Normalizer.Form.NFC));
			attach.put("file_size", multipartFile.getSize());
			attach.put("content_type", multipartFile.getContentType());
			attach.put("id", Helper.getRand16c());
			File tempFile = new File(tempDir, attach.id());
			multipartFile.transferTo(tempFile);
			tempFile.renameTo(getAttachFile(attach.id()));
			insert(attach);
		}
	}
	
	@Override
	public void postDelete(OcContext ctx) {
		getAttachFile(ctx.id()).delete();
	}

	public File getAttachFile(String id) {
		String attachPath = Configs.get("base.attachPath", "/attach");
		File attachDir = new File(attachPath);
		if(!attachDir.exists())
			attachDir.mkdir();
		return new File(attachDir, id);
	}
	
}
