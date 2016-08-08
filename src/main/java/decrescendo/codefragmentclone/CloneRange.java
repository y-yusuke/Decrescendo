package decrescendo.codefragmentclone;

public class CloneRange {

	private int startLine;
	private int endLine;
	private String gapLines;
	private int gapLinesize;

	public CloneRange(int startLine, int endLine, String gapLines, int gapLinesize) {
		this.startLine = startLine;
		this.endLine = endLine;
		this.gapLines = gapLines;
		this.gapLinesize = gapLinesize;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public String getGapLines() {
		return gapLines;
	}

	public int getGapLinesize() {
		return gapLinesize;
	}

}
