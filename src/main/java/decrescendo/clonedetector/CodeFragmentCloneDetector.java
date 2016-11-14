package decrescendo.clonedetector;

import decrescendo.codefragmentclone.CloneRange;
import decrescendo.codefragmentclone.CodeFragmentClonePair;
import decrescendo.config.Config;
import decrescendo.db.DBManager;
import decrescendo.db.DataAccessObject;
import decrescendo.granularity.CodeFragment;
import decrescendo.granularity.Method;
import decrescendo.hash.Hash;
import decrescendo.lexer.sentence.SentenceLexer;
import decrescendo.smithwaterman.SmithWaterman;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedList;

public class CodeFragmentCloneDetector {
	private static int clonePairId;

	public CodeFragmentCloneDetector() {
		clonePairId = 0;
	}

	public void execute(HashSet<Method> set) throws SQLException {
		long start, stop;
		double time;

		System.out.println("Identifying Sentence...");
		start = System.currentTimeMillis();
		List<CodeFragment> list = SentenceLexer.getCodeFragmentList(set);
		set.clear();

		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time (Parse) :")).append(time).append(" s\n").toString());

		System.out.println("Detecting Code Fragment Clone...");
		start = System.currentTimeMillis();
		List<CodeFragmentClonePair> cfClonePairList = synchronizedList(new ArrayList<CodeFragmentClonePair>());

		int threadsNum = Runtime.getRuntime().availableProcessors();
		ExecutorService service;

		List<Future<List<CodeFragmentClonePair>>> futures = new ArrayList<>();

		for (int i = 0; i < list.size() - 1; i++) {
			service = Executors.newFixedThreadPool(threadsNum);
			for (int j = i + 1; j < list.size(); j++) {
				Future<List<CodeFragmentClonePair>> future = service.submit(new SmithWaterman(list.get(i), list.get(j)));
				futures.add(future);
			}
			service.shutdown();
			while (!service.isTerminated()) {
				try {
					service.awaitTermination(100L, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					System.out.println("Interrupted...");
				}
			}
		}

		futures.forEach(e -> {
			try {
				List<CodeFragmentClonePair> tmpCfCloneList = e.get();
				tmpCfCloneList.forEach(cfClonePairList::add);
			} catch (InterruptedException | ExecutionException e1) {
				e1.printStackTrace();
			}
		});

		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time (Match) :")).append(time).append(" s\n").toString());

		System.out.println("Outputting Code Fragment Clone Result...");
		start = System.currentTimeMillis();
		List<List<CodeFragmentClonePair>> cfCloneSets = getCodeFragmentCloneSets(cfClonePairList);
		outputCodeFragmentCloneResult(cfCloneSets);
		stop = System.currentTimeMillis();
		time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time (Output) :")).append(time).append(" s\n").toString());

		System.out.println("Detected " + clonePairId + " Code Fragment Clone Pair\n");
	}

	private List<List<CodeFragmentClonePair>> getCodeFragmentCloneSets(List<CodeFragmentClonePair> cfClonePairList) {
		return cfClonePairList.stream()
				.parallel()
				.collect(Collectors.groupingBy(CodeFragmentClonePair::getCommonHash, Collectors.toList()))
				.values()
				.stream()
				.parallel()
				.collect(Collectors.toList());
	}

	private void outputCodeFragmentCloneResult(List<List<CodeFragmentClonePair>> cfCloneSets) throws SQLException {
		for (int cloneSetId = 0; cloneSetId < cfCloneSets.size(); cloneSetId++) {
			List<CodeFragmentClonePair> cfClonePairList = cfCloneSets.get(cloneSetId);

			for (CodeFragmentClonePair clonePair : cfClonePairList) {
				outputCodeFragmentClonePair(clonePair, cloneSetId);

				if (Config.file || Config.method)
					searchCodeFragmentCloneInRepresentativeFileAndMethod(clonePair, cloneSetId);
			}
		}
		DBManager.insertCodeFragmentCloneInfo.executeBatch();
	}

	private static void outputCodeFragmentClonePair(CodeFragmentClonePair clonePair, int cloneSetId) throws SQLException {
		CodeFragment cf1 = clonePair.clone1;
		List<Integer> cloneIndexes1 = clonePair.cloneIndexes1;
		List<Integer> gapIndexes1 = clonePair.gapIndexes1;
		CodeFragment cf2 = clonePair.clone2;
		List<Integer> cloneIndexes2 = clonePair.cloneIndexes2;
		List<Integer> gapIndexes2 = clonePair.gapIndexes2;

		CloneRange cloneRange1 = getCloneRange(cf1, cloneIndexes1, gapIndexes1);
		CloneRange cloneRange2 = getCloneRange(cf2, cloneIndexes2, gapIndexes2);

		int type;
		if (cloneRange1.gapLineSize != 0 || cloneRange2.gapLineSize != 0)
			type = 3;
		else
			type = getCloneType(cf1.originalSentences, cf2.originalSentences, cloneIndexes1, cloneIndexes2);

		DataAccessObject.insertCodeFragmentClonePairInfo(cf1, cloneRange1, cf2, cloneRange2, type, clonePairId, cloneSetId);

		if (clonePairId % 1000 == 0)
			DBManager.insertCodeFragmentCloneInfo.executeBatch();
		clonePairId++;
	}

	private static CloneRange getCloneRange(CodeFragment cf, List<Integer> cloneIndexes, List<Integer> gapIndexes) {
		int startIndex = cloneIndexes.get(cloneIndexes.size() - 1);
		List<Integer> list = cf.lineNumberPerSentence.get(startIndex);
		int startLine = list.get(0);

		int endIndex = cloneIndexes.get(0);
		if (cf.lineNumberPerSentence.size() <= endIndex) {
			System.out.println(cf.path);
			System.out.println(cf.name);
			System.out.println(cf.startLine);
			System.out.println(cf.lineNumberPerSentence.size());
			System.out.println(endIndex);
		}
		list = cf.lineNumberPerSentence.get(endIndex);
		int endLine = list.get(list.size() - 1);

		List<Integer> gapLines = getGapLines(cf, gapIndexes);
		StringBuffer gapSb = getStringOfGapLines(gapLines);
		return new CloneRange(startLine, endLine, gapSb.toString(), gapLines.size());
	}

	private static List<Integer> getGapLines(CodeFragment cf, List<Integer> gapIndexes) {
		List<Integer> gapLines = new ArrayList<>();
		for (Integer gapIndex : gapIndexes) {
			int tmp = 0;
			for (int p = cf.lineNumberPerSentence.get(gapIndex).size() - 1; p >= 0; p--) {
				int gapLine = cf.lineNumberPerSentence.get(gapIndex).get(p);
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

	private static int getCloneType(List<Hash> originalSentences1, List<Hash> originalSentences2, List<Integer> cloneIndexes1, List<Integer> cloneIndexes2) {
		for (int i = 0; i < cloneIndexes1.size(); i++) {
			if (!originalSentences2.get(cloneIndexes2.get(i)).equals(originalSentences1.get(cloneIndexes1.get(i)))) {
				return 2;
			}
		}
		return 1;
	}

	private void searchCodeFragmentCloneInRepresentativeFileAndMethod(CodeFragmentClonePair cfClonePair, int cloneSetId) throws SQLException {
		CodeFragment cf1 = cfClonePair.clone1;
		List<Integer> cloneIndexes1 = cfClonePair.cloneIndexes1;
		List<Integer> gapIndexes1 = cfClonePair.gapIndexes1;

		CodeFragment cf2 = cfClonePair.clone2;
		List<Integer> cloneIndexes2 = cfClonePair.cloneIndexes2;
		List<Integer> gapIndexes2 = cfClonePair.gapIndexes2;

		List<CodeFragment> otherFile1 = new ArrayList<>();
		List<CodeFragment> otherMethod1 = new ArrayList<>();
		List<CodeFragment> otherFile2 = new ArrayList<>();
		List<CodeFragment> otherMethod2 = new ArrayList<>();


		if (cf1.representative == 1 && Config.file) {
			otherFile1 = searchCodeFragmentCloneInRepresentativeFile(cf1);
		} else if (cf1.representative == 2 && Config.method) {
			otherMethod1 = searchCodeFragmentCloneInRepresentativeMethod(cf1);
		} else if (cf1.representative == 3 && Config.file && Config.method) {
			otherFile1 = searchCodeFragmentCloneInRepresentativeFile(cf1);
			otherMethod1 = searchCodeFragmentCloneInRepresentativeMethod(cf1);
		}

		otherFile1.forEach(e -> insertCodeFragmentCloneInRepresentative(e, cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, cloneSetId));
		otherMethod1.forEach(e -> insertCodeFragmentCloneInRepresentative(e, cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, cloneSetId));

		if (cf2.representative == 1 && Config.file) {
			otherFile2 = searchCodeFragmentCloneInRepresentativeFile(cf2);
		} else if (cf2.representative == 2 && Config.method) {
			otherMethod2 = searchCodeFragmentCloneInRepresentativeMethod(cf2);
		} else if (cf2.representative == 3 && Config.file && Config.method) {
			otherFile2 = searchCodeFragmentCloneInRepresentativeFile(cf2);
			otherMethod2 = searchCodeFragmentCloneInRepresentativeMethod(cf2);
		}

		otherFile2.forEach(e -> insertCodeFragmentCloneInRepresentative(e, cloneIndexes2, gapIndexes2, cf1, cloneIndexes1, gapIndexes1, cloneSetId));
		otherMethod2.forEach(e -> insertCodeFragmentCloneInRepresentative(e, cloneIndexes2, gapIndexes2, cf1, cloneIndexes1, gapIndexes1, cloneSetId));

		List<CodeFragment> finalOtherFile = otherFile2;
		List<CodeFragment> finalOtherMethod = otherMethod2;
		otherFile1.forEach(e1 -> {
			finalOtherFile.forEach(e2 -> insertCodeFragmentCloneInRepresentative(e1, cloneIndexes1, gapIndexes1, e2, cloneIndexes2, gapIndexes2, cloneSetId));
			finalOtherMethod.forEach(e2 -> insertCodeFragmentCloneInRepresentative(e1, cloneIndexes1, gapIndexes1, e2, cloneIndexes2, gapIndexes2, cloneSetId));
		});
		otherMethod1.forEach(e1 -> {
			finalOtherFile.forEach(e2 -> insertCodeFragmentCloneInRepresentative(e1, cloneIndexes1, gapIndexes1, e2, cloneIndexes2, gapIndexes2, cloneSetId));
			finalOtherMethod.forEach(e2 -> insertCodeFragmentCloneInRepresentative(e1, cloneIndexes1, gapIndexes1, e2, cloneIndexes2, gapIndexes2, cloneSetId));
		});
	}

	private List<CodeFragment> searchCodeFragmentCloneInRepresentativeFile(CodeFragment cf1) throws SQLException {
		DBManager.selectFileClonePath2.setString(1, cf1.path);
		List<CodeFragment> others = new ArrayList<>();
		try (ResultSet results = DBManager.selectFileClonePath2.executeQuery()) {
			while (results.next()) {
				others.add(getOther(results.getString(1), cf1.order, cf1.name));
			}
		}

		DBManager.selectFileClonePath1.setString(1, cf1.path);
		try (ResultSet results = DBManager.selectFileClonePath1.executeQuery()) {
			while (results.next()) {
				others.add(getOther(results.getString(1), cf1.order, cf1.name));
			}
		}

		return others;
	}

	private List<CodeFragment> searchCodeFragmentCloneInRepresentativeMethod(CodeFragment cf1) throws SQLException {
		DBManager.selectMethodClonePath2.setString(1, cf1.path);
		DBManager.selectMethodClonePath2.setInt(2, cf1.order);
		List<CodeFragment> others = new ArrayList<>();
		try (ResultSet results = DBManager.selectMethodClonePath2.executeQuery()) {
			while (results.next()) {
				others.add(getOther(results.getString(1), results.getInt(2), results.getString(3)));
			}
		}

		DBManager.selectMethodClonePath1.setString(1, cf1.path);
		DBManager.selectMethodClonePath1.setInt(2, cf1.order);
		try (ResultSet results = DBManager.selectMethodClonePath1.executeQuery()) {
			while (results.next()) {
				others.add(getOther(results.getString(1), results.getInt(2), results.getString(3)));
			}
		}

		return others;
	}

	private CodeFragment getOther(String path, int order, String name) throws SQLException {
		List<Hash> normalizedSentences = new ArrayList<>();
		List<Hash> originalSentences = new ArrayList<>();
		List<List<Integer>> lineNumberPerSentenceList = new ArrayList<>();
		normalizedSentences.add(null);
		originalSentences.add(null);
		lineNumberPerSentenceList.add(null);

		DBManager.selectDeletedSentences.setString(1, path);
		DBManager.selectDeletedSentences.setInt(2, order);
		try (ResultSet results2 = DBManager.selectDeletedSentences.executeQuery()) {
			while (results2.next()) {
				List<Integer> lineNumberPerSentence = new ArrayList<>();
				originalSentences.add(new Hash(results2.getBytes(7)));
				normalizedSentences.add(new Hash(results2.getBytes(8)));
				lineNumberPerSentence.add(results2.getInt(5));
				lineNumberPerSentence.add(results2.getInt(6));
				lineNumberPerSentenceList.add(lineNumberPerSentence);
			}
		}

		CodeFragment codeFragment = new CodeFragment(path, name, order, 0, 0,
				normalizedSentences, originalSentences, lineNumberPerSentenceList
		);

		return codeFragment;
	}

	private void insertCodeFragmentCloneInRepresentative(
			CodeFragment cf1, List<Integer> cloneIndexes1, List<Integer> gapIndexes1,
			CodeFragment cf2, List<Integer> cloneIndexes2, List<Integer> gapIndexes2,
			int cloneSetId) {

		CodeFragmentClonePair cfClonePair = new CodeFragmentClonePair(
				cf1, cf2, null, cloneIndexes1, cloneIndexes2, gapIndexes1, gapIndexes2);

		try {
			outputCodeFragmentClonePair(cfClonePair, cloneSetId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

