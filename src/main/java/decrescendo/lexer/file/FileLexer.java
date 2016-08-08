package decrescendo.lexer.file;

import java.nio.file.Path;
import java.util.HashSet;

import decrescendo.granularity.File;

public interface FileLexer {
	HashSet<File> getFileSet(String path) throws Exception;

	File getFileInfo(Path path);
}
