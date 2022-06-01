package net.mycorp.jimin.mybot.util;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.mycorp.jimin.base.util.Helper;
import net.mycorp.jimin.base.util.StringHelper;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class AppHelper {
	
	public static String M_K = "!QAZxsw2";

	@SuppressWarnings("unused")
	private static String sayHello(String text) throws IOException {
		return text;

	}

	public static String Decrypt(String text, String key) throws Exception {

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] keyBytes = new byte[16];
		byte[] b = key.getBytes("UTF-8");
		int len = b.length;

		if (len > keyBytes.length)
			len = keyBytes.length;

		System.arraycopy(b, 0, keyBytes, 0, len);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] results = cipher.doFinal(decoder.decodeBuffer(text));
		return new String(results, "UTF-8");
	}

	public static String Encrypt(String text, String key) throws Exception {
		
		if(text == null || text == "") {
			return null;
		}
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] keyBytes = new byte[16];
		byte[] b = key.getBytes("UTF-8");
		int len = b.length;
		if (len > keyBytes.length)
			len = keyBytes.length;
		System.arraycopy(b, 0, keyBytes, 0, len);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

		byte[] results = cipher.doFinal(text.getBytes("UTF-8"));

		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(results);
	}
}