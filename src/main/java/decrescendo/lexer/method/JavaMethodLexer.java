package decrescendo.lexer.method;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

import decrescendo.config.Config;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;
import decrescendo.hash.HashCreator;

public class JavaMethodLexer implements MethodLexer {
	public JavaMethodLexer() {
	}

	@Override
	public HashSet<Method> getMethodSet(HashSet<File> files) throws IOException {
		return files.stream()
				.parallel()
				.map(e -> getMethodInfo(e.getPath(), e.getSource(), e.isRepresentative()))
				.filter(e -> e != null)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(HashSet::new));
	}

	@Override
	public HashSet<Method> getMethodInfo(String path, String source, int representative) {
		HashSet<Method> methodSet = new HashSet<>();
		final ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray());
		final CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());

		unit.accept(new ASTVisitor() {
			int methodOrder = 0;

			@Override
			public boolean visit(MethodDeclaration node) {
				if (node.isConstructor())
					return super.visit(node);

				int startLine = unit.getLineNumber(node.getName().getStartPosition());
				int endLine = unit.getLineNumber(node.getStartPosition() + node.getLength());
				String methodSource = getJavaMethodSourceCode(source, startLine, endLine);

				try {
					Scanner scanner = new Scanner();
					scanner.setSource(methodSource.toCharArray());
					scanner.recordLineSeparator = true;
					scanner.sourceLevel = ClassFileConstants.JDK1_8;

					StringBuilder originalSb = new StringBuilder();
					StringBuilder normalizedSb = new StringBuilder();
					List<String> normalizedTokens = new ArrayList<>();
					List<String> originalTokens = new ArrayList<>();
					List<Integer> lineNumberPerToken = new ArrayList<>();

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

							case TokenNameLBRACE:
							case TokenNameRBRACE:
							case TokenNameSEMICOLON:
								normalizedTokens.add(scanner.getCurrentTokenString());
								originalTokens.add(scanner.getCurrentTokenString());
								lineNumberPerToken
										.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);
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
								lineNumberPerToken
										.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);
								break;

							default:
								normalizedSb.append(scanner.getCurrentTokenString());
								originalSb.append(scanner.getCurrentTokenString());
								normalizedTokens.add(scanner.getCurrentTokenString());
								originalTokens.add(scanner.getCurrentTokenString());
								lineNumberPerToken
										.add(startLine + scanner.getLineNumber(scanner.getCurrentTokenStartPosition()) - 1);
						}
					}

					if (normalizedTokens.size() >= Config.mMinTokens) {
						Method method = new Method();
						method.setPath(path);
						method.setName(node.getName().toString());
						method.setNormalizedHash(HashCreator.convertString(HashCreator.getHash(normalizedSb.toString())));
						method.setOriginalHash(HashCreator.convertString(HashCreator.getHash(originalSb.toString())));
						method.setNormalizedTokens(normalizedTokens);
						method.setOriginalTokens(originalTokens);
						method.setLineNumberPerToken(lineNumberPerToken);
						method.setStartLine(startLine);
						method.setEndLine(endLine);
						method.setOrder(methodOrder);
						method.setRepresentative(representative);

						methodSet.add(method);
					}
					methodOrder++;
					return super.visit(node);
				} catch (InvalidInputException e) {
					e.printStackTrace();
					return false;
				}
			}
		});
		if (methodSet.size() != 0)
			return methodSet;
		else
			return null;
	}

	private static String getJavaMethodSourceCode(String source, int startLine, int endLine) {
		StringBuilder methodSource = new StringBuilder();
		String[] strs = source.split("\n");
		for (int i = startLine; i <= endLine; i++) {
			methodSource.append(strs[i - 1]);
			methodSource.append("\n");
		}
		return methodSource.toString();
	}
}
