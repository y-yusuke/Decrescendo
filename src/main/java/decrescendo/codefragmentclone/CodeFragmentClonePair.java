package decrescendo.codefragmentclone;

import java.util.List;

public class CodeFragmentClonePair<T> {
	private T clone1;
	private T clone2;
	private String commonHash;
	private List<Integer> cloneIndexes1;
	private List<Integer> cloneIndexes2;
	private List<Integer> gapIndexes1;
	private List<Integer> gapIndexes2;

	public CodeFragmentClonePair(T clone1, T clone2, String commonHash,
								 List<Integer> cloneIndexes1, List<Integer> cloneIndexes2,
								 List<Integer> gapIndexes1, List<Integer> gapIndexes2) {
		this.clone1 = clone1;
		this.clone2 = clone2;
		this.commonHash = commonHash;
		this.cloneIndexes1 = cloneIndexes1;
		this.cloneIndexes2 = cloneIndexes2;
		this.gapIndexes1 = gapIndexes1;
		this.gapIndexes2 = gapIndexes2;
	}

	public T getClone1() {
		return clone1;
	}

	public T getClone2() {
		return clone2;
	}

	public String getCommonHash() {
		return commonHash;
	}

	public List<Integer> getCloneIndexes1() {
		return cloneIndexes1;
	}

	public List<Integer> getCloneIndexes2() {
		return cloneIndexes2;
	}

	public List<Integer> getGapIndexes1() {
		return gapIndexes1;
	}

	public List<Integer> getGapIndexes2() {
		return gapIndexes2;
	}

}
