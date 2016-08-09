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
		StringBuilder buffer = new StringBuilder();
		for (byte aDigest : digest) {
			String tmp = Integer.toHexString(aDigest & 0xff);
			if (tmp.length() == 1)
				buffer.append('0').append(tmp);
			else
				buffer.append(tmp);
		}
		return buffer.toString();
	}

}
