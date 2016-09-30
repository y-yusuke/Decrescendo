package decrescendo.main;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
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
		try {
			long start = System.currentTimeMillis();
			Config.setConfig();
			DBManager.dbSetup();

			PrintStream ps = new PrintStream(new FileOutputStream(new java.io.File(Config.logPath)));
			System.setOut(ps);
			System.setErr(ps);

			HashSet<File> files = new FileCloneDetector().execute(Config.targetPath);
			System.out.println(getMemoryInfo());

			HashSet<Method> methods = new HashSet<>();
			if (Config.method || Config.codeFragment) {
				methods = new MethodCloneDetector().execute(files);
				files.clear();
				System.out.println(getMemoryInfo());
			}

			if (Config.codeFragment) {
				new CodeFragmentCloneDetector<Method>().execute(methods);
				System.out.println(getMemoryInfo());
			}

			DBManager.closeDB();
			long stop = System.currentTimeMillis();
			double time = (double) (stop - start) / 1000D;
			System.out.println((new StringBuilder("Execution Time :")).append(time).append(" s").toString());
		} catch (Exception e) {
			e.printStackTrace();
			DBManager.closeDB();
		}
	}

	private static String getMemoryInfo() {
		DecimalFormat f1 = new DecimalFormat("#,###GB");
		DecimalFormat f2 = new DecimalFormat("##.#");
		long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
		long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
		long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
		long used = total - free;
		double ratio = (used * 100 / (double) total);
		return "Java Memory Info :\nSum =" + f1.format(total) + "\n"
				+ "Usage=" + f1.format(used) + " (" + f2.format(ratio) + "%)\n"
				+ "Max Usage=" + f1.format(max) + "\n";
	}
}
