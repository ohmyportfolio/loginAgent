package net.mycorp.jimin.base.common.services;

import java.io.File;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.common.io.BaseEncoding;

import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.util.FileHelper;

@Service
public class GridStates extends Bases {

	@Override
	@CacheEvict(value = "gridStates", key = "#ctx.id()")
	public int update(OcContext ctx) {
		File dir = getDataDir(resourceName);
		OcMap row = ctx.getRow();
		String filename = encodeFilename(ctx.id());
		row.put("filename", filename);
		row.put("id", ctx.getId());
		FileHelper.write(new File(dir, filename), ctx.getRow().getString("state"));
		int result = super.update(ctx.skipPermit(true));
		if(result == 0)
			super.insert(row);
		return result;
	}

	@Override
	@Cacheable(value = "gridStates", key = "#ctx.id()")
	public OcMap get(OcContext ctx) {
		OcMap row = super.get(ctx);
		String state = FileHelper.readString(new File(getDataDir(resourceName), encodeFilename(ctx.id())));
		if(row != null)
			row.put("state", state);
		return row;
	}

	private String encodeFilename(String key) {
		return BaseEncoding.base32().encode(key.getBytes());
	}

}
