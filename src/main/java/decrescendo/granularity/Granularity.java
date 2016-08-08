package decrescendo.granularity;

import java.util.List;

public abstract class Granularity {
	private String path;
	private String name;
	private int num;
	private String normalizedHash;
	private String originalHash;
	private List<String> normalizedTokens;
	private List<String> originalTokens;
	private List<Integer> lineNumberPerToken;
	private int startline;
	private int endline;
	private int representative;
	private List<byte[]> normalizedSentences;
	private List<byte[]> originalSentences;
	private List<List<Integer>> lineNumberPerSentence;

	public Granularity(String path, String name, String originalHash, String normalizedHash,
			List<String> originalTokens, List<String> normalizedTokens, List<Integer> lineNumberPerToken,
			int startline, int endline, int num, int representative) {
		this.path = path;
		this.name = name;
		this.originalHash = originalHash;
		this.normalizedHash = normalizedHash;
		this.originalTokens = originalTokens;
		this.normalizedTokens = normalizedTokens;
		this.lineNumberPerToken = lineNumberPerToken;
		this.startline = startline;
		this.endline = endline;
		this.num = num;
		this.representative = representative;
	}

	public void setRepresentative(int representative) {
		this.representative = representative;
	}

	public void setNormalizedSentences(List<byte[]> normalizedSentences) {
		this.normalizedSentences = normalizedSentences;
	}

	public void setOriginalSentences(List<byte[]> originalSentences) {
		this.originalSentences = originalSentences;
	}

	public void setLineNumberPerSentence(List<List<Integer>> lineNumberPerSentence) {
		this.lineNumberPerSentence = lineNumberPerSentence;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public int getNum() {
		return num;
	}

	public String getOriginalHash() {
		return originalHash;
	}

	public String getNormalizedHash() {
		return normalizedHash;
	}

	public List<String> getOriginalTokens() {
		return originalTokens;
	}

	public List<String> getNormalizedTokens() {
		return normalizedTokens;
	}

	public List<Integer> getLineNumberPerToken() {
		return lineNumberPerToken;
	}

	public int getStartline() {
		return startline;
	}

	public int getEndline() {
		return endline;
	}

	public int isRepresentative() {
		return representative;
	}

	public List<byte[]> getNormalizedSentences() {
		return normalizedSentences;
	}

	public List<byte[]> getOriginalSentences() {
		return originalSentences;
	}

	public List<List<Integer>> getLineNumberPerSentence() {
		return lineNumberPerSentence;
	}

}
