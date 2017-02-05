package decrescendo.suffixtree;

import java.util.ArrayList;
import java.util.List;

class SuffixTree {
	private List<String> buff;
	public int size;
	private final Node root;
	private int sets;
	private List<Integer> interval = new ArrayList<>();
	private int maxLen;

	SuffixTree(List<String> buff, int maxLen) {
		this.maxLen = maxLen;
		this.buff = buff;
		this.size = buff.size();
		this.root = new Node(-1, 0, -1, this.maxLen);
		this.root.link = this.root;
		this.sets = 0;
		this.interval.add(0);

		makeSuffixTree(0);
		this.sets++;
	}

	private void makeSuffixTree(int bi) {
		int ni = 0;
		int si = bi;

		Node node = this.root;
		Node prev = node;
		int nlen = 0;

		while (bi < this.size) {
			if (ni == nlen) {
				Node child = node.searchChild(this.buff, this.size, this.buff.get(bi));
				if (child == null) {
					if (si == bi) {
						this.root.insertChild(new Node(bi, 0, this.sets, this.maxLen));
						si++;
						bi++;
					} else {
						Return rn = insertLeaf(node, bi, si);
						prev = rn.getPrev();
						node = rn.getNode();
						nlen = rn.getNlen();
						ni = rn.getNi();
						si = rn.getSi();
						bi = rn.getBi();
					}
				} else {
					prev = node;
					node = child;
					nlen = child.getLength();
					ni = 1;
					bi++;
				}
			} else {
				if (!buff.get(bi).equals(buff.get(node.start + ni))) {
					Return rn = divideNode(prev, node, ni, bi, si);
					prev = rn.getPrev();
					node = rn.getNode();
					nlen = rn.getNlen();
					ni = rn.getNi();
					si = rn.getSi();
					bi = rn.getBi();
				} else {
					ni++;
					bi++;
				}
			}
		}
		if (si < bi) {
			if (nlen == ni) {
				insertLeaf(node, bi, si);
			} else {
				divideNode(prev, node, ni, bi, si);
			}
		}
	}

	private Return insertLeaf(Node node, int bi, int si) {
		node.insertChild(new Node(bi, node.depth + node.getLength(), this.sets, this.maxLen));
		node = node.link;
		si++;
		while (si < bi) {
			if (bi < this.size) {
				Node child = node.searchChild(this.buff, this.size, this.buff.get(bi));
				if (child != null) {
					return new Return(node, child, child.getLength(), 1, si, bi + 1);
				}
			}
			node.insertChild(new Node(bi, node.depth + node.getLength(), this.sets, this.maxLen));
			node = node.link;
			si++;
		}
		return new Return(this.root, this.root, 0, 0, si, bi);
	}

	private Return divideNode(Node prev, Node node, int ni, int bi, int si) {
		Node linkNode = insertNode(prev, node, bi, ni);
		si++;
		while (si < bi) {
			SearchNextNode searchNextNode = searchNextNode(prev.link, si, bi);
			prev = searchNextNode.getPrev();
			node = searchNextNode.getNode();
			int match = searchNextNode.getMatch();
			if (match == 0) {
				if (bi < this.size) {
					Node child = node.searchChild(this.buff, this.size, this.buff.get(bi));
					if (child != null) {
						linkNode.link = node;
						return new Return(node, child, child.getLength(), 1, si, bi + 1);
					}
				}
				linkNode.link = node;
				return insertLeaf(node, bi, si);
			}
			linkNode.link = insertNode(prev, node, bi, match);
			linkNode = linkNode.link;
			si++;
		}
		linkNode.link = this.root;
		return new Return(this.root, this.root, 0, 0, si, bi);
	}

	private SearchNextNode searchNextNode(Node node, int i, int end) {
		Node prev = node;
		if (node != this.root) {
			i += node.child.depth;
		}
		while (i < end) {
			Node child = node.searchChild(this.buff, this.size, this.buff.get(i));
			int j = child.getLength();
			if (i + j > end) {
				return new SearchNextNode(node, child, end - i);
			}
			i += j;
			prev = node;
			node = child;
		}
		return new SearchNextNode(prev, node, 0);
	}

	private Node insertNode(Node parent, Node node, int match, int submatch) {
		//if (submatch > 0) {
		Node newNode = new Node(node.start, node.depth, -1, this.maxLen);
		node.depth += submatch;
		node.start += submatch;

		parent.deleteChild(node);
		parent.insertChild(newNode);
		newNode.insertChild(node);

		Node leaf = new Node(match, node.depth, this.sets, this.maxLen);
		newNode.insertChild(leaf);
		return newNode;
	}

	public void add(List<String> buff, int maxLen) {
		this.maxLen = maxLen;
		this.buff.addAll(buff);
		int n = this.size;
		this.interval.add(n);
		this.size = this.buff.size();
		makeSuffixTree(n);
		this.sets++;
	}

	public List<String> commonString(int n) {
		List<String> a = new ArrayList<>();
		searchCommonSubstring(this.root, n, a);
		return a;
	}

	private int[] searchCommonSubstring(Node node, int n, List<String> a) {
		if (node.child == null) {
			int cnt[] = new int[this.sets];
			for (int i = 0; i < this.sets; i++) {
				cnt[i] = 0;
			}
			cnt[node.id] = 1;
			return cnt;
		} else {
			Node x = node.child;
			int cnt[] = new int[this.sets];
			for (int i = 0; i < this.sets; i++) {
				cnt[i] = 0;
			}
			while (x != null) {
				addCnt(cnt, searchCommonSubstring(x, n, a));
				x = x.bros;
			}
			int l = node.getLength();
			if (l > 0) {
				for (int i = 0; i < this.sets; i++) {
					if (cnt[i] == 0) return cnt;
				}
				if (node.depth + l >= n) {
					for (int i = node.start - node.depth; i < node.start + l; i++) {
						a.add(this.buff.get(i));
					}
					//a.add(Arrays.toString(cnt));
					a.add("\n");
				}
			}
			return cnt;
		}
	}

	private void addCnt(int[] src, int[] dst) {
		for (int i = 0; i < this.sets; i++) {
			src[i] += dst[i];
		}
	}

	public List<PatternIndex> searchPatternAll(List<String> seq) {
		Node node = this.searchPatternSub(seq);
		if (node == null) {
			return null;
		}

		List<PatternIndex> pis = new ArrayList<>();

		for (Node x : node.traverseLeaf()) {
			pis.add(new PatternIndex(x.id, (x.start - x.depth - this.interval.get(x.id))));
		}

		return pis;
	}

	private Node searchPatternSub(List<String> seq) {
		int size = seq.size();
		Node node = this.root;

		int i = 0;
		while (i < size) {
			Node child = node.searchChild(this.buff, this.size, seq.get(i));
			if (child == null) {
				return null;
			}

			int j = 1;
			int k = child.getLength();

			while (j < k) {
				if (i + j == size) {
					return child;
				}
				if (i + j == this.size || !seq.get(i + j).equals(this.buff.get(child.start + j))) {
					return null;
				}
				j++;
			}

			i += j;
			node = child;
		}
		return node;
	}

	/*	public List<String> longestCommonSubstring() {
		List<Node> a = new ArrayList<>();
		a.add(this.root);
		this.searchLongestSubstring(this.root, a);
		List<String> b = new ArrayList<>();
		for (int i = a.get(0).start - a.get(0).depth; i < a.get(0).start + a.get(0).getLength(); i++) {
			b.add(buff.get(i));
		}
		return b;
	}

	private int searchLongestSubstring(Node node, List<Node> a) {
		if (node.child == null) {
			return 1 << node.id;
		} else {
			Node x = node.child;
			int c = 0;

			while (x != null) {
				c |= this.searchLongestSubstring(x, a);
				x = x.bros;
			}

			if (c == (1 << this.sets) - 1) {
				if (a.get(0).child.depth < node.child.depth) {
					a.set(0, node);
				}
			}
			return c;
		}
	}*/
}
