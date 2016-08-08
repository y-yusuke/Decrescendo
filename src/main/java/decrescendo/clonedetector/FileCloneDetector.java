package decrescendo.clonedetector;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import decrescendo.config.Config;
import decrescendo.db.DBManager;
import decrescendo.db.DataAcsessObject;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;
import decrescendo.lexer.file.FileLexer;
import decrescendo.lexer.file.JavaFileLexer;
import decrescendo.lexer.method.JavaMethodLexer;
import decrescendo.lexer.method.MethodLexer;
import decrescendo.lexer.sentence.SentenceLexer;

public class FileCloneDetector {

	public FileCloneDetector() {
	}

	public HashSet<File> execute(String path) throws Exception {
		FileLexer fileLexer = null;
		if (Config.language.equals("java"))
			fileLexer = new JavaFileLexer();
		HashSet<File> fileSet = fileLexer.getFileSet(path);

		if (Config.file) {
			System.out.println("Start to detect File Code Clone");
			System.out.print("File : " + fileSet.size() + " -> ");

			List<List<File>> fileCloneSets = getFileCloneSets(fileSet);
			fileSet = outputFileCloneResult(fileCloneSets, fileSet);

			System.out.println(fileSet.size());
			System.out.println("Finish to detect File Code Clone");
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
		int count = 0;
		for (int i = 0; i < fileCloneSets.size(); i++) {
			List<File> fileCloneSet = fileCloneSets.get(i);

			for (int p = 0; p < fileCloneSet.size() - 1; p++) {
				File fileClone1 = fileCloneSet.get(p);

				for (int q = p + 1; q < fileCloneSet.size(); q++) {
					File fileClone2 = fileCloneSet.get(q);

					DataAcsessObject.insertFileCloneInfo(fileClone1, fileClone2, count, i);

					if (count % 1000 == 0)
						DBManager.fcStatement.executeBatch();
					count++;

					if (p == 0) {
						if (Config.method || Config.codefragment) {
							insertDeleteFileInfo(fileClone2);
							fileSet.remove(fileClone2);
						}
					}
				}
			}

			if (Config.method || Config.codefragment) {
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

	private void insertDeleteFileInfo(File file) {
		try {
			if (Config.method) {
				MethodLexer methodLexer = null;
				if (Config.language.equals("java"))
					methodLexer = new JavaMethodLexer();
				HashSet<Method> methodSet = methodLexer.getMethodInfo(file.getPath(), file.getSource(), 0);

				if (methodSet != null) {
					methodSet.forEach(e -> DataAcsessObject.insertDeleteMethodInfo(e));
					DBManager.mStatement.executeBatch();
				}

				if (Config.codefragment) {
					List<Method> separatedMethodList = CodeFragmentCloneDetector.addSeparatedSentenceInfo(methodSet);
					separatedMethodList.forEach(e -> DataAcsessObject.insertDeleteSentenceInfo(e));
					DBManager.sStatement.executeBatch();
				}

			} else if (Config.codefragment) {
				File separatedFile = SentenceLexer.separateSentences(file);
				DataAcsessObject.insertDeleteSentenceInfo(separatedFile);
				DBManager.sStatement.executeBatch();
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
