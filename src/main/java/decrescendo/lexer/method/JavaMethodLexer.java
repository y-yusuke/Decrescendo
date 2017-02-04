package decrescendo.lexer.method;

import decrescendo.config.Config;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;
import decrescendo.hash.Hash;
import decrescendo.hash.HashCreator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.*;

public class JavaMethodLexer implements MethodLexer {
	public JavaMethodLexer() {
	}

	@Override
	public HashSet<Method> getMethodSet(HashSet<File> files) throws IOException {
		return files.stream()
				.parallel()
				.map(e -> getMethodObjects(e.path, e.code, e.representative))
				.filter(e -> e != null)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(HashSet::new));
	}

	@Override
	public HashSet<Method> getMethodObjects(String path, String code, int representative) {
		HashSet<Method> methodSet = new HashSet<>();

		final ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(code.toCharArray());

		final Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);

		final CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());

		unit.accept(new ASTVisitor() {
			int methodOrder = 0;

			@Override
			public boolean visit(MethodDeclaration node) {
				if (node.isConstructor()) {
					return super.visit(node);
				}

				int startLine = unit.getLineNumber(node.getName().getStartPosition());
				int endLine = unit.getLineNumber(node.getStartPosition() + node.getLength());
				String methodCode = getJavaMethodCode(code, startLine, endLine);

				try {
					Scanner scanner = new Scanner();
					scanner.setSource(methodCode.toCharArray());
					scanner.recordLineSeparator = true;
					scanner.sourceLevel = ClassFileConstants.JDK1_8;

					StringBuilder originalSb = new StringBuilder();
					StringBuilder normalizedSb = new StringBuilder();
					List<String> normalizedTokens = new ArrayList<>();
					List<String> originalTokens = new ArrayList<>();
					List<Integer> lineNumberPerToken = new ArrayList<>();

					int separateTokenCount = 0;

					label:
					while (true) {
						switch (scanner.getNextToken()) {
							case TokenNameEOF:
								break label;

							case TokenNameNotAToken:
							case TokenNameWHITESPACE:
							case TokenNameCOMMENT_LINE:
							case TokenNameCOMMENT_BLOCK:
							case TokenNameCOMMENT_JAVADOC:
								break;

							case TokenNameabstract:
							case TokenNamefinal:
							case TokenNamepublic:
							case TokenNameprivate:
							case TokenNameprotected:
							case TokenNamestatic:
							case TokenNamenative:
							case TokenNamesynchronized:
							case TokenNametransient:
							case TokenNamevolatile:
								break;

							case TokenNameAT:
								scanner.getNextToken();
								break;

							case TokenNameLBRACE:
							case TokenNameRBRACE:
							case TokenNameSEMICOLON:
								separateTokenCount++;
								if (Config.smithWaterman) {
									normalizedTokens.add(scanner.getCurrentTokenString());
									originalTokens.add(scanner.getCurrentTokenString());
									lineNumberPerToken.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);
								}
								break;

							case TokenNamefor:
							case TokenNameif:
							case TokenNamewhile:
								normalizedSb.append(scanner.getCurrentTokenString());
								originalSb.append(scanner.getCurrentTokenString());
								normalizedTokens.add(scanner.getCurrentTokenString());
								originalTokens.add(scanner.getCurrentTokenString());
								lineNumberPerToken.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);

								int count = 0;

								label2:
								while (true) {
									switch (scanner.getNextToken()) {
										case TokenNameNotAToken:
										case TokenNameWHITESPACE:
										case TokenNameCOMMENT_LINE:
										case TokenNameCOMMENT_BLOCK:
										case TokenNameCOMMENT_JAVADOC:
											break;

										case TokenNameabstract:
										case TokenNamefinal:
										case TokenNamepublic:
										case TokenNameprivate:
										case TokenNameprotected:
										case TokenNamestatic:
										case TokenNamenative:
										case TokenNamesynchronized:
										case TokenNametransient:
										case TokenNamevolatile:
											break;

										case TokenNameLBRACE:
										case TokenNameRBRACE:
										case TokenNameSEMICOLON:
											normalizedTokens.add(scanner.getCurrentTokenString());
											originalTokens.add(scanner.getCurrentTokenString());
											lineNumberPerToken.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);
											break;

										case TokenNameLPAREN:
											normalizedSb.append(scanner.getCurrentTokenString());
											originalSb.append(scanner.getCurrentTokenString());
											normalizedTokens.add(scanner.getCurrentTokenString());
											originalTokens.add(scanner.getCurrentTokenString());
											lineNumberPerToken.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);

											count++;
											break;

										case TokenNameRPAREN:
											normalizedSb.append(scanner.getCurrentTokenString());
											originalSb.append(scanner.getCurrentTokenString());
											normalizedTokens.add(scanner.getCurrentTokenString());
											originalTokens.add(scanner.getCurrentTokenString());
											lineNumberPerToken.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);

											if (count == 1) {
												break label2;
											} else {
												count--;
												break;
											}

										case TokenNameIdentifier:
										case TokenNameIntegerLiteral:
										case TokenNameLongLiteral:
										case TokenNameFloatingPointLiteral:
										case TokenNameDoubleLiteral:
										case TokenNameCharacterLiteral:
										case TokenNameStringLiteral:
											normalizedSb.append("$");
											originalSb.append(scanner.getCurrentTokenString());
											normalizedTokens.add("$");
											originalTokens.add(scanner.getCurrentTokenString());
											lineNumberPerToken.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);
											break;

										default:
											normalizedSb.append(scanner.getCurrentTokenString());
											originalSb.append(scanner.getCurrentTokenString());
											normalizedTokens.add(scanner.getCurrentTokenString());
											originalTokens.add(scanner.getCurrentTokenString());
											lineNumberPerToken.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);
									}
								}
								break;

							case TokenNameIdentifier:
							case TokenNameIntegerLiteral:
							case TokenNameLongLiteral:
							case TokenNameFloatingPointLiteral:
							case TokenNameDoubleLiteral:
							case TokenNameCharacterLiteral:
							case TokenNameStringLiteral:
								normalizedSb.append("$");
								originalSb.append(scanner.getCurrentTokenString());
								normalizedTokens.add("$");
								originalTokens.add(scanner.getCurrentTokenString());
								lineNumberPerToken.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);
								break;

							default:
								normalizedSb.append(scanner.getCurrentTokenString());
								originalSb.append(scanner.getCurrentTokenString());
								normalizedTokens.add(scanner.getCurrentTokenString());
								originalTokens.add(scanner.getCurrentTokenString());
								lineNumberPerToken.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);
						}
					}

					if (normalizedTokens.size() - separateTokenCount >= Config.mMinTokens) {
						String name = node.getName().toString();
						Hash normalizedHash = new Hash(HashCreator.getHash(normalizedSb.toString()));
						Hash originalHash = new Hash(HashCreator.getHash(originalSb.toString()));
						Method method = new Method(path, name, methodOrder, startLine, endLine, normalizedHash, originalHash, normalizedTokens, originalTokens, lineNumberPerToken);

						method.representative = representative;
						methodSet.add(method);
					}

					methodOrder++;
					return super.visit(node);

				} catch (InvalidInputException e) {
					System.err.println("Cannot parse this method: " + path + "\t" + node.getName().toString());
					e.printStackTrace();
					System.err.println();
					return false;
				}
			}
		});

		if (methodSet.size() != 0) {
			return methodSet;
		} else {
			return null;
		}
	}

	private String getJavaMethodCode(String code, int startLine, int endLine) {
		StringBuilder methodCode = new StringBuilder();
		String[] strs = code.split("\n");

		for (int i = startLine; i < endLine; i++) {
			methodCode.append(strs[i - 1]);
			methodCode.append("\n");
		}

		int lastIndexBrace = strs[endLine - 1].lastIndexOf("}");
		int lastIndexSemicolon = strs[endLine - 1].lastIndexOf(";");

		if (lastIndexBrace > lastIndexSemicolon) {
			methodCode.append(strs[endLine - 1].substring(0, lastIndexBrace + 1));
		} else {
			methodCode.append(strs[endLine - 1].substring(0, lastIndexSemicolon + 1));
		}

		return methodCode.toString();
	}
}