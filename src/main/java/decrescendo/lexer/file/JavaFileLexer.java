package decrescendo.lexer.file;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

import decrescendo.config.Config;
import decrescendo.granularity.File;
import decrescendo.hash.HashCreator;

public class JavaFileLexer implements FileLexer {

	public JavaFileLexer() {
	}

	@Override
	public HashSet<File> getFileSet(String path) throws Exception {
		Path target = Paths.get(path);
		return Files.walk(target)
				.parallel()
				.filter(e -> e.toFile()
						.isFile())
				.filter(e -> e.getFileName().toString()
						.endsWith(".java")).map(this::getFileInfo)
				.filter(e -> e != null)
				.collect(Collectors.toCollection(HashSet::new));
	}

	@Override
	public File getFileInfo(Path path) {
		try {
			String source = getJavaFileSourceCode(path);
			Scanner scanner = new Scanner();
			if (source == null) throw new AssertionError();
			scanner.setSource(source.toCharArray());
			scanner.recordLineSeparator = true;
			scanner.sourceLevel = ClassFileConstants.JDK1_8;

			StringBuilder originalSb = new StringBuilder();
			StringBuilder normalizedSb = new StringBuilder();
			
			int endLine;
			int tokenSize = 0;

			label:
			while (true) {
				switch (scanner.getNextToken()) {
					case TokenNameEOF:
						endLine = scanner.getLineNumber(scanner.getCurrentTokenStartPosition());
						break label;

					case TokenNameNotAToken:
					case TokenNameWHITESPACE:
					case TokenNameCOMMENT_LINE:
					case TokenNameCOMMENT_BLOCK:
					case TokenNameCOMMENT_JAVADOC:
						break;

					case TokenNameimport:
					case TokenNamepackage:
						label2:
						while (true) {
							switch (scanner.getNextToken()) {
								case TokenNameSEMICOLON:
									break label2;
							}
						}
						break;

					case TokenNameLBRACE:
					case TokenNameRBRACE:
					case TokenNameSEMICOLON:
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
						tokenSize++;
						break;

					default:
						normalizedSb.append(scanner.getCurrentTokenString());
						tokenSize++;
				}
			}

			if (tokenSize >= Config.fMinTokens) {
				File file = new File();
				file.setPath(path.toString());
				file.setSource(source);
				file.setNormalizedHash(HashCreator.convertString(HashCreator.getHash(normalizedSb.toString())));
				file.setOriginalHash(HashCreator.convertString(HashCreator.getHash(originalSb.toString())));
				file.setStartLine(1);
				file.setEndLine(endLine);
				file.setRepresentative(0);
				return file;
			} else
				return null;
		} catch (InvalidInputException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String getJavaFileSourceCode(Path path) {
		try {
			return Files.lines(path).collect(Collectors.joining("\n"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
