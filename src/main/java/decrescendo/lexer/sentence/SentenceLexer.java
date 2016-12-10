package decrescendo.lexer.sentence;

import decrescendo.granularity.CodeFragment;
import decrescendo.granularity.Method;
import decrescendo.hash.Hash;
import decrescendo.hash.HashCreator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SentenceLexer {

	public static List<CodeFragment> getCodeFragmentList(HashSet<Method> set) {
		return set.stream()
				.parallel()
				.map(SentenceLexer::separateSentences)
				.collect(Collectors.toList());
	}

	public static CodeFragment separateSentences(Method e) {
		List<Hash> normalizedSentences = new ArrayList<>();
		List<Hash> originalSentences = new ArrayList<>();
		List<List<Integer>> lineNumberPerSentence = new ArrayList<>();
		List<Integer> lineNumbers = new ArrayList<>();

		normalizedSentences.add(null);
		originalSentences.add(null);
		lineNumberPerSentence.add(null);

		StringBuilder nTmp = new StringBuilder();
		StringBuilder oTmp = new StringBuilder();

		Iterator<String> normalizedTokens = e.normalizedTokens.iterator();
		Iterator<String> originalTokens = e.originalTokens.iterator();
		Iterator<Integer> lineNumberPerToken = e.lineNumberPerToken.iterator();

		while (normalizedTokens.hasNext()) {
			String nToken = normalizedTokens.next();
			String oToken = originalTokens.next();
			Integer lineNumber = lineNumberPerToken.next();

			switch (nToken) {
				case ";":
				case "{":
				case "}":
					if (!nTmp.toString().equals("")) {
						normalizedSentences.add(new Hash(HashCreator.getHash(nTmp.toString())));
						originalSentences.add(new Hash(HashCreator.getHash(oTmp.toString())));
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

					int count = 0;

					label:
					while (true) {
						String nToken2 = normalizedTokens.next();
						String oToken2 = originalTokens.next();
						Integer lineNumber2 = lineNumberPerToken.next();
						switch (nToken2) {
							case "(":
								nTmp.append(nToken2);
								oTmp.append(oToken2);
								lineNumbers.add(lineNumber2);

								count++;
								break;
							case ")":
								nTmp.append(nToken2);
								oTmp.append(oToken2);
								lineNumbers.add(lineNumber2);

								if (count == 1) {
									normalizedSentences.add(new Hash(HashCreator.getHash(nTmp.toString())));
									originalSentences.add(new Hash(HashCreator.getHash(oTmp.toString())));
									lineNumberPerSentence.add(lineNumbers);

									nTmp = new StringBuilder();
									oTmp = new StringBuilder();
									lineNumbers = new ArrayList<>();

									break label;
								} else {
									count--;
									break;
								}
							default:
								nTmp.append(nToken2);
								oTmp.append(oToken2);
								lineNumbers.add(lineNumber2);
						}
					}
					break;

				case "case":
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

								normalizedSentences.add(new Hash(HashCreator.getHash(nTmp.toString())));
								originalSentences.add(new Hash(HashCreator.getHash(oTmp.toString())));
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

					normalizedSentences.add(new Hash(HashCreator.getHash(nTmp.toString())));
					originalSentences.add(new Hash(HashCreator.getHash(oTmp.toString())));
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

		CodeFragment codeFragment = new CodeFragment(e.path, e.name, e.order, e.startLine, e.endLine, normalizedSentences, originalSentences, lineNumberPerSentence, e.normalizedHash);
		codeFragment.representative = e.representative;

		return codeFragment;
	}
}
