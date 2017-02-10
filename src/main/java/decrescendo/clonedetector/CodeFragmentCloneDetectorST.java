package decrescendo.clonedetector;

import decrescendo.codefragmentclonest.CodeFragmentClonePairST;
import decrescendo.config.Config;
import decrescendo.db.DBManager;
import decrescendo.db.DataAccessObject;
import decrescendo.granularity.Method;
import decrescendo.suffixtree.ExecuteSuffixTree;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CodeFragmentCloneDetectorST {
	private static int clonePairId;
	public List<CodeFragmentClonePairST> cfClonePairList = new ArrayList<>();

	public CodeFragmentCloneDetectorST() {
		clonePairId = 0;
	}

	public void execute(HashSet<Method> set) throws SQLException {
		long start, stop;
		double time;

		List<Method> list = new ArrayList<>(set);

		System.out.println("Detecting Code Fragment Clone...");
		start = System.currentTimeMillis();

		cfClonePairList = new ExecuteSuffixTree(list).run();

		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time (Match) :")).append(time).append(" s\n").toString());


		System.out.println("Outputting Code Fragment Clone Result...");
		start = System.currentTimeMillis();

		List<List<CodeFragmentClonePairST>> cfCloneSets = getCodeFragmentCloneSets(cfClonePairList);
		outputCodeFragmentCloneResult(cfCloneSets);

		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time (Output) :")).append(time).append(" s\n").toString());

		System.out.println("Detected " + clonePairId + " Code Fragment Clone Pair\n");
	}

	private List<List<CodeFragmentClonePairST>> getCodeFragmentCloneSets(List<CodeFragmentClonePairST> cfClonePairList) {
		return cfClonePairList.stream()
				.parallel()
				.collect(Collectors.groupingBy(CodeFragmentClonePairST::getCommonHash, Collectors.toList()))
				.values()
				.stream()
				.parallel()
				.collect(Collectors.toList());
	}

	private void outputCodeFragmentCloneResult(List<List<CodeFragmentClonePairST>> cfCloneSets) throws SQLException {
		for (int cloneSetId = 0; cloneSetId < cfCloneSets.size(); cloneSetId++) {
			List<CodeFragmentClonePairST> cfClonePairList = cfCloneSets.get(cloneSetId);

			for (CodeFragmentClonePairST clonePair : cfClonePairList) {
				outputCodeFragmentClonePair(clonePair, cloneSetId);
				if (Config.file || Config.method) {
					searchCodeFragmentInRepresentativeFileAndMethod(clonePair, cloneSetId);
				}
			}
		}
		DBManager.insertCodeFragmentCloneInfo_storage.executeBatch();
	}

	private void outputCodeFragmentClonePair(CodeFragmentClonePairST clonePair, int cloneSetId) throws SQLException {
		Method clone1 = clonePair.clone1;
		Method clone2 = clonePair.clone2;
		int index1 = clonePair.index1;
		int index2 = clonePair.index2;
		int size = clonePair.size;

		int type = getCloneType(clone1.originalTokens, clone2.originalTokens, index1, index2, size);
		int startLine1 = getLineNumber(clone1.lineNumberPerToken, index1);
		int endLine1 = getLineNumber(clone1.lineNumberPerToken, index1 + size - 1);
		int startLine2 = getLineNumber(clone2.lineNumberPerToken, index2);
		int endLine2 = getLineNumber(clone2.lineNumberPerToken, index2 + size - 1);

		DataAccessObject.insertCodeFragmentClonePairInfoST(clone1, clone2, startLine1, endLine1, startLine2, endLine2, type, clonePairId, cloneSetId);

		if (clonePairId % 10000 == 0) {
			DBManager.insertCodeFragmentCloneInfo_storage.executeBatch();
		}
		clonePairId++;
	}

	private int getLineNumber(List<Integer> lineNumberPerToken, int index) {
		return lineNumberPerToken.get(index);
	}

	private int getCloneType(List<String> originalTokens1, List<String> originalTokens2, int index1, int index2, int size) {
		for (int i = 0; i < size; i++) {
			if (!originalTokens1.get(i + index1).equals(originalTokens2.get(i + index2))) {
				return 2;
			}
		}
		return 1;
	}

	private void searchCodeFragmentInRepresentativeFileAndMethod(CodeFragmentClonePairST cfClonePair, int cloneSetId) throws SQLException {
		Method clone1 = cfClonePair.clone1;
		Method clone2 = cfClonePair.clone2;
		int index1 = cfClonePair.index1;
		int index2 = cfClonePair.index2;
		int size = cfClonePair.size;

		List<Method> otherFile1 = new ArrayList<>();
		List<Method> otherMethod1 = new ArrayList<>();
		List<Method> otherFile2 = new ArrayList<>();
		List<Method> otherMethod2 = new ArrayList<>();


		if (clone1.representative == 1 && Config.file) {
			otherFile1 = searchCodeFragmentInRepresentativeFile(clone1);
		} else if (clone1.representative == 2 && Config.method) {
			otherMethod1 = searchCodeFragmentInRepresentativeMethod(clone1);
		} else if (clone1.representative == 3 && Config.file && Config.method) {
			otherFile1 = searchCodeFragmentInRepresentativeFile(clone1);
			otherMethod1 = searchCodeFragmentInRepresentativeMethod(clone1);
		}

		otherFile1.forEach(e -> insertCodeFragmentCloneInRepresentative(e, index1, clone2, index2, size, cloneSetId));
		otherMethod1.forEach(e -> insertCodeFragmentCloneInRepresentative(e, index1, clone2, index2, size, cloneSetId));


		if (clone2.representative == 1 && Config.file) {
			otherFile2 = searchCodeFragmentInRepresentativeFile(clone2);
		} else if (clone2.representative == 2 && Config.method) {
			otherMethod2 = searchCodeFragmentInRepresentativeMethod(clone2);
		} else if (clone2.representative == 3 && Config.file && Config.method) {
			otherFile2 = searchCodeFragmentInRepresentativeFile(clone2);
			otherMethod2 = searchCodeFragmentInRepresentativeMethod(clone2);
		}

		otherFile2.forEach(e -> insertCodeFragmentCloneInRepresentative(e, index2, clone1, index1, size, cloneSetId));
		otherMethod2.forEach(e -> insertCodeFragmentCloneInRepresentative(e, index2, clone1, index1, size, cloneSetId));


		List<Method> finalOtherFile = otherFile2;
		List<Method> finalOtherMethod = otherMethod2;

		if (otherFile1.size() != 0) {
			otherFile1.forEach(e1 -> {
				finalOtherFile.forEach(e2 -> insertCodeFragmentCloneInRepresentative(e1, index1, e2, index2, size, cloneSetId));
				finalOtherMethod.forEach(e2 -> insertCodeFragmentCloneInRepresentative(e1, index1, e2, index2, size, cloneSetId));
			});
		}

		if (otherMethod1.size() != 0) {
			otherMethod1.forEach(e1 -> {
				finalOtherFile.forEach(e2 -> insertCodeFragmentCloneInRepresentative(e1, index1, e2, index2, size, cloneSetId));
				finalOtherMethod.forEach(e2 -> insertCodeFragmentCloneInRepresentative(e1, index1, e2, index2, size, cloneSetId));
			});
		}
	}

	private List<Method> searchCodeFragmentInRepresentativeFile(Method clone) throws SQLException {

		List<Method> others = new ArrayList<>();

		DBManager.selectFileClonePath1.setString(1, clone.path);
		try (ResultSet results = DBManager.selectFileClonePath1.executeQuery()) {
			while (results.next()) {
				others.add(getOther(results.getString(1), clone.order, clone.name));
			}
		}


		DBManager.selectFileClonePath2.setString(1, clone.path);

		try (ResultSet results = DBManager.selectFileClonePath2.executeQuery()) {
			while (results.next()) {
				others.add(getOther(results.getString(1), clone.order, clone.name));
			}
		}

		return others;
	}

	private List<Method> searchCodeFragmentInRepresentativeMethod(Method clone) throws SQLException {

		List<Method> others = new ArrayList<>();

		DBManager.selectMethodClonePath1.setString(1, clone.path);
		DBManager.selectMethodClonePath1.setInt(2, clone.order);

		try (ResultSet results = DBManager.selectMethodClonePath1.executeQuery()) {
			while (results.next()) {
				others.add(getOther(results.getString(1), results.getInt(2), results.getString(3)));
			}
		}


		DBManager.selectMethodClonePath2.setString(1, clone.path);
		DBManager.selectMethodClonePath2.setInt(2, clone.order);

		try (ResultSet results = DBManager.selectMethodClonePath2.executeQuery()) {
			while (results.next()) {
				others.add(getOther(results.getString(1), results.getInt(2), results.getString(3)));
			}
		}

		return others;
	}

	private Method getOther(String path, int order, String name) throws SQLException {
		List<String> normalizedTokens = new ArrayList<>();
		List<String> originalTokens = new ArrayList<>();
		List<Integer> lineNumberPerToken = new ArrayList<>();

		DBManager.selectDeletedTokens.setString(1, path);
		DBManager.selectDeletedTokens.setInt(2, order);
		try (ResultSet results2 = DBManager.selectDeletedTokens.executeQuery()) {
			while (results2.next()) {
				String originalString = results2.getString(5);
				String normalizeString = results2.getString(6);
				String lineNumberString = results2.getString(4);

				String[] original = originalString.split("\t");
				String[] normalize = normalizeString.split("\t");
				String[] lineNumber = lineNumberString.split("\t");

				int skip = 0;

				for (int i = 0; i < normalize.length; i++) {
					if (original[i + skip].startsWith("\"") && !original[i + skip].endsWith("\"")) {
						StringBuilder tmp = new StringBuilder();
						tmp.append(original[i + skip]);
						for (int j = i + skip + 1; j < original.length; j++) {
							tmp.append(original[j]);
							skip++;
							if (original[j].endsWith("\"")) {
								originalTokens.add(tmp.toString());
								break;
							}
						}
					} else {
						originalTokens.add(original[i + skip]);
					}
					normalizedTokens.add(normalize[i]);
					lineNumberPerToken.add(Integer.parseInt(lineNumber[i]));
				}
			}
		}

		return new Method(path, name, order, lineNumberPerToken.get(0), lineNumberPerToken.get(lineNumberPerToken.size() - 1), null, null, originalTokens, normalizedTokens, lineNumberPerToken);
	}

	private void insertCodeFragmentCloneInRepresentative(Method clone1, int index1, Method clone2, int index2, int size, int cloneSetId) {

		CodeFragmentClonePairST cfClonePair = new CodeFragmentClonePairST(clone1, clone2, null, index1, index2, size);

		try {
			outputCodeFragmentClonePair(cfClonePair, cloneSetId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
