package decrescendo.db;

import decrescendo.codefragmentclone.CloneRange;
import decrescendo.granularity.CodeFragment;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;
import decrescendo.hash.Hash;

import java.sql.SQLException;
import java.util.List;

public class DataAccessObject {

	public static void insertFileClonePairInfo(File fc1, File fc2, int clonePairId, int cloneSetId) {
		try {
			DBManager.insertFileCloneInfo.setInt(1, clonePairId);
			DBManager.insertFileCloneInfo.setString(2, fc1.path);
			DBManager.insertFileCloneInfo.setInt(3, fc1.startLine);
			DBManager.insertFileCloneInfo.setInt(4, fc1.endLine);
			DBManager.insertFileCloneInfo.setString(5, fc2.path);
			DBManager.insertFileCloneInfo.setInt(6, fc2.startLine);
			DBManager.insertFileCloneInfo.setInt(7, fc2.endLine);
			DBManager.insertFileCloneInfo.setInt(8, cloneSetId);

			if (fc1.originalHash.equals(fc2.originalHash))
				DBManager.insertFileCloneInfo.setInt(9, 1);
			else
				DBManager.insertFileCloneInfo.setInt(9, 2);

			DBManager.insertFileCloneInfo.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void insertMethodClonePairInfo(Method mc1, Method mc2, int clonePairId, int cloneSetId) {
		try {
			DBManager.insertMethodCloneInfo.setInt(1, clonePairId);
			DBManager.insertMethodCloneInfo.setString(2, mc1.path);
			DBManager.insertMethodCloneInfo.setString(3, mc1.name);
			DBManager.insertMethodCloneInfo.setInt(4, mc1.order);
			DBManager.insertMethodCloneInfo.setInt(5, mc1.startLine);
			DBManager.insertMethodCloneInfo.setInt(6, mc1.endLine);
			DBManager.insertMethodCloneInfo.setString(7, mc2.path);
			DBManager.insertMethodCloneInfo.setString(8, mc2.name);
			DBManager.insertMethodCloneInfo.setInt(9, mc2.order);
			DBManager.insertMethodCloneInfo.setInt(10, mc2.startLine);
			DBManager.insertMethodCloneInfo.setInt(11, mc2.startLine);
			DBManager.insertMethodCloneInfo.setInt(12, cloneSetId);

			if (mc1.originalHash.equals(mc2.originalHash))
				DBManager.insertMethodCloneInfo.setInt(13, 1);
			else
				DBManager.insertMethodCloneInfo.setInt(13, 2);

			DBManager.insertMethodCloneInfo.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertCodeFragmentClonePairInfo(
			CodeFragment cf1, CloneRange cloneRange1, CodeFragment cf2, CloneRange cloneRange2, int type, int clonePairId, int cloneSetId) {
		try {
			DBManager.insertCodeFragmentCloneInfo.setInt(1, clonePairId);
			DBManager.insertCodeFragmentCloneInfo.setString(2, cf1.path);
			DBManager.insertCodeFragmentCloneInfo.setString(3, cf1.name);
			DBManager.insertCodeFragmentCloneInfo.setInt(4, cf1.order);
			DBManager.insertCodeFragmentCloneInfo.setInt(5, cloneRange1.startLine);
			DBManager.insertCodeFragmentCloneInfo.setInt(6, cloneRange1.endLine);
			DBManager.insertCodeFragmentCloneInfo.setString(7, cloneRange1.gapLines);
			DBManager.insertCodeFragmentCloneInfo.setString(8, cf2.path);
			DBManager.insertCodeFragmentCloneInfo.setString(9, cf2.name);
			DBManager.insertCodeFragmentCloneInfo.setInt(10, cf2.order);
			DBManager.insertCodeFragmentCloneInfo.setInt(11, cloneRange2.startLine);
			DBManager.insertCodeFragmentCloneInfo.setInt(12, cloneRange2.endLine);
			DBManager.insertCodeFragmentCloneInfo.setString(13, cloneRange2.gapLines);
			DBManager.insertCodeFragmentCloneInfo.setInt(14, cloneSetId);
			DBManager.insertCodeFragmentCloneInfo.setInt(15, type);
			DBManager.insertCodeFragmentCloneInfo.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertDeletedMethodInfo(Method e) {
		try {
			DBManager.insertDeletedMethodInfo.setString(1, e.path);
			DBManager.insertDeletedMethodInfo.setString(2, e.name);
			DBManager.insertDeletedMethodInfo.setInt(3, e.order);
			DBManager.insertDeletedMethodInfo.setInt(4, e.startLine);
			DBManager.insertDeletedMethodInfo.setInt(5, e.endLine);
			DBManager.insertDeletedMethodInfo.setBytes(6, e.originalHash.hash);
			DBManager.insertDeletedMethodInfo.setBytes(7, e.normalizedHash.hash);
			DBManager.insertDeletedMethodInfo.addBatch();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public static void insertDeletedSentenceInfo(CodeFragment e) {
		List<Hash> normalizedSentences = e.normalizedSentences;
		List<Hash> originalSentences = e.originalSentences;
		List<List<Integer>> lineNumberPerSentenceList = e.lineNumberPerSentence;

		for (int i = 1; i < normalizedSentences.size(); i++) {
			Hash normalizedSentence = normalizedSentences.get(i);
			Hash originalSentence = originalSentences.get(i);
			List<Integer> lineNumbers = lineNumberPerSentenceList.get(i);

			try {
				DBManager.insertDeletedSentenceInfo.setString(1, e.path);
				DBManager.insertDeletedSentenceInfo.setString(2, e.name);
				DBManager.insertDeletedSentenceInfo.setInt(3, e.order);
				DBManager.insertDeletedSentenceInfo.setInt(4, i);
				DBManager.insertDeletedSentenceInfo.setInt(5, lineNumbers.get(0));
				DBManager.insertDeletedSentenceInfo.setInt(6, lineNumbers.get(lineNumbers.size() - 1));
				DBManager.insertDeletedSentenceInfo.setBytes(7, originalSentence.hash);
				DBManager.insertDeletedSentenceInfo.setBytes(8, normalizedSentence.hash);
				DBManager.insertDeletedSentenceInfo.addBatch();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
}
