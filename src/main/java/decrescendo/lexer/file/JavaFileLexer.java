package decrescendo.lexer.file;

import decrescendo.config.Config;
import decrescendo.granularity.File;
import decrescendo.hash.Hash;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.*;

public class JavaFileLexer implements FileLexer {
	private final static Logger log = LoggerFactory.getLogger(JavaFileLexer.class);

	public JavaFileLexer() {
	}

	@Override
	public HashSet<File> getFileSet(String path) throws Exception {
		Path target = Paths.get(path);

		return Files.walk(target)
				.parallel()
				.filter(e -> e.toFile().isFile())
				.filter(e -> e.getFileName().toString().endsWith(".java"))
				.map(this::getFileObject)
				.filter(e -> e != null)
				.collect(Collectors.toCollection(HashSet::new));
	}

	@Override
	public File getFileObject(Path path) {
		try {
			String code = getJavaFileCode(path);
			if (code == null) {
				log.error("Cannot read this file: {}", path);
				return null;
			}


			Scanner scanner = new Scanner();
			scanner.setSource(getTrimmedCode(code).toCharArray());
			scanner.recordLineSeparator = true;
			scanner.sourceLevel = ClassFileConstants.JDK1_8;

			StringBuilder originalSb = new StringBuilder();
			StringBuilder normalizedSb = new StringBuilder();

			int tokenSize = 0;

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
				return new File(path.toString(), Hash.createHash(normalizedSb.toString()), Hash.createHash(originalSb.toString()), code);
			} else {
				return null;
			}

		} catch (InvalidInputException e) {
			log.error("Cannot parse this file: {}", path, e);
			return null;
		}
	}

	private static String getJavaFileCode(Path path) {
		Charset[] charsets = new Charset[]{
				StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1,
				StandardCharsets.US_ASCII, StandardCharsets.UTF_16,
				StandardCharsets.UTF_16BE, StandardCharsets.UTF_16LE,
		};

		for (final Charset c : charsets) {
			try {
				return Files.lines(path, c).collect(Collectors.joining("\n"));
			} catch (final Exception ignored) {
			}
		}

		return null;
	}

	private static String getTrimmedCode(String source) {
		return Arrays.stream(source.split("\n"))
				.filter(e -> !e.startsWith("import") && !e.startsWith("package") && !e.startsWith("#set") && !e.startsWith("/*import"))
				.collect(Collectors.joining("\n"));
	}
}
