package net.mycorp.jimin.base.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class FileHelper {

	public static String getHashValue(File file) {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			Hasher hasher = Hashing.murmur3_128().newHasher();
			byte[] buffer = new byte[1024 * 512];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				hasher.putBytes(buffer, 0, bytesRead);
			}
			return hasher.hash().toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static String readString(File file) {
		try {
			return FileUtils.readFileToString(file, "UTF-8");
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}

	public static void write(File file, String str) {
		try {
			FileUtils.write(file, str, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void write(File file, InputStream in) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			IOUtils.copyLarge(in, out);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
