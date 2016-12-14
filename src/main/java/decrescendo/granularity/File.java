package decrescendo.granularity;

import decrescendo.hash.Hash;

public class File extends Granularity {
	public final String code;

	public File(String path, Hash normalizedHash, Hash originalHash, String code) {
		super(path, normalizedHash, originalHash);

		this.code = code;
	}
}
