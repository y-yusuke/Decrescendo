package decrescendo.evaluate;

import java.util.List;

public class ClonePair {

	private String path1;
	private int startLine1;
	private int endLine1;
	private List<Integer> gapLines1;
	private String path2;
	private int startLine2;
	private int endLine2;
	private List<Integer> gapLines2;
	private boolean isMatch;

	public ClonePair(String path1, int startLine1, int endLine1, List<Integer> gapLines1,
					 String path2, int startLine2, int endLine2, List<Integer> gapLines2) {
		this.path1 = path1;
		this.startLine1 = startLine1;
		this.endLine1 = endLine1;
		this.gapLines1 = gapLines1;
		this.path2 = path2;
		this.startLine2 = startLine2;
		this.endLine2 = endLine2;
		this.gapLines2 = gapLines2;
		this.isMatch = false;
	}

	public String getPath1() {
		return path1;
	}

	public int getStartLine1() {
		return startLine1;
	}

	public int getEndLine1() {
		return endLine1;
	}

	public List<Integer> getGapLine1() {
		return gapLines1;
	}

	public String getPath2() {
		return path2;
	}

	public int getStartLine2() {
		return startLine2;
	}

	public int getEndLine2() {
		return endLine2;
	}

	public List<Integer> getGapLine2() {
		return gapLines2;
	}

	public boolean isMatch() {
		return isMatch;
	}

	public void setMatch() {
		isMatch = true;
	}
}
