package decrescendo.lexer.file;

import decrescendo.granularity.File;

import java.nio.file.Path;
import java.util.HashSet;

public interface FileLexer {
	HashSet<File> getFileSet(String path) throws Exception;

	File getFileObject(Path path);
}
