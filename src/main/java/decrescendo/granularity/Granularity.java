package decrescendo.granularity;

import decrescendo.hash.Hash;

public abstract class Granularity {
	public final String path;
	public final Hash normalizedHash;
	public final Hash originalHash;
	public final int startLine;
	public final int endLine;
	public int representative;

	Granularity(String path, Hash normalizedHash, Hash originalHash, int startLine, int endLine) {
		this.path = path;
		this.normalizedHash = normalizedHash;
		this.originalHash = originalHash;
		this.startLine = startLine;
		this.endLine = endLine;
		this.representative = 0;
	}

	public Hash getNormalizedHash() {
		return normalizedHash;
	}
}
