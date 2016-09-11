package decrescendo.clonedetector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import decrescendo.codefragmentclone.CloneRange;
import decrescendo.codefragmentclone.CodeFragmentClonePair;
import decrescendo.config.Config;
import decrescendo.db.DBManager;
import decrescendo.db.DataAccessObject;
import decrescendo.granularity.File;
import decrescendo.granularity.Granularity;
import decrescendo.granularity.Method;
import decrescendo.lexer.sentence.SentenceLexer;
import decrescendo.smithwaterman.SmithWaterman;

import static java.util.Collections.synchronizedList;

public class CodeFragmentCloneDetector<T extends Granularity> {
	private static int clonePairId;

	public CodeFragmentCloneDetector() {
		clonePairId = 0;
	}

	public void execute(HashSet<T> set) throws SQLException {
		System.out.println("Identifying Sentence...");
		List<T> list = SentenceLexer.addSeparatedSentenceInfo(set);

		System.out.println("Detecting Code Fragment Clone...");
		List<CodeFragmentClonePair<T>> cfClonePairList;
		cfClonePairList = synchronizedList(new ArrayList<CodeFragmentClonePair<T>>());

		int threadsNum = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threadsNum);

		List<Future<List<CodeFragmentClonePair<T>>>> futures = new ArrayList<>();

		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = i + 1; j < list.size(); j++) {
				Future<List<CodeFragmentClonePair<T>>> future = service.submit(new SmithWaterman<>(list.get(i), list.get(j)));
				futures.add(future);
			}
		}

		futures.forEach(e -> {
			try {
				List<CodeFragmentClonePair<T>> tmpCfCloneList = e.get();
				tmpCfCloneList.forEach(cfClonePairList::add);
			} catch (InterruptedException | ExecutionException e1) {
				e1.printStackTrace();
			}
		});

		service.shutdown();
		while (!service.isTerminated()) {
			try {
				service.awaitTermination(100L, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				System.out.println("Interrupted...");
			}
		}

		System.out.println("Outputting Code Fragment Clone Result...");
		List<List<CodeFragmentClonePair<T>>> cfCloneSets = getCodeFragmentCloneSets(cfClonePairList);
		outputCodeFragmentCloneResult(cfCloneSets);

		System.out.println("Detected " + clonePairId + " Code Fragment Clone Pair\n");
	}

	private List<List<CodeFragmentClonePair<T>>> getCodeFragmentCloneSets(List<CodeFragmentClonePair<T>> cfClonePairList) {
		return cfClonePairList.stream()
				.parallel()
				.collect(Collectors.groupingBy(CodeFragmentClonePair::getCommonHash, Collectors.toList()))
				.values()
				.stream()
				.parallel()
				.collect(Collectors.toList());
	}

	private void outputCodeFragmentCloneResult(List<List<CodeFragmentClonePair<T>>> cfCloneSets) throws SQLException {
		for (int cloneSetId = 0; cloneSetId < cfCloneSets.size(); cloneSetId++) {
			List<CodeFragmentClonePair<T>> cfClonePairList = cfCloneSets.get(cloneSetId);

			for (CodeFragmentClonePair<T> clonePair : cfClonePairList) {
				outputCodeFragmentClonePair(clonePair, cloneSetId);

				if (Config.file || Config.method)
					searchCodeFragmentCloneInRepresentativeFileAndMethod(clonePair, cloneSetId);
			}
		}
		DBManager.cfcStatement.executeBatch();
	}

	private static <T extends Granularity> void outputCodeFragmentClonePair(CodeFragmentClonePair<T> clonePair, int cloneSetId) throws SQLException {
		T cf1 = clonePair.getClone1();
		List<Integer> cloneIndexes1 = clonePair.getCloneIndexes1();
		List<Integer> gapIndexes1 = clonePair.getGapIndexes1();
		T cf2 = clonePair.getClone2();
		List<Integer> cloneIndexes2 = clonePair.getCloneIndexes2();
		List<Integer> gapIndexes2 = clonePair.getGapIndexes2();

		CloneRange cloneRange1 = getCloneRange(cf1, cloneIndexes1, gapIndexes1);
		CloneRange cloneRange2 = getCloneRange(cf2, cloneIndexes2, gapIndexes2);

		int type;
		if (cloneRange1.getGapLineSize() != 0 || cloneRange2.getGapLineSize() != 0)
			type = 3;
		else
			type = getCloneType(cf1.getOriginalSentences(), cf2.getOriginalSentences(), cloneIndexes1, cloneIndexes2);

		DataAccessObject.insertCodeFragmentCloneInfo(cf1, cloneRange1, cf2, cloneRange2, type, clonePairId, cloneSetId);

		if (clonePairId % 1000 == 0)
			DBManager.cfcStatement.executeBatch();
		clonePairId++;
	}

	private static <T extends Granularity> CloneRange getCloneRange(T cf, List<Integer> cloneIndexes, List<Integer> gapIndexes) {
		int startIndex = cloneIndexes.get(cloneIndexes.size() - 1);
		List<Integer> list = cf.getLineNumberPerSentence().get(startIndex);
		int startLine = list.get(0);

		int endIndex = cloneIndexes.get(0);
		list = cf.getLineNumberPerSentence().get(endIndex);
		int endLine = list.get(list.size() - 1);

		List<Integer> gapLines = getGapLines(cf, gapIndexes);
		StringBuffer gapSb = getStringOfGapLines(gapLines);
		return new CloneRange(startLine, endLine, gapSb.toString(), gapLines.size());
	}

	private static <T extends Granularity> List<Integer> getGapLines(T cf, List<Integer> gapIndexes) {
		List<Integer> gapLines = new ArrayList<>();
		for (Integer gapIndex : gapIndexes) {
			int tmp = 0;
			for (int p = cf.getLineNumberPerSentence().get(gapIndex).size() - 1; p >= 0; p--) {
				int gapLine = cf.getLineNumberPerSentence().get(gapIndex).get(p);
				if (gapLine != tmp)
					gapLines.add(gapLine);
				tmp = gapLine;
			}
		}
		return gapLines;
	}

	private static StringBuffer getStringOfGapLines(List<Integer> gapLines) {
		StringBuffer gapSb = new StringBuffer();
		for (int i = gapLines.size() - 1; i >= 0; i--) {
			gapSb.append(gapLines.get(i));
			if (i != 0)
				gapSb.append(",");
		}
		return gapSb;
	}

	private static int getCloneType(List<byte[]> originalSentences1, List<byte[]> originalSentences2, List<Integer> cloneIndexes1, List<Integer> cloneIndexes2) {
		for (int i = 0; i < cloneIndexes1.size(); i++) {
			if (!java.util.Arrays.equals(originalSentences2.get(cloneIndexes2.get(i)),
					originalSentences1.get(cloneIndexes1.get(i)))) return 2;
		}
		return 1;
	}

	private void searchCodeFragmentCloneInRepresentativeFileAndMethod(CodeFragmentClonePair<T> cfClonePair, int cloneSetId) throws SQLException {
		T cf1 = cfClonePair.getClone1();
		List<Integer> cloneIndexes1 = cfClonePair.getCloneIndexes1();
		List<Integer> gapIndexes1 = cfClonePair.getGapIndexes1();

		T cf2 = cfClonePair.getClone2();
		List<Integer> cloneIndexes2 = cfClonePair.getCloneIndexes2();
		List<Integer> gapIndexes2 = cfClonePair.getGapIndexes2();

		if (cf1.isRepresentative() == 1)
			searchCodeFragmentCloneInRepresentativeFile(cf1, cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, cloneSetId);

		if (cf1.isRepresentative() == 2)
			searchCodeFragmentCloneInRepresentativeMethod(cf1, cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, cloneSetId);

		if (cf2.isRepresentative() == 1)
			searchCodeFragmentCloneInRepresentativeFile(cf2, cloneIndexes2, gapIndexes2, cf1, cloneIndexes1, gapIndexes1, cloneSetId);

		if (cf2.isRepresentative() == 2)
			searchCodeFragmentCloneInRepresentativeMethod(cf2, cloneIndexes2, gapIndexes2, cf1, cloneIndexes1, gapIndexes1, cloneSetId);
	}

	private void searchCodeFragmentCloneInRepresentativeFile(
			T cf1, List<Integer> cloneIndexes1, List<Integer> gapIndexes1,
			T cf2, List<Integer> cloneIndexes2, List<Integer> gapIndexes2, int cloneSetId) throws SQLException {
		DBManager.searchfc1Statement.setString(1, cf1.getPath());
		try (ResultSet results = DBManager.searchfc1Statement.executeQuery()) {
			while (results.next()) {
				insertCodeFragmentCloneInRepresentative(results.getString(1), cf1.getOrder(), cf1.getName(),
						cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, cloneSetId);
			}
		}

		DBManager.searchfc2Statement.setString(1, cf1.getPath());
		try (ResultSet results = DBManager.searchfc2Statement.executeQuery()) {
			while (results.next()) {
				insertCodeFragmentCloneInRepresentative(results.getString(1), cf1.getOrder(), cf1.getName(),
						cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, cloneSetId);
			}
		}
	}

	private void searchCodeFragmentCloneInRepresentativeMethod(
			T cf1, List<Integer> cloneIndexes1, List<Integer> gapIndexes1,
			T cf2, List<Integer> cloneIndexes2, List<Integer> gapIndexes2, int cloneSetId) throws SQLException {
		DBManager.searchmc1Statement.setString(1, cf1.getPath());
		DBManager.searchmc1Statement.setInt(2, cf1.getOrder());
		try (ResultSet results = DBManager.searchmc1Statement.executeQuery()) {
			while (results.next()) {
				insertCodeFragmentCloneInRepresentative(results.getString(1), results.getInt(2), results.getString(3),
						cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, cloneSetId);
			}
		}

		DBManager.searchmc2Statement.setString(1, cf1.getPath());
		DBManager.searchmc2Statement.setInt(2, cf1.getOrder());
		try (ResultSet results = DBManager.searchmc2Statement.executeQuery()) {
			while (results.next()) {
				insertCodeFragmentCloneInRepresentative(results.getString(1), results.getInt(2), results.getString(3),
						cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, cloneSetId);
			}
		}
	}

	private void insertCodeFragmentCloneInRepresentative(
			String path, int order, String name, List<Integer> cloneIndexes1, List<Integer> gapIndexes1,
			T cf2, List<Integer> cloneIndexes2, List<Integer> gapIndexes2, int cloneSetId) throws SQLException {
		List<byte[]> normalizedSentences = new ArrayList<>();
		List<byte[]> originalSentences = new ArrayList<>();
		List<List<Integer>> lineNumberPerSentenceList = new ArrayList<>();
		normalizedSentences.add(null);
		originalSentences.add(null);
		lineNumberPerSentenceList.add(null);

		DBManager.searchdsStatement.setString(1, path);
		DBManager.searchdsStatement.setInt(2, order);
		try (ResultSet results2 = DBManager.searchdsStatement.executeQuery()) {
			while (results2.next()) {
				List<Integer> lineNumberPerSentence = new ArrayList<>();
				originalSentences.add(results2.getBytes(7));
				normalizedSentences.add(results2.getBytes(8));
				lineNumberPerSentence.add(results2.getInt(5));
				lineNumberPerSentence.add(results2.getInt(6));
				lineNumberPerSentenceList.add(lineNumberPerSentence);
			}
		}

		if (Config.method) {
			Method method = new Method();
			method.setPath(path);
			method.setName(name);
			method.setNormalizedHash(null);
			method.setOriginalHash(null);
			method.setNormalizedTokens(null);
			method.setOriginalTokens(null);
			method.setLineNumberPerToken(null);
			method.setStartLine(0);
			method.setEndLine(0);
			method.setOrder(order);
			method.setRepresentative(0);
			method.setNormalizedSentences(normalizedSentences);
			method.setOriginalSentences(originalSentences);
			method.setLineNumberPerSentence(lineNumberPerSentenceList);

			CodeFragmentClonePair<Method> cfClonePair = new CodeFragmentClonePair<>(
					method, (Method) cf2, "", cloneIndexes1, cloneIndexes2, gapIndexes1, gapIndexes2);

			outputCodeFragmentClonePair(cfClonePair, cloneSetId);
		} else {
			File file = new File();
			file.setPath(path);
			file.setSource("");
			file.setNormalizedHash(null);
			file.setOriginalHash(null);
			file.setNormalizedTokens(null);
			file.setOriginalTokens(null);
			file.setLineNumberPerToken(null);
			file.setStartLine(0);
			file.setEndLine(0);
			file.setRepresentative(0);
			file.setNormalizedSentences(normalizedSentences);
			file.setOriginalSentences(originalSentences);
			file.setLineNumberPerSentence(lineNumberPerSentenceList);

			CodeFragmentClonePair<File> cfClonePair = new CodeFragmentClonePair<>(
					file, (File) cf2, "", cloneIndexes1, cloneIndexes2, gapIndexes1, gapIndexes2);

			outputCodeFragmentClonePair(cfClonePair, cloneSetId);
		}
	}

}
