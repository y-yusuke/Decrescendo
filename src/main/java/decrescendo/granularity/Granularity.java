package decrescendo.granularity;

import decrescendo.hash.Hash;

public abstract class Granularity {
	public final String path;
	public final Hash normalizedHash;
	public final Hash originalHash;
	public int representative;

	Granularity(String path, Hash normalizedHash, Hash originalHash) {
		this.path = path;
		this.normalizedHash = normalizedHash;
		this.originalHash = originalHash;
		this.representative = 0;
	}

	public Hash getNormalizedHash() {
		return normalizedHash;
	}
}
