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
	private static int count;

	public CodeFragmentCloneDetector() {
		count = 0;
	}

	public void execute(HashSet<T> set) throws SQLException {
		System.out.println("Start to detect CodeFragment Code Clone");

		List<T> list = addSeparatedSentenceInfo(set);

		List<CodeFragmentClonePair<T>> cfClonePairList;
        cfClonePairList = synchronizedList(new ArrayList<CodeFragmentClonePair<T>>());

        ExecutorService service = Executors.newCachedThreadPool();
		// ExecutorService service = Executors.newSingleThreadExecutor();

		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = i + 1; j < list.size(); j++) {
				Future<List<CodeFragmentClonePair<T>>> future = service.submit(new SmithWaterman<>(list.get(i), list.get(j)));
				try {
                    List<CodeFragmentClonePair<T>> tmpCfCloneList = future.get();
                    tmpCfCloneList.forEach(cfClonePairList::add);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
			}
		}
		service.shutdown();
		while (!service.isTerminated()) {
			try {
				service.awaitTermination(100L, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				System.out.println("Interrupted...");
			}
		}

		List<List<CodeFragmentClonePair<T>>> cfCloneSets = getCodeFragmentCloneSets(cfClonePairList);
		outputCodeFragmentCloneResult(cfCloneSets);

		System.out.println("detected clone : " + count);
		System.out.println("Finish to detect CodeFragment Code Clone");
	}

	static <T extends Granularity> List<T> addSeparatedSentenceInfo(HashSet<T> set) {
		return set.stream()
				.parallel()
				.map(SentenceLexer::separateSentences)
				.collect(Collectors.toList());
	}

	private List<List<CodeFragmentClonePair<T>>> getCodeFragmentCloneSets(
			List<CodeFragmentClonePair<T>> cfClonePairList) {
		return cfClonePairList.stream()
				.parallel()
				.collect(Collectors.groupingBy(CodeFragmentClonePair::getCommonHash, Collectors.toList()))
				.values()
				.stream()
				.parallel()
				.collect(Collectors.toList());
	}

	private void outputCodeFragmentCloneResult(List<List<CodeFragmentClonePair<T>>> cfCloneSets) throws SQLException {
		for (int i = 0; i < cfCloneSets.size(); i++) {
			List<CodeFragmentClonePair<T>> cfClonePairList = cfCloneSets.get(i);

			for (CodeFragmentClonePair<T> clonePair : cfClonePairList) {
				outputCodeFragmentClonePair(clonePair, i);

				if (Config.file || Config.method)
					searchCodeFragmentCloneInRepresentativeFileAndMethod(clonePair, i);
			}
		}
		DBManager.cfcStatement.executeBatch();
	}

	private static <T extends Granularity> void outputCodeFragmentClonePair(CodeFragmentClonePair<T> clonePair, int i) throws SQLException {
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

		DataAccessObject.insertCodeFragmentCloneInfo(cf1, cloneRange1, cf2, cloneRange2, type, count, i);

		if (count % 1000 == 0)
			DBManager.cfcStatement.executeBatch();
		count++;
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

	private static int getCloneType(List<byte[]> originalSentences1, List<byte[]> originalSentences2,
			List<Integer> cloneIndexes1, List<Integer> cloneIndexes2) {
		for (int i = 0; i < cloneIndexes1.size(); i++) {
			if (! java.util.Arrays.equals(originalSentences2.get(cloneIndexes2.get(i)),
                    originalSentences1.get(cloneIndexes1.get(i)))) return 2;
		}
		return 1;
	}

	private void searchCodeFragmentCloneInRepresentativeFileAndMethod(CodeFragmentClonePair<T> cfClonePair, int i)
			throws SQLException {
		T cf1 = cfClonePair.getClone1();
		List<Integer> cloneIndexes1 = cfClonePair.getCloneIndexes1();
		List<Integer> gapIndexes1 = cfClonePair.getGapIndexes1();

		T cf2 = cfClonePair.getClone2();
		List<Integer> cloneIndexes2 = cfClonePair.getCloneIndexes2();
		List<Integer> gapIndexes2 = cfClonePair.getGapIndexes2();

		if (cf1.isRepresentative() == 1)
			searchCodeFragmentCloneInRepresentativeFile(cf1, cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, i);

		if (cf1.isRepresentative() == 2 || cf1.isRepresentative() == 3)
			searchCodeFragmentCloneInRepresentativeMethod(cf1, cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, i);

		if (cf2.isRepresentative() == 1)
			searchCodeFragmentCloneInRepresentativeFile(cf2, cloneIndexes2, gapIndexes2, cf1, cloneIndexes1, gapIndexes1, i);

		if (cf2.isRepresentative() == 2 || cf2.isRepresentative() == 3)
			searchCodeFragmentCloneInRepresentativeMethod(cf2, cloneIndexes2, gapIndexes2, cf1, cloneIndexes1, gapIndexes1, i);
	}

	private void searchCodeFragmentCloneInRepresentativeFile(T cf1, List<Integer> cloneIndexes1, List<Integer> gapIndexes1,
			T cf2, List<Integer> cloneIndexes2, List<Integer> gapIndexes2, int i) throws SQLException {
		DBManager.searchfc1Statement.setString(1, cf1.getPath());
		try (ResultSet results = DBManager.searchfc1Statement.executeQuery()) {
			while (results.next()) {
				addCodeFragmentCloneInRepresentative(results.getString(1), cf1.getNum(),
                        cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, i);
			}
		}

		DBManager.searchfc2Statement.setString(1, cf1.getPath());
		try (ResultSet results = DBManager.searchfc2Statement.executeQuery()) {
			while (results.next()) {
				addCodeFragmentCloneInRepresentative(results.getString(1), cf1.getNum(),
                        cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, i);
			}
		}
	}

	private void searchCodeFragmentCloneInRepresentativeMethod(T cf1, List<Integer> cloneIndexes1, List<Integer> gapIndexes1,
			T cf2, List<Integer> cloneIndexes2, List<Integer> gapIndexes2, int i) throws SQLException {
		DBManager.searchmc1Statement.setString(1, cf1.getPath());
		DBManager.searchmc1Statement.setInt(2, cf1.getNum());
		try (ResultSet results = DBManager.searchmc1Statement.executeQuery()) {
			while (results.next()) {
				addCodeFragmentCloneInRepresentative(results.getString(1), results.getInt(2),
                        cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, i);
			}
		}

		DBManager.searchmc2Statement.setString(1, cf1.getPath());
		DBManager.searchmc2Statement.setInt(2, cf1.getNum());
		try (ResultSet results = DBManager.searchmc2Statement.executeQuery()) {
			while (results.next()) {
				addCodeFragmentCloneInRepresentative(results.getString(1), results.getInt(2),
                        cloneIndexes1, gapIndexes1, cf2, cloneIndexes2, gapIndexes2, i);
			}
		}
	}

	private void addCodeFragmentCloneInRepresentative(String path, int num,
                                                      List<Integer> cloneIndexes1, List<Integer> gapIndexes1,
                                                      T method2, List<Integer> cloneIndexes2, List<Integer> gapIndexes2, int i) throws SQLException {
		String path2 = "";
		String name2 = "";
		int num2 = -1;
		List<byte[]> normalizedSentences = new ArrayList<>();
		List<byte[]> originalSentences = new ArrayList<>();
		List<List<Integer>> lineNumberPerSentenceList = new ArrayList<>();
		normalizedSentences.add(null);
		originalSentences.add(null);
		lineNumberPerSentenceList.add(null);

		DBManager.searchdsStatement.setString(1, path);
		DBManager.searchdsStatement.setInt(2, num);
		try (ResultSet results2 = DBManager.searchdsStatement.executeQuery()) {
			while (results2.next()) {
				List<Integer> lineNumberPerSentence = new ArrayList<>();
				path2 = results2.getString(1);
				name2 = results2.getString(2);
				num2 = results2.getInt(3);
				originalSentences.add(results2.getBytes(7));
				normalizedSentences.add(results2.getBytes(8));
				lineNumberPerSentence.add(results2.getInt(5));
				lineNumberPerSentence.add(results2.getInt(6));
				lineNumberPerSentenceList.add(lineNumberPerSentence);
			}
		}

		Method method;
		File file;
		if (Config.method) {
			method = new Method(path2, name2, null, null, null, null, null, 0, 0, num2, 0);
			method.setNormalizedSentences(normalizedSentences);
			method.setOriginalSentences(originalSentences);
			method.setLineNumberPerSentence(lineNumberPerSentenceList);
			CodeFragmentClonePair<Method> cfClonePair = new CodeFragmentClonePair<>(method, (Method) method2,
					null, cloneIndexes1, cloneIndexes2, gapIndexes1, gapIndexes2);
			outputCodeFragmentClonePair(cfClonePair, i);
		} else {
			file = new File(path2, "", null, null, null, null, null, 0, 0, 0);
			file.setNormalizedSentences(normalizedSentences);
			file.setOriginalSentences(originalSentences);
			file.setLineNumberPerSentence(lineNumberPerSentenceList);
			CodeFragmentClonePair<File> cfClonePair = new CodeFragmentClonePair<>(file, (File) method2, null,
					cloneIndexes1, cloneIndexes2, gapIndexes1, gapIndexes2);
			outputCodeFragmentClonePair(cfClonePair, i);
		}
	}

}
