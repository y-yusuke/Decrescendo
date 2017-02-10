package decrescendo.suffixtree;

import decrescendo.codefragmentclonest.CodeFragmentClonePairST;
import decrescendo.granularity.Method;
import decrescendo.hash.Hash;
import decrescendo.hash.HashCreator;

import java.util.ArrayList;
import java.util.List;

public class ExecuteSuffixTree {
	private SuffixTree suffixTree;
	private int maxLen;
	private List<String> a;
	private List<String> b;
	List<Method> list;
	List<CloneIndex>[][] memo;
	private int arraySize = 1000;

	public ExecuteSuffixTree(List<Method> list) {
		this.list = list;
		long start, stop;
		double time;


		System.out.println("Allocating Objects on the Heap ...");
		start = System.currentTimeMillis();
		memo = new ArrayList[arraySize][arraySize];
/*
		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = i + 1; j < list.size(); j++) {
				memo[i][j] = null;
			}
		}
*/
		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time  :")).append(time).append(" s\n").toString());
	}

	public List<CodeFragmentClonePairST> run() {
		long start, stop;
		double time;
		System.out.println("Making Suffix Tree ...");
		start = System.currentTimeMillis();

		makeSuffixTree();

		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time  :")).append(time).append(" s\n").toString());

		return searchCommonSequence();
		//printNode(suffixTree.root, a);
	}

	private void makeSuffixTree() {
		Method method = list.get(0);
		this.a = new ArrayList<>(method.normalizedTokens);
		a.add("$" + 0);
		maxLen = a.size();
		suffixTree = new SuffixTree(a, maxLen);
		for (int i = 1; i < list.size(); i++) {
			method = list.get(i);
			b = new ArrayList<>(method.normalizedTokens);
			b.add("$" + i);
			maxLen = b.size();
			suffixTree.add(b, maxLen);
			//System.out.println("finish " + i);
		}
	}

	private List<CodeFragmentClonePairST> searchCommonSequence() {
		long start, stop;
		double time;
		System.out.println("Searching Repeated Substring ...");
		start = System.currentTimeMillis();

		List<List<PatternIndex>> temp = new ArrayList<>();

		/*List<String> c = */
		suffixTree.repeatedSubstring(50, 2, temp);

		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time  :")).append(time).append(" s\n").toString());

/*		System.out.println("Searching Pattern Index ...");
		start = System.currentTimeMillis();*/

/*		List<String> tmp = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

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
						if (pi1.getId() < pi2.getId()) {
							index1 = pi1.getIndex();
							id1 = pi1.getId();
							index2 = pi2.getIndex();
							id2 = pi2.getId();
						} else if (pi1.getId() > pi2.getId()) {
							index1 = pi2.getIndex();
							id1 = pi2.getId();
							index2 = pi1.getIndex();
							id2 = pi1.getId();
						} else {
*//*							if (pi1.getIndex() < pi2.getIndex()) {
								index1 = pi1.getIndex();
								id1 = pi1.getId();
								index2 = pi2.getIndex();
								id2 = pi2.getId();
							} else {
								index1 = pi2.getIndex();
								id1 = pi2.getId();
								index2 = pi1.getIndex();
								id2 = pi1.getId();
							}*//*
							continue;
						}

						boolean flag = false;

						int arrayId1 = id1 % arraySize;
						int arrayId2 = id2 % arraySize;

						if (memo[arrayId1][arrayId2] == null) {
							memo[arrayId1][arrayId2] = new ArrayList<>();
							CloneIndex ci = new CloneIndex(id1, id2, index1, index2, size);
							memo[arrayId1][arrayId2].add(ci);
							continue;
						}
						for (CloneIndex ci2 : memo[arrayId1][arrayId2]) {
							if (id1 != ci2.getId1() || id2 != ci2.getId2()) {
								continue;
							}

							int index3 = ci2.getIndex1();
							int index4 = ci2.getIndex2();
							int size2 = ci2.getSize();

							if (index1 >= index3 && index2 >= index4) {
								if (index1 + size <= index3 + size2 && index2 + size <= index4 + size2) {
									flag = true;
									break;
								}
							}

							if (index1 == index3 && index2 == index4) {
								if (index1 + size > index3 + size2 && index2 + size > index4 + size2) {
									ci2.setSize(size);
									flag = true;
									break;
								} else if (index1 + size <= index3 + size2 && index2 + size <= index4 + size2) {
									flag = true;
									break;
								}
							}

							if (index1 + size == index3 + size2 && index2 + size == index4 + size2) {
								if (index1 < index3 && index2 < index4) {
									ci2.setIndex1(index1);
									ci2.setIndex2(index2);
									ci2.setSize(size);
									flag = true;
									break;
								} else if (index1 >= index3 && index2 >= index4) {
									flag = true;
									break;
								}
							}
						}
						if (!flag) {
							CloneIndex ci = new CloneIndex(id1, id2, index1, index2, size);
							memo[arrayId1][arrayId2].add(ci);
						}
					}
				}

				tmp = new ArrayList<>();
				sb = new StringBuilder();
			} else {
				tmp.add(c.get(i));
				sb.append(c.get(i));
			}
		}*/

/*		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time  :")).append(time).append(" s\n").toString());*/


		System.out.println("Generating Clone Pair List ...");
		start = System.currentTimeMillis();

		for (List<PatternIndex> pis : temp) {
			int index1;
			int id1;
			int index2;
			int id2;
			int size;

			for (int p = 0; p < pis.size() - 1; p++) {
				PatternIndex pi1 = pis.get(p);
				size = pi1.getSize();
				for (int q = p + 1; q < pis.size(); q++) {
					PatternIndex pi2 = pis.get(q);
					if (pi1.getId() < pi2.getId()) {
						index1 = pi1.getIndex();
						id1 = pi1.getId();
						index2 = pi2.getIndex();
						id2 = pi2.getId();
					} else if (pi1.getId() > pi2.getId()) {
						index1 = pi2.getIndex();
						id1 = pi2.getId();
						index2 = pi1.getIndex();
						id2 = pi1.getId();
					} else {
/*						if (pi1.getIndex() < pi2.getIndex()) {
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

					boolean flag = false;

					int arrayId1 = id1 % arraySize;
					int arrayId2 = id2 % arraySize;

					if (memo[arrayId1][arrayId2] == null) {
						memo[arrayId1][arrayId2] = new ArrayList<>();
						CloneIndex ci = new CloneIndex(id1, id2, index1, index2, size);
						memo[arrayId1][arrayId2].add(ci);
						continue;
					}
					for (CloneIndex ci2 : memo[arrayId1][arrayId2]) {
						if (id1 != ci2.getId1() || id2 != ci2.getId2()) {
							continue;
						}

						int index3 = ci2.getIndex1();
						int index4 = ci2.getIndex2();
						int size2 = ci2.getSize();

						if (index1 >= index3 && index2 >= index4) {
							if (index1 + size <= index3 + size2 && index2 + size <= index4 + size2) {
								flag = true;
								break;
							}
						}

						if (index1 == index3 && index2 == index4) {
							if (index1 + size > index3 + size2 && index2 + size > index4 + size2) {
								ci2.setSize(size);
								flag = true;
								break;
							} else if (index1 + size <= index3 + size2 && index2 + size <= index4 + size2) {
								flag = true;
								break;
							}
						}

						if (index1 + size == index3 + size2 && index2 + size == index4 + size2) {
							if (index1 < index3 && index2 < index4) {
								ci2.setIndex1(index1);
								ci2.setIndex2(index2);
								ci2.setSize(size);
								flag = true;
								break;
							} else if (index1 >= index3 && index2 >= index4) {
								flag = true;
								break;
							}
						}
					}
					if (!flag) {
						CloneIndex ci = new CloneIndex(id1, id2, index1, index2, size);
						memo[arrayId1][arrayId2].add(ci);
					}
				}
			}
		}

		List<CodeFragmentClonePairST> cfClonePairList = new ArrayList<>();

		for (int i = 0; i < arraySize; i++) {
			for (int j = 0; j < arraySize; j++) {
				if (memo[i][j] == null) {
					continue;
				}
				for (CloneIndex ci : memo[i][j]) {
					Method clone1 = list.get(ci.getId1());
					Method clone2 = list.get(ci.getId2());
					int index1 = ci.getIndex1();
					int index2 = ci.getIndex2();
					int size = ci.getSize();

					List<String> tokens = clone1.normalizedTokens;
					StringBuilder sb2 = new StringBuilder();
					for (int p = index1; p < index1 + size; p++) {
						sb2.append(tokens.get(p));
					}

					cfClonePairList.add(new CodeFragmentClonePairST(clone1, clone2, new Hash(HashCreator.getHash(sb2.toString())), index1, index2, ci.getSize()));
				}
			}
		}

		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time  :")).append(time).append(" s\n").toString());

		return cfClonePairList;
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
