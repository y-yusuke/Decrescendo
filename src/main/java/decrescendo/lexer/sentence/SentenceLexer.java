package decrescendo.lexer.sentence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import decrescendo.granularity.Granularity;
import decrescendo.hash.HashCreator;

public class SentenceLexer {

	public static <T extends Granularity> List<T> addSeparatedSentenceInfo(HashSet<T> set) {
		return set.stream()
				.parallel()
				.map(SentenceLexer::separateSentences)
				.collect(Collectors.toList());
	}

	public static <T extends Granularity> T separateSentences(T e) {
		List<byte[]> normalizedSentences = new ArrayList<>();
		List<byte[]> originalSentences = new ArrayList<>();
		List<List<Integer>> lineNumberPerSentence = new ArrayList<>();
		List<Integer> lineNumbers = new ArrayList<>();

		normalizedSentences.add(null);
		originalSentences.add(null);
		lineNumberPerSentence.add(null);

		StringBuilder nTmp = new StringBuilder();
		StringBuilder oTmp = new StringBuilder();

		Iterator<String> normalizedTokens = e.getNormalizedTokens().iterator();
		Iterator<String> originalTokens = e.getOriginalTokens().iterator();
		Iterator<Integer> lineNumberPerToken = e.getLineNumberPerToken().iterator();

		while (normalizedTokens.hasNext()) {
			String nToken = normalizedTokens.next();
			String oToken = originalTokens.next();
			Integer lineNumber = lineNumberPerToken.next();

			switch (nToken) {
				case ";":
				case "{":
				case "}":
					if (!nTmp.toString().equals("")) {
						normalizedSentences.add(HashCreator.getHash(nTmp.toString()));
						originalSentences.add(HashCreator.getHash(oTmp.toString()));
						lineNumberPerSentence.add(lineNumbers);
					}
					nTmp = new StringBuilder();
					oTmp = new StringBuilder();
					lineNumbers = new ArrayList<>();
					break;

				case "if":
				case "for":
				case "while":
					nTmp.append(nToken);
					oTmp.append(oToken);
					lineNumbers.add(lineNumber);

					label:
					while (true) {
						String nToken2 = normalizedTokens.next();
						String oToken2 = originalTokens.next();
						Integer lineNumber2 = lineNumberPerToken.next();
						switch (nToken2) {
							case ")":
								nTmp.append(nToken2);
								oTmp.append(oToken2);
								lineNumbers.add(lineNumber2);

								normalizedSentences.add(HashCreator.getHash(nTmp.toString()));
								originalSentences.add(HashCreator.getHash(oTmp.toString()));
								lineNumberPerSentence.add(lineNumbers);

								nTmp = new StringBuilder();
								oTmp = new StringBuilder();
								lineNumbers = new ArrayList<>();

								break label;
							default:
								nTmp.append(nToken2);
								oTmp.append(oToken2);
								lineNumbers.add(lineNumber2);
						}
					}
					break;

				case "case":
				case "default":
					nTmp.append(nToken);
					oTmp.append(oToken);
					lineNumbers.add(lineNumber);

					label_case:
					while (true) {
						String nToken2 = normalizedTokens.next();
						String oToken2 = originalTokens.next();
						Integer lineNumber2 = lineNumberPerToken.next();
						switch (nToken2) {
							case ":":
								nTmp.append(nToken2);
								oTmp.append(oToken2);
								lineNumbers.add(lineNumber2);

								normalizedSentences.add(HashCreator.getHash(nTmp.toString()));
								originalSentences.add(HashCreator.getHash(oTmp.toString()));
								lineNumberPerSentence.add(lineNumbers);

								nTmp = new StringBuilder();
								oTmp = new StringBuilder();
								lineNumbers = new ArrayList<>();

								break label_case;
							default:
								nTmp.append(nToken2);
								oTmp.append(oToken2);
								lineNumbers.add(lineNumber2);
						}
					}
					break;

				case "do":
				case "else":
					nTmp.append(nToken);
					oTmp.append(oToken);
					lineNumbers.add(lineNumber);

					normalizedSentences.add(HashCreator.getHash(nTmp.toString()));
					originalSentences.add(HashCreator.getHash(oTmp.toString()));
					lineNumberPerSentence.add(lineNumbers);

					nTmp = new StringBuilder();
					oTmp = new StringBuilder();
					lineNumbers = new ArrayList<>();

					break;

				default:
					nTmp.append(nToken);
					oTmp.append(oToken);
					lineNumbers.add(lineNumber);
			}

		}

		e.setNormalizedSentences(normalizedSentences);
		e.setOriginalSentences(originalSentences);
		e.setLineNumberPerSentence(lineNumberPerSentence);
		return e;
	}
}
