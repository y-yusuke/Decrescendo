package decrescendo.clonedetector;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import decrescendo.config.Config;
import decrescendo.db.DBManager;
import decrescendo.db.DataAccessObject;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;
import decrescendo.lexer.method.JavaMethodLexer;
import decrescendo.lexer.method.MethodLexer;
import decrescendo.lexer.sentence.SentenceLexer;

public class MethodCloneDetector {
	private int clonePairId;

	public MethodCloneDetector() {
		this.clonePairId = 0;
	}

	public HashSet<Method> execute(HashSet<File> files) throws SQLException, IOException {
		MethodLexer methodLexer = null;
		if (Config.language.equals("java"))
			methodLexer = new JavaMethodLexer();

		if (methodLexer == null)
			throw new AssertionError();

		System.out.println("Parsing Method...");
		HashSet<Method> methodSet = methodLexer.getMethodSet(files);

		System.out.println("Detecting Method Code Clone...");
		List<List<Method>> methodCloneSets = getMethodCloneSets(methodSet);

		System.out.println("Outputting Method Code Clone Result...");
		methodSet = outputMethodCloneResult(methodCloneSets, methodSet);

		System.out.println("Detected " + clonePairId + " Method Code Clone Pair\n");

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

	private HashSet<Method> outputMethodCloneResult(List<List<Method>> methodCloneSets, HashSet<Method> methodSet)
			throws SQLException, IOException {
		for (int cloneSetId = 0; cloneSetId < methodCloneSets.size(); cloneSetId++) {
			List<Method> methodCloneSet = methodCloneSets.get(cloneSetId);

			for (int p = 0; p < methodCloneSet.size() - 1; p++) {
				Method methodClone1 = methodCloneSet.get(p);

				for (int q = p + 1; q < methodCloneSet.size(); q++) {
					Method methodClone2 = methodCloneSet.get(q);

					DataAccessObject.insertMethodCloneInfo(methodClone1, methodClone2, clonePairId, cloneSetId);

					if (clonePairId % 1000 == 0)
						DBManager.mcStatement.executeBatch();

					clonePairId++;

					if (Config.file) {
						if (methodClone1.isRepresentative() != 0)
							searchMethodCloneInRepresentativeFile(methodClone1, methodClone2, cloneSetId);

						if (methodClone2.isRepresentative() != 0)
							searchMethodCloneInRepresentativeFile(methodClone2, methodClone1, cloneSetId);
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
				Method tmpMethod = methodCloneSet.get(0);
				methodSet.remove(tmpMethod);
				tmpMethod.setRepresentative(2);
				methodSet.add(tmpMethod);
			}
		}
		DBManager.mcStatement.executeBatch();
		return methodSet;
	}

	private void insertDeleteMethodInfo(Method method) throws SQLException {
		Method separatedMethod = SentenceLexer.separateSentences(method);
		DataAccessObject.insertDeleteSentenceInfo(separatedMethod);
		DBManager.sStatement.executeBatch();
	}

	private void searchMethodCloneInRepresentativeFile(Method methodClone1, Method methodClone2, int cloneSetId)
			throws SQLException, IOException {
		DBManager.searchfc1Statement.setString(1, methodClone1.getPath());

		try (ResultSet results = DBManager.searchfc1Statement.executeQuery()) {
			insertMethodCloneInRepresentativeFile(results, methodClone1.getOrder(), methodClone2, cloneSetId);
		}

		DBManager.searchfc2Statement.setString(1, methodClone1.getPath());

		try (ResultSet results = DBManager.searchfc2Statement.executeQuery()) {
			insertMethodCloneInRepresentativeFile(results, methodClone1.getOrder(), methodClone2, cloneSetId);
		}
	}

	private void insertMethodCloneInRepresentativeFile(ResultSet results, int order, Method methodClone, int cloneSetId)
			throws SQLException {
		while (results.next()) {
			DBManager.searchdmStatement.setString(1, results.getString(1));
			DBManager.searchdmStatement.setInt(2, order);

			try (ResultSet results2 = DBManager.searchdmStatement.executeQuery()) {
				while (results2.next()) {
					Method methodClone2 = new Method();
					methodClone2.setPath(results2.getString(1));
					methodClone2.setName(results2.getString(2));
					methodClone2.setOriginalHash(results2.getString(6));
					methodClone2.setNormalizedHash(results2.getString(7));
					methodClone2.setNormalizedTokens(null);
					methodClone2.setOriginalTokens(null);
					methodClone2.setLineNumberPerToken(null);
					methodClone2.setStartLine(results2.getInt(4));
					methodClone2.setEndLine(results2.getInt(5));
					methodClone2.setOrder(results2.getInt(3));
					methodClone2.setRepresentative(0);
					DataAccessObject.insertMethodCloneInfo(methodClone, methodClone2, clonePairId, cloneSetId);
					clonePairId++;
				}
			}
		}
	}

}
