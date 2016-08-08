package decrescendo.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCreator {
	public HashCreator() {
	}

	public static byte[] getHash(String source) {
		try {
			MessageDigest hash = MessageDigest.getInstance("MD5");
			hash.update(source.getBytes());
			return hash.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String convertString(byte[] digest) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			String tmp = Integer.toHexString(digest[i] & 0xff);
			if (tmp.length() == 1)
				buffer.append('0').append(tmp);
			else
				buffer.append(tmp);
		}
		return buffer.toString();
	}

}
