package decrescendo.smithwaterman;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import decrescendo.codefragmentclone.CodeFragmentClonePair;
import decrescendo.config.Config;
import decrescendo.granularity.Granularity;
import decrescendo.hash.HashCreator;

public class SmithWaterman<T extends Granularity> implements Callable<List<CodeFragmentClonePair<T>>> {
	T target1, target2;
	private List<byte[]> one, two;
	private int matrix[][];
	private int traceback[][];
	final private int match = 2;
	final private int mismatch = -2;
	final private int gap = -1;

	public SmithWaterman(T target1, T target2) {
		if (target1.getNormalizedSentences().size() >= target2.getNormalizedSentences().size()) {
			this.target1 = target1;
			this.target2 = target2;
			this.one = target1.getNormalizedSentences();
			this.two = target2.getNormalizedSentences();
		} else {
			this.target1 = target2;
			this.target2 = target1;
			this.one = target2.getNormalizedSentences();
			this.two = target1.getNormalizedSentences();
		}
	}

	public List<CodeFragmentClonePair<T>> call() {
		initialMatrix();
		calculateScore();
		// printMatrix();
		return traceBack();
	}

	private void initialMatrix() {
		matrix = new int[one.size()][two.size()];
		traceback = new int[one.size()][two.size()];
		for (int i = 0; i < one.size(); i++) {
			for (int j = 0; j < two.size(); j++) {
				matrix[i][j] = 0;
				traceback[i][j] = 0;
			}
		}
	}

	private void calculateScore() {
		for (int i = 1; i < one.size(); i++) {
			for (int j = 1; j < two.size(); j++) {
				if (java.util.Arrays.equals(one.get(i), two.get(j))) {
					matrix[i][j] = Math.max(0, Math.max(matrix[i - 1][j - 1] + match,
							Math.max(matrix[i - 1][j] + gap, matrix[i][j - 1] + gap)));
				} else {
					matrix[i][j] = Math.max(0, Math.max(matrix[i - 1][j - 1] + mismatch,
							Math.max(matrix[i - 1][j] + gap, matrix[i][j - 1] + gap)));
				}
			}
		}
	}

	private List<CodeFragmentClonePair<T>> traceBack() {
		List<CodeFragmentClonePair<T>> cfClonePairList = new ArrayList<>();
		for (int i = one.size() - 1; i > 0; i--) {
			for (int j = two.size() - 1; j > 0; j--) {
				if (matrix[i][j] != 0 && traceback[i][j] == 0 && java.util.Arrays.equals(one.get(i), two.get(j))) {
					CodeFragmentClonePair<T> cfClonePair = startTraceBack(i, j);
					if (cfClonePair != null)
						cfClonePairList.add(cfClonePair);
				}
			}
		}
		return cfClonePairList;
	}

	private CodeFragmentClonePair<T> startTraceBack(int i, int j) {
		int iL = i;
		int jL = j;
		StringBuffer commonsb = new StringBuffer();
		List<Integer> cloneIndexes1 = new ArrayList<Integer>();
		List<Integer> cloneIndexes2 = new ArrayList<Integer>();
		List<Integer> gapIndexes1 = new ArrayList<Integer>();
		List<Integer> gapIndexes2 = new ArrayList<Integer>();
		int gapTokensize1 = 0;
		int gapTokensize2 = 0;
		int a[] = getLocalMaxCell(i, j);
		i = a[0];
		j = a[1];

		while (i != 0 && j != 0) {
			int max = Math.max(matrix[i - 1][j - 1], Math.max(matrix[i - 1][j], matrix[i][j - 1]));
			// break
			if (max == 0) {
				commonsb.append(HashCreator.convertString(one.get(i)));
				cloneIndexes1.add(i);
				cloneIndexes2.add(j);
				break;
			}
			// diag case
			else if (max == matrix[i - 1][j - 1]) {
				commonsb.append(HashCreator.convertString(one.get(i)));
				cloneIndexes1.add(i);
				cloneIndexes2.add(j);
				i = i - 1;
				j = j - 1;
				// left case
			} else if (max == matrix[i - 1][j]) {
				cloneIndexes1.add(i);
				gapIndexes1.add(i);
				gapTokensize1 += target1.getLineNumberPerSentence().get(i).size();
				i = i - 1;
				// up case
			} else {
				cloneIndexes2.add(j);
				gapIndexes2.add(j);
				gapTokensize2 += target2.getLineNumberPerSentence().get(j).size();
				j = j - 1;
			}
		}
		for (int p = i; p <= iL; p++)
			for (int q = j; q <= jL; q++)
				traceback[p][q] = 1;
		if (checkClone(cloneIndexes1, cloneIndexes2, gapTokensize1, gapTokensize2))
			return new CodeFragmentClonePair<T>(target1, target2,
					HashCreator.convertString(HashCreator.getHash(commonsb.toString())), cloneIndexes1, cloneIndexes2,
					gapIndexes1, gapIndexes2);
		else
			return null;
	}

	private int[] getLocalMaxCell(int i, int j) {
		int a[] = new int[2];
		while (i != 0 && j != 0) {
			int max = Math.max(matrix[i][j],
					Math.max(matrix[i - 1][j - 1], Math.max(matrix[i - 1][j], matrix[i][j - 1])));
			if (max == matrix[i][j]) {
				a[0] = i;
				a[1] = j;
				break;
			} else if (max == matrix[i - 1][j - 1]) {
				i = i - 1;
				j = j - 1;
			} else if (max == matrix[i - 1][j]) {
				i = i - 1;
			} else {
				j = j - 1;
			}
		}
		return a;
	}

	private boolean checkClone(List<Integer> cloneIndexs1, List<Integer> cloneIndexs2, int gapTokensize1,
			int gapTokensize2) {
		int tokensize1 = getTokenSize(target1, cloneIndexs1);
		int tokensize2 = getTokenSize(target2, cloneIndexs2);

		if (tokensize1 < Config.cfMinTokens || tokensize2 < Config.cfMinTokens)
			return false;

		if ((double) gapTokensize1 / tokensize1 >= Config.gapRate
				|| (double) gapTokensize2 / tokensize2 >= Config.gapRate)
			return false;

		return true;
	}

	private int getTokenSize(T clone, List<Integer> cloneIndexs) {
		int tokensize = 0;
		for (int i : cloneIndexs)
			tokensize += clone.getLineNumberPerSentence().get(i).size();
		return tokensize;
	}

	private void printMatrix() {
		for (int i = 0; i < one.size(); i++) {
			for (int j = 0; j < two.size(); j++) {
				System.out.print(String.format("%5d", matrix[i][j]) + " ");
			}
			System.out.println();
		}
	}
}
