package decrescendo.granularity;

import java.util.List;

public class Method extends Granularity {

	public Method(String path, String name, String originalHash, String normalizedHash,
			List<String> originalTokens, List<String> normalizedTokens, List<Integer> lineNumberPerToken,
			int startline, int endline, int num, int representative) {
		super(path, name, originalHash, normalizedHash,
				originalTokens, normalizedTokens, lineNumberPerToken,
				startline, endline, num, representative);
	}

}
