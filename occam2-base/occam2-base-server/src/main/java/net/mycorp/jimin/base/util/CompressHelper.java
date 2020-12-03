package net.mycorp.jimin.base.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class CompressHelper {
	
	public static byte[] compress(String inStr) {
		ZipOutputStream out = null;
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			out = new ZipOutputStream(bytes);
			out.putNextEntry(new ZipEntry("str"));
			IOUtils.write(inStr, out, "UTF-8");
			out.closeEntry();
			return bytes.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static String extract(byte[] bytes) {
		ZipInputStream zipIn = null;
		try {
			zipIn = new ZipInputStream(new ByteArrayInputStream(bytes));
			zipIn.getNextEntry();
			String result = IOUtils.toString(zipIn, "UTF-8");
			zipIn.closeEntry();
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				zipIn.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	
	public static long compressZip(File inFile, File outFile) {
		ZipOutputStream out = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(inFile);
			out = new ZipOutputStream(new FileOutputStream(outFile));
			out.putNextEntry(new ZipEntry(outFile.getName()));
			long result = IOUtils.copyLarge(in, out);
			out.closeEntry();
			return result;
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

	public static File extractZip(File inFile, File outFile) {
		FileOutputStream unzipOut = null;
		ZipInputStream zipIn = null;
		try {
			unzipOut = new FileOutputStream(outFile);
			zipIn = new ZipInputStream(new FileInputStream(inFile));
			zipIn.getNextEntry();
			IOUtils.copyLarge(zipIn, unzipOut);
			zipIn.closeEntry();
			return outFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				zipIn.close();
				unzipOut.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static File extractZipFolder(File file) {
		ZipFile zip = null;

		try {
			zip = new ZipFile(file);
			File folder = new File(file.getParentFile(),
					FilenameUtils.getBaseName(file.getName()));
			folder.mkdirs();

			Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

			while (zipFileEntries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();
				File destFile = new File(folder, currentEntry);
				File destinationParent = destFile.getParentFile();
				destinationParent.mkdirs();

				if (!entry.isDirectory()) {
					FileHelper.write(destFile, zip.getInputStream(entry));
				}

				if (currentEntry.endsWith(".zip")) {
					extractZipFolder(destFile);
				}
			}
			return folder;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				zip.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
