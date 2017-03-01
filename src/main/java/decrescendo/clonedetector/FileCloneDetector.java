package decrescendo.clonedetector;

import decrescendo.config.Config;
import decrescendo.db.DBManager;
import decrescendo.db.DataAccessObject;
import decrescendo.granularity.CodeFragment;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;
import decrescendo.lexer.file.FileLexer;
import decrescendo.lexer.file.JavaFileLexer;
import decrescendo.lexer.method.JavaMethodLexer;
import decrescendo.lexer.method.MethodLexer;
import decrescendo.lexer.sentence.SentenceLexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class FileCloneDetector {
	private final static Logger log = LoggerFactory.getLogger(FileCloneDetector.class);
	private int clonePairId;

	public FileCloneDetector() {
	}

	public HashSet<File> execute(String path) throws Exception {
		long start, stop;
		double time;


		FileLexer fileLexer;
		switch (Config.language) {
			case "java":
				fileLexer = new JavaFileLexer();
				break;

			default:
				throw new AssertionError();
		}

		log.info("Parsing File...");
		start = System.currentTimeMillis();

		HashSet<File> fileSet = fileLexer.getFileSet(path);

		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		log.info("Execution Time (Parse) :{} s", time);


		if (Config.file) {
			log.info("Detecting File Code Clone...");
			start = System.currentTimeMillis();

			List<List<File>> fileCloneSets = getFileCloneSets(fileSet);

			stop = System.currentTimeMillis();
			time = (double) (stop - start) / 1000D;
			log.info("Execution Time (Match) :{} s", time);


			log.info("Outputting File Code Clone Result...");
			start = System.currentTimeMillis();

			outputFileCloneResult(fileCloneSets, fileSet);

			stop = System.currentTimeMillis();
			time = (double) (stop - start) / 1000D;
			log.info("Execution Time (Output) :{} s", time);

			log.info("Detected {} File Code Clone Pair", clonePairId);
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

	private void outputFileCloneResult(List<List<File>> fileCloneSets, HashSet<File> fileSet)
			throws SQLException {
		for (int cloneSetId = 0; cloneSetId < fileCloneSets.size(); cloneSetId++) {
			List<File> fileCloneSet = fileCloneSets.get(cloneSetId);

			for (int i = 0; i < fileCloneSet.size() - 1; i++) {
				File fileClone1 = fileCloneSet.get(i);

				for (int j = i + 1; j < fileCloneSet.size(); j++) {
					File fileClone2 = fileCloneSet.get(j);

					DataAccessObject.insertFileClonePairInfo(fileClone1, fileClone2, clonePairId, cloneSetId);

					if (clonePairId % 1000 == 0)
						DBManager.insertFileCloneInfo_memory.executeBatch();

					clonePairId++;

					if (i == 0) {
						if (Config.method || Config.codeFragment) {
							insertDeletedFileInfo(fileClone2);
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
				tmpFile.representative = 1;
				fileSet.add(tmpFile);
			}
		}

		DBManager.insertFileCloneInfo_memory.executeBatch();
	}

	private void insertDeletedFileInfo(File e) throws SQLException {
		MethodLexer methodLexer;
		switch (Config.language) {
			case "java":
				methodLexer = new JavaMethodLexer();
				break;

			default:
				throw new AssertionError();
		}

		HashSet<Method> methodSet = methodLexer.getMethodObjects(e.path, e.code, 0);

		if (methodSet != null) {
			methodSet.forEach(DataAccessObject::insertDeletedMethodInfo);
			DBManager.insertDeletedMethodInfo.executeBatch();
		}

		if (methodSet != null && Config.codeFragment) {
			if (Config.smithWaterman) {
				List<CodeFragment> codeFragmentList = SentenceLexer.getCodeFragmentList(methodSet);
				codeFragmentList.forEach(DataAccessObject::insertDeletedSentenceInfo);
				DBManager.insertDeletedSentenceInfo.executeBatch();
			} else if (Config.suffix) {
				methodSet.forEach(DataAccessObject::insertDeletedTokenInfo);
				DBManager.insertDeletedTokenInfo.executeBatch();
			}
		}
	}
}
