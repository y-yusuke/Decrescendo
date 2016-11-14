package decrescendo.hash;

import java.util.Arrays;

public class Hash {
	public final byte[] hash;
	public final int hashCode;

	public Hash(byte[] hash) {
		this.hash = hash;
		this.hashCode = Arrays.hashCode(hash);
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
}
