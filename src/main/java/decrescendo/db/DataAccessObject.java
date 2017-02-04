package decrescendo.db;

import decrescendo.codefragmentclonesw.CloneRange;
import decrescendo.granularity.CodeFragment;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;
import decrescendo.hash.Hash;

import java.sql.SQLException;
import java.util.List;

public class DataAccessObject {

	public static void insertFileClonePairInfo(File fc1, File fc2, int clonePairId, int cloneSetId) {
		try {
			DBManager.insertFileCloneInfo_memory.setInt(1, clonePairId);
			DBManager.insertFileCloneInfo_memory.setString(2, fc1.path);
			DBManager.insertFileCloneInfo_memory.setString(3, fc2.path);
			DBManager.insertFileCloneInfo_memory.setInt(4, cloneSetId);

			if (fc1.originalHash.equals(fc2.originalHash))
				DBManager.insertFileCloneInfo_memory.setInt(5, 1);
			else
				DBManager.insertFileCloneInfo_memory.setInt(5, 2);

			DBManager.insertFileCloneInfo_memory.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void insertMethodClonePairInfo(Method mc1, Method mc2, int clonePairId, int cloneSetId) {
		try {
			DBManager.insertMethodCloneInfo_memory.setInt(1, clonePairId);
			DBManager.insertMethodCloneInfo_memory.setString(2, mc1.path);
			DBManager.insertMethodCloneInfo_memory.setString(3, mc1.name);
			DBManager.insertMethodCloneInfo_memory.setInt(4, mc1.order);
			DBManager.insertMethodCloneInfo_memory.setInt(5, mc1.startLine);
			DBManager.insertMethodCloneInfo_memory.setInt(6, mc1.endLine);
			DBManager.insertMethodCloneInfo_memory.setString(7, mc2.path);
			DBManager.insertMethodCloneInfo_memory.setString(8, mc2.name);
			DBManager.insertMethodCloneInfo_memory.setInt(9, mc2.order);
			DBManager.insertMethodCloneInfo_memory.setInt(10, mc2.startLine);
			DBManager.insertMethodCloneInfo_memory.setInt(11, mc2.endLine);
			DBManager.insertMethodCloneInfo_memory.setInt(12, cloneSetId);

			if (mc1.originalHash.equals(mc2.originalHash))
				DBManager.insertMethodCloneInfo_memory.setInt(13, 1);
			else
				DBManager.insertMethodCloneInfo_memory.setInt(13, 2);

			DBManager.insertMethodCloneInfo_memory.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertCodeFragmentClonePairInfoSW(
			CodeFragment cf1, CloneRange cloneRange1, CodeFragment cf2, CloneRange cloneRange2, int type, int clonePairId, int cloneSetId) {
		try {
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(1, clonePairId);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(2, cf1.path);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(3, cf1.name);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(4, cf1.order);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(5, cloneRange1.startLine);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(6, cloneRange1.endLine);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(7, cloneRange1.gapLines);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(8, cf2.path);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(9, cf2.name);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(10, cf2.order);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(11, cloneRange2.startLine);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(12, cloneRange2.endLine);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(13, cloneRange2.gapLines);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(14, cloneSetId);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(15, type);
			DBManager.insertCodeFragmentCloneInfo_storage.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertCodeFragmentClonePairInfoST(
			Method clone1, Method clone2, int startLine1, int endLine1, int startLine2, int endLine2, int type, int clonePairId, int cloneSetId) {
		try {
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(1, clonePairId);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(2, clone1.path);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(3, clone1.name);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(4, clone1.order);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(5, startLine1);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(6, endLine1);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(7, "");
			DBManager.insertCodeFragmentCloneInfo_storage.setString(8, clone2.path);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(9, clone2.name);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(10, clone2.order);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(11, startLine2);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(12, endLine2);
			DBManager.insertCodeFragmentCloneInfo_storage.setString(13, "");
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(14, cloneSetId);
			DBManager.insertCodeFragmentCloneInfo_storage.setInt(15, type);
			DBManager.insertCodeFragmentCloneInfo_storage.addBatch();
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

	public static void insertDeletedTokenInfo(Method e) {
		for (int i = 0; i < e.normalizedTokens.size(); i++) {
			String normalizedToken = e.normalizedTokens.get(i);
			String originalToken = e.originalTokens.get(i);
			int lineNumber = e.lineNumberPerToken.get(i);

			try {
				DBManager.insertDeletedTokenInfo.setString(1, e.path);
				DBManager.insertDeletedTokenInfo.setString(2, e.name);
				DBManager.insertDeletedTokenInfo.setInt(3, e.order);
				DBManager.insertDeletedTokenInfo.setInt(4, i);
				DBManager.insertDeletedTokenInfo.setInt(5, lineNumber);
				DBManager.insertDeletedTokenInfo.setString(6, originalToken);
				DBManager.insertDeletedTokenInfo.setString(7, normalizedToken);
				DBManager.insertDeletedTokenInfo.addBatch();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
}
