package decrescendo.lexer.method;

import decrescendo.granularity.File;
import decrescendo.granularity.Method;

import java.io.IOException;
import java.util.HashSet;

public interface MethodLexer {
	HashSet<Method> getMethodSet(HashSet<File> fileSet) throws IOException;

	HashSet<Method> getMethodObjects(String path, String source, int representative);
}
