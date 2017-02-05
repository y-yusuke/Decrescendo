package decrescendo.suffixtree;

import decrescendo.clonedetector.CodeFragmentCloneDetectorST;
import decrescendo.codefragmentclonest.CodeFragmentClonePairST;
import decrescendo.granularity.Method;
import decrescendo.hash.Hash;
import decrescendo.hash.HashCreator;

import java.util.ArrayList;
import java.util.List;

public class ExecuteSuffixTree implements Runnable {
	private SuffixTree suffixTree;
	private int maxLen;
	private final Method method1;
	private final Method method2;
	private List<String> a;
	private List<String> b;

	public ExecuteSuffixTree(Method method1, Method method2) {
		this.method1 = method1;
		this.method2 = method2;
		this.a = new ArrayList<>(method1.normalizedTokens);
		this.b = new ArrayList<>(method2.normalizedTokens);
	}

	public void run() {
		makeSuffixTree();
		searchCommonSequence();
		//printNode(suffixTree.root, a);
	}

	private void makeSuffixTree() {
		a.add("$1");
		b.add("&2");
		maxLen = a.size();
		suffixTree = new SuffixTree(a, maxLen);
		maxLen = b.size();
		suffixTree.add(b, maxLen);
	}

	private void searchCommonSequence() {
		List<String> c = suffixTree.commonString(50);

		List<String> tmp = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		List<CloneIndex> cis = new ArrayList<>();

		for (int i = 0; i < c.size(); i++) {
			if (c.get(i).equals("\n")) {
				List<PatternIndex> pis = suffixTree.searchPatternAll(tmp);

				if (pis == null) {
					tmp = new ArrayList<>();
					sb = new StringBuilder();
					continue;
				}

				int index1;
				int id1;
				int index2;
				int id2;
				int size = tmp.size();

				for (int p = 0; p < pis.size() - 1; p++) {
					PatternIndex pi1 = pis.get(p);
					for (int q = p + 1; q < pis.size(); q++) {
						PatternIndex pi2 = pis.get(q);
						if (pi1.getId() == 0 && pi2.getId() == 1) {
							index1 = pi1.getIndex();
							id1 = pi1.getId();
							index2 = pi2.getIndex();
							id2 = pi2.getId();
						} else if (pi1.getId() == 1 && pi2.getId() == 0) {
							index1 = pi2.getIndex();
							id1 = pi2.getId();
							index2 = pi1.getIndex();
							id2 = pi1.getId();
						} else {
/*							if (pi1.getIndex() < pi2.getIndex()) {
								index1 = pi1.getIndex();
								id1 = pi1.getId();
								index2 = pi2.getIndex();
								id2 = pi2.getId();
							} else {
								index1 = pi2.getIndex();
								id1 = pi2.getId();
								index2 = pi1.getIndex();
								id2 = pi1.getId();
							}*/
							continue;
						}

						CloneIndex ci = new CloneIndex(id1, id2, index1, index2, size, new Hash(HashCreator.getHash(sb.toString())));

						boolean flag = false;
						for (CloneIndex ci2 : cis) {
							if (ci.getId1() != ci2.getId1() || ci.getId2() != ci2.getId2()) {
								continue;
							}

							if (ci.getIndex1() + ci.getSize() != ci2.getIndex1() + ci2.getSize() || ci.getIndex2() + ci.getSize() != ci2.getIndex2() + ci2.getSize()) {
								continue;
							}

							if (ci.getIndex1() < ci2.getIndex1() && ci.getIndex2() < ci2.getIndex2()) {
								ci2.setIndex1(ci.getIndex1());
								ci2.setIndex2(ci.getIndex2());
								ci2.setSize(ci.getSize());
								ci2.setCommonHash(ci.getCommonHash());
								flag = true;
								break;
							} else if (ci.getIndex1() >= ci2.getIndex1() && ci.getIndex2() >= ci2.getIndex2()) {
								flag = true;
								break;
							}
						}
						if (!flag) {
							cis.add(ci);
						}
					}
				}

				tmp = new ArrayList<>();
				sb = new StringBuilder();
			} else {
				tmp.add(c.get(i));
				sb.append(c.get(i));
			}
		}

		for (CloneIndex ci : cis) {
			Method clone1;
			Method clone2;
			int index1;
			int index2;

			if (ci.getId1() == 0) {
				clone1 = method1;
			} else {
				clone1 = method2;
			}

			if (ci.getId2() == 0) {
				clone2 = method1;
			} else {
				clone2 = method2;
			}

			index1 = ci.getIndex1();
			index2 = ci.getIndex2();

			CodeFragmentCloneDetectorST.cfClonePairList.add(new CodeFragmentClonePairST(clone1, clone2, ci.getCommonHash(), index1, index2, ci.getSize()));

/*			System.out.println("-----------------------");
			System.out.println("[" + ci.getId1() + " " + ci.getIndex1() + "]");
			if (ci.getId1() == 0) {
				for (int i = ci.getIndex1(); i < ci.getIndex1() + ci.getSize(); i++) {
					System.out.print(a.get(i));
				}
			} else {
				for (int i = ci.getIndex1(); i < ci.getIndex1() + ci.getSize(); i++) {
					System.out.print(b.get(i));
				}
			}
			System.out.println();

			System.out.println("[" + ci.getId2() + " " + ci.getIndex2() + "]");
			if (ci.getId2() == 0) {
				for (int i = ci.getIndex2(); i < ci.getIndex2() + ci.getSize(); i++) {
					System.out.print(a.get(i));
				}
			} else {
				for (int i = ci.getIndex2(); i < ci.getIndex2() + ci.getSize(); i++) {
					System.out.print(b.get(i));
				}
			}
			System.out.println();*/
		}
	}

	private static void printNode(Node node, List<String> buff) {
		if (node.child == null) {
			for (int i = 0; i < node.depth; i++) {
				System.out.print(" ");
			}
			for (int i = node.start; i < buff.size(); i++) {
				System.out.print(buff.get(i));
			}
			System.out.print("\n");
		} else {
			if (node.start != -1) {
				for (int i = 0; i < node.depth; i++) {
					System.out.print(" ");
				}
				for (int i = node.start; i < node.start + node.getLength(); i++) {
					System.out.print(buff.get(i));
				}
				System.out.print("\n");
			}
			Node child = node.child;
			while (child != null) {
				printNode(child, buff);
				child = child.bros;
			}
		}
	}
}
