package decrescendo.granularity;

import java.util.List;

public abstract class Granularity {
	private String path;
	private String name;
	private int order;
	private String normalizedHash;
	private String originalHash;
	private List<String> normalizedTokens;
	private List<String> originalTokens;
	private List<Integer> lineNumberPerToken;
	private int startLine;
	private int endLine;
	private int representative;
	private List<byte[]> normalizedSentences;
	private List<byte[]> originalSentences;
	private List<List<Integer>> lineNumberPerSentence;

	Granularity() {
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getNormalizedHash() {
		return normalizedHash;
	}

	public void setNormalizedHash(String normalizedHash) {
		this.normalizedHash = normalizedHash;
	}

	public String getOriginalHash() {
		return originalHash;
	}

	public void setOriginalHash(String originalHash) {
		this.originalHash = originalHash;
	}

	public List<String> getNormalizedTokens() {
		return normalizedTokens;
	}

	public void setNormalizedTokens(List<String> normalizedTokens) {
		this.normalizedTokens = normalizedTokens;
	}

	public List<String> getOriginalTokens() {
		return originalTokens;
	}

	public void setOriginalTokens(List<String> originalTokens) {
		this.originalTokens = originalTokens;
	}

	public List<Integer> getLineNumberPerToken() {
		return lineNumberPerToken;
	}

	public void setLineNumberPerToken(List<Integer> lineNumberPerToken) {
		this.lineNumberPerToken = lineNumberPerToken;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public int isRepresentative() {
		return representative;
	}

	public void setRepresentative(int representative) {
		this.representative = representative;
	}

	public List<byte[]> getNormalizedSentences() {
		return normalizedSentences;
	}

	public void setNormalizedSentences(List<byte[]> normalizedSentences) {
		this.normalizedSentences = normalizedSentences;
	}

	public List<byte[]> getOriginalSentences() {
		return originalSentences;
	}

	public void setOriginalSentences(List<byte[]> originalSentences) {
		this.originalSentences = originalSentences;
	}

	public List<List<Integer>> getLineNumberPerSentence() {
		return lineNumberPerSentence;
	}

	public void setLineNumberPerSentence(List<List<Integer>> lineNumberPerSentence) {
		this.lineNumberPerSentence = lineNumberPerSentence;
	}
}
