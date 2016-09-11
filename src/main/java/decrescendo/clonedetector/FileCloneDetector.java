package decrescendo.clonedetector;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import decrescendo.config.Config;
import decrescendo.db.DBManager;
import decrescendo.db.DataAccessObject;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;
import decrescendo.lexer.file.FileLexer;
import decrescendo.lexer.file.JavaFileLexer;
import decrescendo.lexer.method.JavaMethodLexer;
import decrescendo.lexer.method.MethodLexer;
import decrescendo.lexer.sentence.SentenceLexer;

public class FileCloneDetector {
	private int clonePairId;

	public FileCloneDetector() {
	}

	public HashSet<File> execute(String path) throws Exception {
		FileLexer fileLexer = null;
		if (Config.language.equals("java"))
			fileLexer = new JavaFileLexer();

		if (fileLexer == null)
			throw new AssertionError();

		System.out.println("Parsing File...");
		HashSet<File> fileSet = fileLexer.getFileSet(path);

		if (Config.file) {
			System.out.println("Detecting File Code Clone...");
			List<List<File>> fileCloneSets = getFileCloneSets(fileSet);

			System.out.println("Outputting File Code Clone Result...");
			fileSet = outputFileCloneResult(fileCloneSets, fileSet);

			System.out.println("Detected " + clonePairId + " File Code Clone Pair\n");
		}

		return fileSet;
	}

	private List<List<File>> getFileCloneSets(HashSet<File> fileSet) {
		return fileSet.stream()
				.parallel()
				.collect(Collectors.groupingBy(File::getNormalizedHash, Collectors.toList()))
				.values()
				.stream()
				.parallel()
				.filter(e -> e.size() > 1).collect(Collectors.toList());
	}

	private HashSet<File> outputFileCloneResult(List<List<File>> fileCloneSets, HashSet<File> fileSet)
			throws SQLException {
		for (int cloneSetId = 0; cloneSetId < fileCloneSets.size(); cloneSetId++) {
			List<File> fileCloneSet = fileCloneSets.get(cloneSetId);

			for (int i = 0; i < fileCloneSet.size() - 1; i++) {
				File fileClone1 = fileCloneSet.get(i);

				for (int j = i + 1; j < fileCloneSet.size(); j++) {
					File fileClone2 = fileCloneSet.get(j);

					DataAccessObject.insertFileCloneInfo(fileClone1, fileClone2, clonePairId, cloneSetId);

					if (clonePairId % 1000 == 0)
						DBManager.fcStatement.executeBatch();

					clonePairId++;

					if (i == 0) {
						if (Config.method || Config.codeFragment) {
							insertDeleteFileInfo(fileClone2);
							fileSet.remove(fileClone2);
						}
					}
				}
			}

			if (Config.method || Config.codeFragment) {
				// 0 ... not representative file
				// 1 ... representative file
				File tmpFile = fileCloneSet.get(0);
				fileSet.remove(tmpFile);
				tmpFile.setRepresentative(1);
				fileSet.add(tmpFile);
			}
		}

		DBManager.fcStatement.executeBatch();

		return fileSet;
	}

	private void insertDeleteFileInfo(File file) throws SQLException {
		if (Config.method) {
			MethodLexer methodLexer = null;
			if (Config.language.equals("java"))
				methodLexer = new JavaMethodLexer();

			if (methodLexer == null)
				throw new AssertionError();

			HashSet<Method> methodSet = methodLexer.getMethodInfo(file.getPath(), file.getSource(), 0);

			if (methodSet != null) {
				methodSet.forEach(DataAccessObject::insertDeleteMethodInfo);
				DBManager.mStatement.executeBatch();
			}

			if (methodSet != null && Config.codeFragment) {
				List<Method> separatedMethodList = SentenceLexer.addSeparatedSentenceInfo(methodSet);
				separatedMethodList.forEach(DataAccessObject::insertDeleteSentenceInfo);
				DBManager.sStatement.executeBatch();
			}
		} else if (Config.codeFragment) {
			File separatedFile = SentenceLexer.separateSentences(file);
			DataAccessObject.insertDeleteSentenceInfo(separatedFile);
			DBManager.sStatement.executeBatch();
		}
	}
}
