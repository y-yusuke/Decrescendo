package decrescendo.smithwaterman;

import decrescendo.clonedetector.CodeFragmentCloneDetector;
import decrescendo.codefragmentclone.CodeFragmentClonePair;
import decrescendo.config.Config;
import decrescendo.granularity.CodeFragment;
import decrescendo.hash.Hash;
import decrescendo.hash.HashList;

import java.util.ArrayList;
import java.util.List;

public class SmithWaterman implements Runnable {
	private final CodeFragment target1;
	private final CodeFragment target2;
	private final List<Hash> one;
	private final List<Hash> two;
	private Cell[][] matrix;
	private final int match = 2;
	private final int mismatch = -2;
	private final int gap = -1;

	public SmithWaterman(CodeFragment target1, CodeFragment target2) {
		int compare = target1.path.compareTo(target2.path);

		if (compare > 0) {
			this.target1 = target1;
			this.target2 = target2;
			this.one = target1.normalizedSentences;
			this.two = target2.normalizedSentences;
		} else if (compare == 0 && target1.order > target2.order) {
			this.target1 = target1;
			this.target2 = target2;
			this.one = target1.normalizedSentences;
			this.two = target2.normalizedSentences;
		} else {
			this.target1 = target2;
			this.target2 = target1;
			this.one = target2.normalizedSentences;
			this.two = target1.normalizedSentences;
		}
	}

	public void run() {
		initialMatrix();
		calculateScore();
		//printMatrix();
		traceBack();
		matrix = null;
	}

	private void initialMatrix() {
		matrix = new Cell[one.size()][two.size()];
		for (int i = 0; i < one.size(); i++) {
			for (int j = 0; j < two.size(); j++) {
				matrix[i][j] = new Cell(0, false);
			}
		}
	}

	private void calculateScore() {
		for (int i = 1; i < one.size(); i++) {
			for (int j = 1; j < two.size(); j++) {
				if (one.get(i).equals(two.get(j))) {
					matrix[i][j].value = Math.max(0,
							Math.max(matrix[i - 1][j - 1].value + match,
									Math.max(matrix[i - 1][j].value + gap, matrix[i][j - 1].value + gap)));
					matrix[i][j].match = true;
				} else {
					matrix[i][j].value = Math.max(0,
							Math.max(matrix[i - 1][j - 1].value + mismatch,
									Math.max(matrix[i - 1][j].value + gap, matrix[i][j - 1].value + gap)));
				}
			}
		}
	}

	private void traceBack() {
		for (int i = one.size() - 1; i > 0; i--) {
			for (int j = two.size() - 1; j > 0; j--) {
				if (!matrix[i][j].isChecked() && matrix[i][j].isMatch()) {
					CodeFragmentClonePair cfClonePair = startTraceBack(i, j);

					if (cfClonePair != null) {
						CodeFragmentCloneDetector.cfClonePairList.add(cfClonePair);
					}
				}
			}
		}
	}

	private CodeFragmentClonePair startTraceBack(int i, int j) {
		List<Hash> commonHashList = new ArrayList<>();
		List<Integer> cloneIndexes1 = new ArrayList<>();
		List<Integer> cloneIndexes2 = new ArrayList<>();
		List<Integer> gapIndexes1 = new ArrayList<>();
		List<Integer> gapIndexes2 = new ArrayList<>();
		int localMaxCell[] = getLocalMaxCell(i, j);
		int iL = localMaxCell[0];
		int jL = localMaxCell[1];

		while (iL != 0 && jL != 0) {
			int max = Math.max(matrix[iL - 1][jL - 1].value, Math.max(matrix[iL - 1][jL].value, matrix[iL][jL - 1].value));
			// break
			if (max == 0) {
				commonHashList.add(one.get(iL));
				cloneIndexes1.add(iL);
				cloneIndexes2.add(jL);
				break;
			}
			// diag case
			else if (max == matrix[iL - 1][jL - 1].value) {
				cloneIndexes1.add(iL);
				cloneIndexes2.add(jL);
				if (matrix[iL][jL].isMatch()) {
					commonHashList.add(one.get(iL));
				} else {
					gapIndexes1.add(iL);
					gapIndexes2.add(jL);
				}
				iL = iL - 1;
				jL = jL - 1;
			}
			// left case
			else if (max == matrix[iL - 1][jL].value) {
				cloneIndexes1.add(iL);
				gapIndexes1.add(iL);
				iL = iL - 1;
			}
			// up case
			else {
				cloneIndexes2.add(jL);
				gapIndexes2.add(jL);
				jL = jL - 1;
			}
		}

		for (int p = iL; p <= i; p++) {
			for (int q = jL; q <= j; q++) {
				matrix[p][q].switchToChecked();
			}
		}

		if (checkClone(cloneIndexes1, cloneIndexes2, gapIndexes1, gapIndexes2)) {
			return new CodeFragmentClonePair(target1, target2, new HashList(commonHashList), cloneIndexes1, cloneIndexes2, gapIndexes1, gapIndexes2);
		} else {
			return null;
		}
	}

	private int[] getLocalMaxCell(int i, int j) {
		int localMaxCell[] = new int[2];

		while (i != 0 && j != 0) {
			int max = Math.max(0, Math.max(matrix[i - 1][j - 1].value, Math.max(matrix[i - 1][j].value, matrix[i][j - 1].value)));

			if (max < matrix[i][j].value) {
				localMaxCell[0] = i;
				localMaxCell[1] = j;
				break;
			} else if (max == matrix[i - 1][j - 1].value) {
				i = i - 1;
				j = j - 1;
			} else if (max == matrix[i - 1][j].value) {
				i = i - 1;
			} else {
				j = j - 1;
			}
		}

		return localMaxCell;
	}

	private boolean checkClone(List<Integer> cloneIndexes1, List<Integer> cloneIndexes2, List<Integer> gapIndexes1, List<Integer> gapIndexes2) {
		int tokenSize1 = getTokenSize(target1, cloneIndexes1);
		int tokenSize2 = getTokenSize(target2, cloneIndexes2);
		int gapTokenSize1 = getTokenSize(target1, gapIndexes1);
		int gapTokenSize2 = getTokenSize(target2, gapIndexes2);

		if (tokenSize1 < Config.cfMinTokens) return false;
		if (tokenSize2 < Config.cfMinTokens) return false;
		if ((double) gapTokenSize1 / (double) tokenSize1 > Config.gapRate) return false;
		if ((double) gapTokenSize2 / (double) tokenSize2 > Config.gapRate) return false;
		return true;
	}

	private int getTokenSize(CodeFragment cf, List<Integer> indexes) {
		int tokenSize = 0;

		for (int i : indexes) {
			tokenSize += cf.lineNumberPerSentence.get(i).size();
		}

		return tokenSize;
	}

	private void printMatrix() {
		for (int i = 0; i < one.size(); i++) {
			for (int j = 0; j < two.size(); j++) {
				System.out.print(String.format("%5d", matrix[i][j].value) + " ");
			}
			System.out.println();
		}
	}
}

class Cell {
	int value;
	boolean match;
	private boolean checked;

	Cell(final int value, final boolean match) {
		this.value = value;
		this.match = match;
		this.checked = false;
	}

	void switchToChecked() {
		this.checked = true;
	}

	boolean isMatch() {
		return this.match;
	}

	boolean isChecked() {
		return this.checked;
	}
}

