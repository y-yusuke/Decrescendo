package decrescendo.granularity;

import decrescendo.hash.Hash;

public class File extends Granularity {
	public final String source;

	public File(String path, int startLine, int endLine,
				Hash normalizedHash, Hash originalHash,
				String source) {
		super(path, normalizedHash, originalHash, startLine, endLine);
		this.source = source;
	}
}
