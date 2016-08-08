package decrescendo.main;

import java.util.HashSet;

import decrescendo.clonedetector.CodeFragmentCloneDetector;
import decrescendo.clonedetector.FileCloneDetector;
import decrescendo.clonedetector.MethodCloneDetector;
import decrescendo.config.Config;
import decrescendo.db.DBManager;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;

public class Main {

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		Config.setConfig();
		DBManager.dbSetup();

		HashSet<File> files = new FileCloneDetector().execute(Config.targetDirectory);

		HashSet<Method> methods = null;
		if (Config.method)
			methods = new MethodCloneDetector().execute(files);

		if (Config.method && Config.codefragment)
			new CodeFragmentCloneDetector<Method>().execute(methods);
		else if (!Config.method && Config.codefragment)
			new CodeFragmentCloneDetector<File>().execute(files);

		DBManager.closeDB();
		long stop = System.currentTimeMillis();
		double time = (double) (stop - start) / 1000D;
		System.out.println((new StringBuilder("Execution Time :")).append(time).append(" s").toString());
	}
}
