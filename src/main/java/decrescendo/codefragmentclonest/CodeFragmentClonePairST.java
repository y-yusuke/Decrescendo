package decrescendo.codefragmentclonest;

import decrescendo.granularity.Method;
import decrescendo.hash.Hash;

public class CodeFragmentClonePairST {
	public final Method clone1;
	public final Method clone2;
	public final Hash commonHash;
	public final int index1;
	public final int index2;
	public final int size;

	public CodeFragmentClonePairST(Method clone1, Method clone2, Hash commonHash, int index1, int index2, int size) {
		this.clone1 = clone1;
		this.clone2 = clone2;
		this.commonHash = commonHash;
		this.index1 = index1;
		this.index2 = index2;
		this.size = size;
	}

	public Hash getCommonHash() {
		return commonHash;
	}
}
