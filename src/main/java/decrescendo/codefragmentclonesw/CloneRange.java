package decrescendo.codefragmentclonesw;

public class CloneRange {
	public final int startLine;
	public final int endLine;
	public final String gapLines;
	public final int gapLineSize;

	public CloneRange(int startLine, int endLine, String gapLines, int gapLineSize) {
		this.startLine = startLine;
		this.endLine = endLine;
		this.gapLines = gapLines;
		this.gapLineSize = gapLineSize;
	}
}
