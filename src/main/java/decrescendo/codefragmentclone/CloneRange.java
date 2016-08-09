package decrescendo.codefragmentclone;

public class CloneRange {

	private int startLine;
	private int endLine;
	private String gapLines;
	private int gapLineSize;

	public CloneRange(int startLine, int endLine, String gapLines, int gapLineSize) {
		this.startLine = startLine;
		this.endLine = endLine;
		this.gapLines = gapLines;
		this.gapLineSize = gapLineSize;
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

	public int getGapLineSize() {
		return gapLineSize;
	}

}
