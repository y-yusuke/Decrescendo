package decrescendo.granularity;

import decrescendo.hash.Hash;

import java.util.List;

public class CodeFragment extends Granularity {
	public final String name;
	public final int order;
	public final List<Hash> normalizedSentences;
	public final List<Hash> originalSentences;
	public final List<List<Integer>> lineNumberPerSentence;
	public final Hash id;

	public CodeFragment(String path, String name, int order, int startLine, int endLine,
						List<Hash> normalizedSentences, List<Hash> originalSentences, List<List<Integer>> lineNumberPerSentence, Hash id) {
		super(path, null, null, startLine, endLine);
		this.name = name;
		this.order = order;
		this.normalizedSentences = normalizedSentences;
		this.originalSentences = originalSentences;
		this.lineNumberPerSentence = lineNumberPerSentence;
		this.id = id;
	}
}
