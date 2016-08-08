package decrescendo.lexer.sentence;

import java.util.ArrayList;
import java.util.List;

import decrescendo.granularity.Granularity;
import decrescendo.hash.HashCreator;

public class SentenceLexer {

	public static <T extends Granularity> T separateSentences(T e) {
		List<byte[]> normalizedSentences = new ArrayList<>();
		List<byte[]> originalSentences = new ArrayList<>();
		List<List<Integer>> lineNumberPerSentence = new ArrayList<>();
		List<Integer> lineNumbers = new ArrayList<>();

		normalizedSentences.add(null);
		originalSentences.add(null);
		lineNumberPerSentence.add(null);

		StringBuilder ntmp = new StringBuilder();
		StringBuilder otmp = new StringBuilder();

		for (int i = 0; i < e.getNormalizedTokens().size(); i++) {
			String nstr = e.getNormalizedTokens().get(i);
			String ostr = e.getOriginalTokens().get(i);

			if (nstr.equals(";") || nstr.equals("{") || nstr.equals("}")) {
				if (!ntmp.toString().equals("")) {
					normalizedSentences.add(HashCreator.getHash(ntmp.toString()));
					originalSentences.add(HashCreator.getHash(otmp.toString()));
					lineNumberPerSentence.add(lineNumbers);
					ntmp = new StringBuilder();
					otmp = new StringBuilder();
					lineNumbers = new ArrayList<>();
				}
			} else {
				ntmp.append(nstr);
				otmp.append(ostr);
				lineNumbers.add(e.getLineNumberPerToken().get(i));
			}
		}

		e.setNormalizedSentences(normalizedSentences);
		e.setOriginalSentences(originalSentences);
		e.setLineNumberPerSentence(lineNumberPerSentence);
		return e;
	}
}
