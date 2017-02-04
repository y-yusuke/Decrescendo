package decrescendo.suffixtree;

import java.util.ArrayList;
import java.util.List;

class Node {
	public int start;
	public int depth;
	public Node child;
	public Node bros;
	public Node link;
	public int id;
	private int maxLen;

	public Node(int start, int depth, int id, int maxLen) {
		this.start = start;
		this.depth = depth;
		this.child = null;
		this.bros = null;
		this.link = null;
		this.id = id;
		this.maxLen = maxLen;
	}

	public Node searchChild(List<String> buff, int size, String x) {
		Node child = this.child;
		while (child != null) {
			if (child.start < size && buff.get(child.start).equals(x)) {
				return child;
			}
			child = child.bros;
		}
		return null;
	}

	public void insertChild(Node child) {
		child.bros = this.child;
		this.child = child;
	}

	public void deleteChild(Node child) {
		if (this.child == child) {
			this.child = child.bros;
		} else {
			Node node = this.child;
			while (node.bros != null) {
				if (node.bros == child) {
					node.bros = node.bros.bros;
					break;
				}
				node = node.bros;
			}
		}
	}

	public int getLength() {
		if (this.start == -1) {
			return 0;
		} else if (this.child == null) {
			return this.maxLen;
		} else {
			return this.child.depth - this.depth;
		}
	}

	public List<Node> traverseLeaf() {
		List<Node> childs = new ArrayList<>();
		if (this.child == null) {
			childs.add(this);
		} else {
			Node node = this.child;
			while (node != null) {
				if (node.child == null) {
					childs.add(node);
				}
				node = node.bros;
			}
		}
		return childs;
	}
}
