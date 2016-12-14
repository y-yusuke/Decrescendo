package decrescendo.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCreator {
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
}
