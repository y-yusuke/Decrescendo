package decrescendo.suffixtree;

class SearchNextNode {
	public final Node prev;
	public final Node node;
	public final int match;

	SearchNextNode(Node prev, Node node, int match) {
		this.prev = prev;
		this.node = node;
		this.match = match;
	}

	public Node getPrev() {
		return prev;
	}

	public Node getNode() {
		return node;
	}

	public int getMatch() {
		return match;
	}
}
