package decrescendo.clonedetector;

import decrescendo.config.Config;
import decrescendo.db.DBManager;
import decrescendo.db.DataAccessObject;
import decrescendo.granularity.CodeFragment;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;
import decrescendo.hash.Hash;
import decrescendo.lexer.method.JavaMethodLexer;
import decrescendo.lexer.method.MethodLexer;
import decrescendo.lexer.sentence.SentenceLexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MethodCloneDetector {
	private final static Logger log = LoggerFactory.getLogger(MethodCloneDetector.class);
	private int clonePairId;

	public MethodCloneDetector() {
		this.clonePairId = 0;
	}

	public HashSet<Method> execute(HashSet<File> files) throws SQLException, IOException {
		long start, stop;
		double time;

		MethodLexer methodLexer;
		switch (Config.language) {
			case "java":
				methodLexer = new JavaMethodLexer();
				break;

			default:
				throw new AssertionError();
		}

		log.info("Parsing Method...");
		start = System.currentTimeMillis();

		HashSet<Method> methodSet = methodLexer.getMethodSet(files);

		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		log.info("Execution Time (Parse) :{} s", time);


		if (Config.method) {
			log.info("Detecting Method Code Clone...");
			start = System.currentTimeMillis();

			List<List<Method>> methodCloneSets = getMethodCloneSets(methodSet);

			stop = System.currentTimeMillis();
			time = (double) (stop - start) / 1000D;
			log.info("Execution Time (Match) :{} s", time);


			log.info("Outputting Method Code Clone Result...");
			start = System.currentTimeMillis();

			outputMethodCloneResult(methodCloneSets, methodSet);

			stop = System.currentTimeMillis();
			time = (double) (stop - start) / 1000D;
			log.info("Execution Time (Output) :{} s", time);

			log.info("Detected {} Method Code Clone Pair", clonePairId);
		}

		return methodSet;
	}

	private List<List<Method>> getMethodCloneSets(HashSet<Method> methodSet) {
		return methodSet.stream()
				.parallel()
				.collect(Collectors.groupingBy(Method::getNormalizedHash, Collectors.toList()))
				.values()
				.stream()
				.parallel()
				.filter(e -> e.size() > 1)
				.collect(Collectors.toList());
	}

	private void outputMethodCloneResult(List<List<Method>> methodCloneSets, HashSet<Method> methodSet) throws SQLException, IOException {
		for (int cloneSetId = 0; cloneSetId < methodCloneSets.size(); cloneSetId++) {
			List<Method> methodCloneSet = methodCloneSets.get(cloneSetId);

			for (int p = 0; p < methodCloneSet.size() - 1; p++) {
				Method methodClone1 = methodCloneSet.get(p);

				for (int q = p + 1; q < methodCloneSet.size(); q++) {
					Method methodClone2 = methodCloneSet.get(q);

					DataAccessObject.insertMethodClonePairInfo(methodClone1, methodClone2, clonePairId, cloneSetId);
					clonePairId++;

					if (clonePairId % 10000 == 0) {
						DBManager.insertMethodCloneInfo_memory.executeBatch();
					}

					if (Config.file) {
						List<Method> otherFile1 = new ArrayList<>();
						List<Method> otherFile2 = new ArrayList<>();
						int finalCloneSetId = cloneSetId;

						if (methodClone1.representative != 0) {
							otherFile1 = searchMethodInRepresentativeFile(methodClone1);
							otherFile1.forEach(e -> insertMethodCloneInRepresentativeFile(e, methodClone2, finalCloneSetId));
						}

						if (methodClone2.representative != 0) {
							otherFile2 = searchMethodInRepresentativeFile(methodClone2);
							otherFile2.forEach(e -> insertMethodCloneInRepresentativeFile(e, methodClone1, finalCloneSetId));
						}

						List<Method> finalOtherFile = otherFile2;
						otherFile1.forEach(e1 -> finalOtherFile.forEach(e2 -> insertMethodCloneInRepresentativeFile(e1, e2, finalCloneSetId)));

					}

					if (p == 0) {
						if (Config.codeFragment) {
							insertDeleteMethodInfo(methodClone2);
							methodSet.remove(methodClone2);
						}
					}
				}
			}

			if (Config.codeFragment) {
				// 0 ... not representative file and method
				// 1 ... representative file
				// 2 ... representative method
				// 3 ... representative file and method
				Method tmpMethod = methodCloneSet.get(0);
				methodSet.remove(tmpMethod);

				if (tmpMethod.representative == 0) {
					tmpMethod.representative = 2;
				} else if (tmpMethod.representative == 1) {
					tmpMethod.representative = 3;
				}

				methodSet.add(tmpMethod);
			}
		}

		DBManager.insertMethodCloneInfo_memory.executeBatch();
	}

	private void insertDeleteMethodInfo(Method method) throws SQLException {
		if (Config.smithWaterman) {
			CodeFragment codeFragment = SentenceLexer.separateSentences(method);
			DataAccessObject.insertDeletedSentenceInfo(codeFragment);
			DBManager.insertDeletedSentenceInfo.executeBatch();
		} else if (Config.suffix) {
			DataAccessObject.insertDeletedTokenInfo(method);
			DBManager.insertDeletedTokenInfo.executeBatch();
		}
	}

	private List<Method> searchMethodInRepresentativeFile(Method methodClone) throws SQLException, IOException {
		List<Method> otherFile = new ArrayList<>();

		DBManager.selectFileClonePath1.setString(1, methodClone.path);

		try (ResultSet results = DBManager.selectFileClonePath1.executeQuery()) {
			List<Method> otherFile1 = getOther(results, methodClone.order);
			if (otherFile1 != null) {
				otherFile.addAll(otherFile1);
			}
		}


		DBManager.selectFileClonePath2.setString(1, methodClone.path);

		try (ResultSet results = DBManager.selectFileClonePath2.executeQuery()) {
			List<Method> otherFile2 = getOther(results, methodClone.order);
			if (otherFile2 != null) {
				otherFile.addAll(otherFile2);
			}
		}

		return otherFile;
	}

	private List<Method> getOther(ResultSet results, int order) throws SQLException {
		List<Method> otherFile = new ArrayList<>();

		while (results.next()) {
			DBManager.selectDeletedMethods.setString(1, results.getString(1));
			DBManager.selectDeletedMethods.setInt(2, order);

			try (ResultSet results2 = DBManager.selectDeletedMethods.executeQuery()) {
				while (results2.next()) {
					Method method = new Method(results2.getString(1), results2.getString(2), results2.getInt(3), results2.getInt(4), results2.getInt(5), new Hash(results2.getBytes(7)), new Hash(results2.getBytes(6)), null, null, null);
					otherFile.add(method);
				}
			}
		}

		return otherFile;
	}


	private void insertMethodCloneInRepresentativeFile(Method methodClone1, Method methodClone2, int cloneSetId) {
		DataAccessObject.insertMethodClonePairInfo(methodClone1, methodClone2, clonePairId, cloneSetId);
		clonePairId++;
	}

}
