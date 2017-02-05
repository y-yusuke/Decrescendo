package decrescendo.suffixtree;

class Return {
	private final Node prev;
	private final Node node;
	private final int nlen;
	private final int ni;
	private final int si;
	private final int bi;

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