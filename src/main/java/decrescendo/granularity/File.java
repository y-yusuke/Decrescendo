package decrescendo.granularity;

import java.util.List;

public class File extends Granularity {
	private String source;

	public File(String path, String source, String originalHash, String normalizedHash,
			List<String> originalTokens, List<String> normalizedTokens, List<Integer> lineNumberPerToken,
			int startLine, int endLine, int representative) {
		super(path, "", originalHash, normalizedHash,
				originalTokens, normalizedTokens, lineNumberPerToken,
				startLine, endLine, 0, representative);
		this.source = source;
	}

	public String getSource() {
		return source;
	}

}
