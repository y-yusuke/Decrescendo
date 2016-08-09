package decrescendo.lexer.method;

import java.io.IOException;
import java.util.HashSet;

import decrescendo.granularity.File;
import decrescendo.granularity.Method;

public interface MethodLexer {
	HashSet<Method> getMethodSet(HashSet<File> fileSet) throws IOException;

	HashSet<Method> getMethodInfo(String path, String source, int representative);
}
