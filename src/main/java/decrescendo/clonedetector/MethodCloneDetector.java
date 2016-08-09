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
	private int count;

	public MethodCloneDetector() {
		this.count = 0;
	}

	public HashSet<Method> execute(HashSet<File> files) throws SQLException, IOException {
		System.out.println("Start to detect Method Code Clone");

		MethodLexer methodLexer = null;
		if (Config.language.equals("java"))
			methodLexer = new JavaMethodLexer();
		if (methodLexer == null) throw new AssertionError();
		HashSet<Method> methodSet = methodLexer.getMethodSet(files);

		System.out.print("Method : " + methodSet.size() + " -> ");

		List<List<Method>> methodCloneSets = getMethodCloneSets(methodSet);
		methodSet = outputMethodCloneResult(methodCloneSets, methodSet);

		System.out.println(methodSet.size());
		System.out.println("Finish to detect Method Code Clone");

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
		for (int i = 0; i < methodCloneSets.size(); i++) {
			List<Method> methodCloneSet = methodCloneSets.get(i);

			for (int p = 0; p < methodCloneSet.size() - 1; p++) {
				Method methodClone1 = methodCloneSet.get(p);

				for (int q = p + 1; q < methodCloneSet.size(); q++) {
					Method methodClone2 = methodCloneSet.get(q);

					DataAccessObject.insertMethodCloneInfo(methodClone1, methodClone2, count, i);

					if (count % 1000 == 0)
						DBManager.mcStatement.executeBatch();
					count++;

					if (Config.file) {
						if (methodClone1.isRepresentative() != 0)
							searchMethodCloneInRepresentativeFile(methodClone1, methodClone2, i);

						if (methodClone2.isRepresentative() != 0)
							searchMethodCloneInRepresentativeFile(methodClone2, methodClone1, i);
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
				else
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

	private void searchMethodCloneInRepresentativeFile(Method methodClone1, Method methodClone2, int i)
			throws SQLException, IOException {
		DBManager.searchfc1Statement.setString(1, methodClone1.getPath());

		try (ResultSet results = DBManager.searchfc1Statement.executeQuery()) {
			insertMethodCloneInfoInRepresentativeFile(results, methodClone1.getNum(), methodClone2, i);
		}

		DBManager.searchfc2Statement.setString(1, methodClone1.getPath());

		try (ResultSet results = DBManager.searchfc2Statement.executeQuery()) {
			insertMethodCloneInfoInRepresentativeFile(results, methodClone1.getNum(), methodClone2, i);
		}
	}

	private void insertMethodCloneInfoInRepresentativeFile(ResultSet results, int num, Method methodClone, int i)
			throws SQLException {
		while (results.next()) {
			DBManager.searchdmStatement.setString(1, results.getString(1));
			DBManager.searchdmStatement.setInt(2, num);

			try (ResultSet results2 = DBManager.searchdmStatement.executeQuery()) {
				while (results2.next()) {
					Method methodClone2 = new Method(results2.getString(1), results2.getString(2),
							results2.getString(6), results2.getString(7),
							null, null, null,
							results2.getInt(4), results2.getInt(5), results2.getInt(3), 0);
					DataAccessObject.insertMethodCloneInfo(methodClone, methodClone2, count, i);
					count++;
				}
			}
		}
	}

}
