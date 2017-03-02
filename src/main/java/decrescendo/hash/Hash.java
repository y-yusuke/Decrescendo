package decrescendo.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Hash {
	public final byte[] hash;
	public final int hashCode;

	public Hash(byte[] hash) {
		if (hash == null || hash.length != 16) {
			throw new IllegalArgumentException();
		}
		this.hash = hash;
		this.hashCode = Arrays.hashCode(hash);
	}

    public static Hash createHash(String source) {
		try {
			MessageDigest hash = MessageDigest.getInstance("MD5");
			hash.update(source.getBytes());
			return new Hash(hash.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Hash hash1 = (Hash) o;
		return hashCode == hash1.hashCode && Arrays.equals(hash, hash1.hash);
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	public int compareTo(Hash hash2) {
		for (int i = 0; i < this.hash.length; i++) {
			if (this.hash[i] > hash2.hash[i]) {
				return 1;
			}
			if (this.hash[i] < hash2.hash[i]) {
				return -1;
			}
		}
		return 0;
	}
}
