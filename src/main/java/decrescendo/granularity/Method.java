package decrescendo.granularity;

import decrescendo.hash.Hash;

import java.util.List;

public class Method extends Granularity {
	public final String name;
	public final int order;
	public final int startLine;
	public final int endLine;
	public final List<String> normalizedTokens;
	public final List<String> originalTokens;
	public final List<Integer> lineNumberPerToken;

	public Method(String path, String name, int order, int startLine, int endLine, Hash normalizedHash, Hash originalHash, List<String> normalizedTokens, List<String> originalTokens, List<Integer> lineNumberPerToken) {
		super(path, normalizedHash, originalHash);

		this.name = name;
		this.order = order;
		this.startLine = startLine;
		this.endLine = endLine;
		this.normalizedTokens = normalizedTokens;
		this.originalTokens = originalTokens;
		this.lineNumberPerToken = lineNumberPerToken;
	}
}
