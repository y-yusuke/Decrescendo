package decrescendo.clonedetector;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
		long start, stop;
		double time;

		MethodLexer methodLexer = null;
		if (Config.language.equals("java"))
			methodLexer = new JavaMethodLexer();

		if (methodLexer == null)
			throw new AssertionError();

		System.out.println("Parsing Method...");
		start = System.currentTimeMillis();
		HashSet<Method> methodSet = methodLexer.getMethodSet(files);
		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time (Parse) :")).append(time).append(" s\n").toString());

		if (Config.method) {
			System.out.println("Detecting Method Code Clone...");
			start = System.currentTimeMillis();
			List<List<Method>> methodCloneSets = getMethodCloneSets(methodSet);
			stop = System.currentTimeMillis();
			time = (double) (stop - start) / 1000D;
			System.out.println((new StringBuilder("Execution Time (Match) :")).append(time).append(" s\n").toString());

			System.out.println("Outputting Method Code Clone Result...");
			start = System.currentTimeMillis();
			methodSet = outputMethodCloneResult(methodCloneSets, methodSet);
			stop = System.currentTimeMillis();
			time = (double) (stop - start) / 1000D;
			System.out.println((new StringBuilder("Execution Time (Output) :")).append(time).append(" s\n").toString());

			System.out.println("Detected " + clonePairId + " Method Code Clone Pair\n");
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
				// 3 ... representative file and method
				Method tmpMethod = methodCloneSet.get(0);
				methodSet.remove(tmpMethod);
				if (tmpMethod.isRepresentative() == 0)
					tmpMethod.setRepresentative(2);
				if (tmpMethod.isRepresentative() == 1)
					tmpMethod.setRepresentative(3);
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
		List<Method> otherFile1 = new ArrayList<>();
		List<Method> otherFile2 = new ArrayList<>();

		DBManager.searchfc1Statement.setString(1, methodClone1.getPath());

		try (ResultSet results = DBManager.searchfc1Statement.executeQuery()) {
			otherFile1 = getOther(results, methodClone1.getOrder());
			otherFile1.forEach(e -> insertMethodCloneInRepresentativeFile(e, methodClone2, cloneSetId));
		}

		DBManager.searchfc2Statement.setString(1, methodClone1.getPath());

		try (ResultSet results = DBManager.searchfc2Statement.executeQuery()) {
			otherFile2 = getOther(results, methodClone1.getOrder());
			otherFile2.forEach(e -> insertMethodCloneInRepresentativeFile(e, methodClone2, cloneSetId));
		}

		List<Method> finalOtherFile = otherFile2;
		otherFile1.forEach(e1 -> finalOtherFile.forEach(e2 -> insertMethodCloneInRepresentativeFile(e1, e2, cloneSetId)));
	}

	private List<Method> getOther(ResultSet results, int order)
			throws SQLException {
		List<Method> otherFile1 = new ArrayList<>();

		while (results.next()) {
			DBManager.searchdmStatement.setString(1, results.getString(1));
			DBManager.searchdmStatement.setInt(2, order);

			try (ResultSet results2 = DBManager.searchdmStatement.executeQuery()) {
				while (results2.next()) {
					Method method = new Method();
					method.setPath(results2.getString(1));
					method.setName(results2.getString(2));
					method.setOriginalHash(results2.getString(6));
					method.setNormalizedHash(results2.getString(7));
					method.setNormalizedTokens(null);
					method.setOriginalTokens(null);
					method.setLineNumberPerToken(null);
					method.setStartLine(results2.getInt(4));
					method.setEndLine(results2.getInt(5));
					method.setOrder(results2.getInt(3));
					method.setRepresentative(0);
					otherFile1.add(method);
				}
			}
		}
		return otherFile1;
	}


	private void insertMethodCloneInRepresentativeFile(Method methodClone1, Method methodClone2, int cloneSetId) {
		DataAccessObject.insertMethodCloneInfo(methodClone1, methodClone2, clonePairId, cloneSetId);
		clonePairId++;
	}

}
