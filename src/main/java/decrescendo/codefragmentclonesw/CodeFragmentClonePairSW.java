package decrescendo.codefragmentclonesw;

import decrescendo.granularity.CodeFragment;
import decrescendo.hash.HashList;

import java.util.List;

public class CodeFragmentClonePairSW {
	public final CodeFragment clone1;
	public final CodeFragment clone2;
	public final HashList commonHash;
	public final List<Integer> cloneIndexes1;
	public final List<Integer> cloneIndexes2;
	public final List<Integer> gapIndexes1;
	public final List<Integer> gapIndexes2;

	public CodeFragmentClonePairSW(CodeFragment clone1, CodeFragment clone2, HashList commonHash,
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

	public HashList getCommonHash() {
		return commonHash;
	}
}
