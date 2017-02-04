package decrescendo.suffixtree;

class Return {
	public final Node prev;
	public final Node node;
	public final int nlen;
	public final int ni;
	public final int si;
	public final int bi;

	Return(Node prev, Node node, int nlen, int ni, int si, int bi) {
		this.prev = prev;
		this.node = node;
		this.nlen = nlen;
		this.ni = ni;
		this.si = si;
		this.bi = bi;
	}

	public Node getPrev() {
		return prev;
	}

	public Node getNode() {
		return node;
	}

	public int getNlen() {
		return nlen;
	}

	public int getNi() {
		return ni;
	}

	public int getSi() {
		return si;
	}

	public int getBi() {
		return bi;
	}
}