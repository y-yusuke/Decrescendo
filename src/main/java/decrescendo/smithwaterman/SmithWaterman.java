package decrescendo.smithwaterman;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import decrescendo.codefragmentclone.CodeFragmentClonePair;
import decrescendo.config.Config;
import decrescendo.granularity.Granularity;
import decrescendo.hash.HashCreator;

public class SmithWaterman<T extends Granularity> implements Callable<List<CodeFragmentClonePair<T>>> {
	private T target1;
	private T target2;
	private List<byte[]> one;
	private List<byte[]> two;
	private int[][] matrix;
	private int traceBack[][];
	final private int match = 2;
	final private int mismatch = -2;
	final private int gap = -1;

	public SmithWaterman(T target1, T target2) {
		int compare = target1.getPath().compareTo(target2.getPath());

		if (compare > 0) {
			this.target1 = target1;
			this.target2 = target2;
			this.one = target1.getNormalizedSentences();
			this.two = target2.getNormalizedSentences();
		} else {
			if (compare == 0 && target1.getOrder() > target2.getOrder()) {
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
	}

	public List<CodeFragmentClonePair<T>> call() {
		initialMatrix();
		calculateScore();
		//printMatrix();
		return traceBack();
	}

	private void initialMatrix() {
		matrix = new int[one.size()][two.size()];
		traceBack = new int[one.size()][two.size()];
		for (int i = 0; i < one.size(); i++) {
			for (int j = 0; j < two.size(); j++) {
				matrix[i][j] = 0;
				traceBack[i][j] = 0;
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
				if (matrix[i][j] != 0 && traceBack[i][j] == 0 && java.util.Arrays.equals(one.get(i), two.get(j))) {
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
		StringBuilder commonSb = new StringBuilder();
		List<Integer> cloneIndexes1 = new ArrayList<>();
		List<Integer> cloneIndexes2 = new ArrayList<>();
		List<Integer> gapIndexes1 = new ArrayList<>();
		List<Integer> gapIndexes2 = new ArrayList<>();
		int gapTokenSize1 = 0;
		int gapTokenSize2 = 0;
		int localMaxCell[] = getLocalMaxCell(i, j);
		i = localMaxCell[0];
		j = localMaxCell[1];

		while (i != 0 && j != 0) {
			int max = Math.max(matrix[i - 1][j - 1], Math.max(matrix[i - 1][j], matrix[i][j - 1]));
			// break
			if (max == 0) {
				commonSb.append(HashCreator.convertString(one.get(i)));
				cloneIndexes1.add(i);
				cloneIndexes2.add(j);
				break;
			}
			// diag case
			else if (max == matrix[i - 1][j - 1]) {
				cloneIndexes1.add(i);
				cloneIndexes2.add(j);
				if (java.util.Arrays.equals(one.get(i), two.get(j))) {
					commonSb.append(HashCreator.convertString(one.get(i)));
				} else {
					gapIndexes1.add(i);
					gapTokenSize1 += target1.getLineNumberPerSentence().get(i).size();
					gapIndexes2.add(j);
					gapTokenSize2 += target2.getLineNumberPerSentence().get(j).size();
				}
				i = i - 1;
				j = j - 1;
			}
			// left case
			else if (max == matrix[i - 1][j]) {
				cloneIndexes1.add(i);
				gapIndexes1.add(i);
				gapTokenSize1 += target1.getLineNumberPerSentence().get(i).size();
				i = i - 1;
			}
			// up case
			else {
				cloneIndexes2.add(j);
				gapIndexes2.add(j);
				gapTokenSize2 += target2.getLineNumberPerSentence().get(j).size();
				j = j - 1;
			}
		}
		for (int p = i; p <= iL; p++)
			for (int q = j; q <= jL; q++)
				traceBack[p][q] = 1;
		if (checkClone(cloneIndexes1, cloneIndexes2, gapTokenSize1, gapTokenSize2))
			return new CodeFragmentClonePair<>(target1, target2,
					HashCreator.convertString(HashCreator.getHash(commonSb.toString())), cloneIndexes1, cloneIndexes2,
					gapIndexes1, gapIndexes2);
		else
			return null;
	}

	private int[] getLocalMaxCell(int i, int j) {
		int localMaxCell[] = new int[2];
		while (i != 0 && j != 0) {
			int max = Math.max(0,
					Math.max(matrix[i - 1][j - 1], Math.max(matrix[i - 1][j], matrix[i][j - 1])));
			if (max < matrix[i][j]) {
				localMaxCell[0] = i;
				localMaxCell[1] = j;
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
		return localMaxCell;
	}

	private boolean checkClone(List<Integer> cloneIndexes1, List<Integer> cloneIndexes2, int gapTokenSize1,
							   int gapTokenSize2) {
		int tokenSize1 = getTokenSize(target1, cloneIndexes1);
		int tokenSize2 = getTokenSize(target2, cloneIndexes2);

		if (tokenSize1 < Config.cfMinTokens) return false;
		if (tokenSize2 < Config.cfMinTokens) return false;
		if ((double) gapTokenSize1 / (double) tokenSize1 > Config.gapRate) return false;
		if ((double) gapTokenSize2 / (double) tokenSize2 > Config.gapRate) return false;
		return true;
	}

	private int getTokenSize(T clone, List<Integer> cloneIndexes) {
		int tokenSize = 0;
		for (int i : cloneIndexes)
			tokenSize += clone.getLineNumberPerSentence().get(i).size();
		return tokenSize;
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
