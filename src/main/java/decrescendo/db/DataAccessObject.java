package decrescendo.db;

import java.sql.SQLException;
import java.util.List;

import decrescendo.codefragmentclone.CloneRange;
import decrescendo.granularity.File;
import decrescendo.granularity.Granularity;
import decrescendo.granularity.Method;

public class DataAccessObject {

	public static void insertFileCloneInfo(File fileClone1, File fileClone2, int count, int cloneSetId) {
		try {
			DBManager.fcStatement.setInt(1, count);
			DBManager.fcStatement.setString(2, fileClone1.getPath());
			DBManager.fcStatement.setInt(3, fileClone1.getStartLine());
			DBManager.fcStatement.setInt(4, fileClone1.getEndLine());
			DBManager.fcStatement.setString(5, fileClone2.getPath());
			DBManager.fcStatement.setInt(6, fileClone2.getStartLine());
			DBManager.fcStatement.setInt(7, fileClone2.getEndLine());
			DBManager.fcStatement.setInt(8, cloneSetId);
			if (fileClone1.getOriginalHash().equals(fileClone2.getOriginalHash()))
				DBManager.fcStatement.setInt(9, 1);
			else
				DBManager.fcStatement.setInt(9, 2);
			DBManager.fcStatement.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void insertMethodCloneInfo(Method methodClone1, Method methodClone2, int count, int cloneSetId) {
		try {
			DBManager.mcStatement.setInt(1, count);
			DBManager.mcStatement.setString(2, methodClone1.getPath());
			DBManager.mcStatement.setString(3, methodClone1.getName());
			DBManager.mcStatement.setInt(4, methodClone1.getNum());
			DBManager.mcStatement.setInt(5, methodClone1.getStartLine());
			DBManager.mcStatement.setInt(6, methodClone1.getEndLine());
			DBManager.mcStatement.setString(7, methodClone2.getPath());
			DBManager.mcStatement.setString(8, methodClone2.getName());
			DBManager.mcStatement.setInt(9, methodClone2.getNum());
			DBManager.mcStatement.setInt(10, methodClone2.getStartLine());
			DBManager.mcStatement.setInt(11, methodClone2.getEndLine());
			DBManager.mcStatement.setInt(12, cloneSetId);
			if (methodClone1.getOriginalHash().equals(methodClone2.getOriginalHash()))
				DBManager.mcStatement.setInt(13, 1);
			else
				DBManager.mcStatement.setInt(13, 2);
			DBManager.mcStatement.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static <T extends Granularity> void insertCodeFragmentCloneInfo(T cf1, CloneRange cloneRange1,
			T cf2, CloneRange cloneRange2, int type, int count, int cloneSetId) {
		try {
			DBManager.cfcStatement.setInt(1, count);
			DBManager.cfcStatement.setString(2, cf1.getPath());
			DBManager.cfcStatement.setString(3, cf1.getName());
			DBManager.cfcStatement.setInt(4, cf1.getNum());
			DBManager.cfcStatement.setInt(5, cloneRange1.getStartLine());
			DBManager.cfcStatement.setInt(6, cloneRange1.getEndLine());
			DBManager.cfcStatement.setString(7, cloneRange1.getGapLines());
			DBManager.cfcStatement.setString(8, cf2.getPath());
			DBManager.cfcStatement.setString(9, cf2.getName());
			DBManager.cfcStatement.setInt(10, cf2.getNum());
			DBManager.cfcStatement.setInt(11, cloneRange2.getStartLine());
			DBManager.cfcStatement.setInt(12, cloneRange2.getEndLine());
			DBManager.cfcStatement.setString(13, cloneRange2.getGapLines());
			DBManager.cfcStatement.setInt(14, cloneSetId);
			DBManager.cfcStatement.setInt(15, type);
			DBManager.cfcStatement.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertDeleteMethodInfo(Method e) {
		try {
			DBManager.mStatement.setString(1, e.getPath());
			DBManager.mStatement.setString(2, e.getName());
			DBManager.mStatement.setInt(3, e.getNum());
			DBManager.mStatement.setInt(4, e.getStartLine());
			DBManager.mStatement.setInt(5, e.getEndLine());
			DBManager.mStatement.setString(6, e.getOriginalHash());
			DBManager.mStatement.setString(7, e.getNormalizedHash());
			DBManager.mStatement.addBatch();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public static <T extends Granularity> void insertDeleteSentenceInfo(T e) {
		List<byte[]> normalizedSentences = e.getNormalizedSentences();
		List<byte[]> originalSentences = e.getOriginalSentences();
		List<List<Integer>> lineNumberPerSentenceList = e.getLineNumberPerSentence();
		for (int i = 1; i < normalizedSentences.size(); i++) {
			byte[] normalizedSentence = normalizedSentences.get(i);
			byte[] originalSentence = originalSentences.get(i);
			List<Integer> lineNumbers = lineNumberPerSentenceList.get(i);
			try {
				DBManager.sStatement.setString(1, e.getPath());
				DBManager.sStatement.setString(2, e.getName());
				DBManager.sStatement.setInt(3, e.getNum());
				DBManager.sStatement.setInt(4, i);
				DBManager.sStatement.setInt(5, lineNumbers.get(0));
				DBManager.sStatement.setInt(6, lineNumbers.get(lineNumbers.size() - 1));
				DBManager.sStatement.setBytes(7, originalSentence);
				DBManager.sStatement.setBytes(8, normalizedSentence);
				DBManager.sStatement.addBatch();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
}
