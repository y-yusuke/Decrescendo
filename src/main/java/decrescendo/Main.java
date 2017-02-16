package decrescendo;

import decrescendo.clonedetector.CodeFragmentCloneDetectorST;
import decrescendo.clonedetector.CodeFragmentCloneDetectorSW;
import decrescendo.clonedetector.FileCloneDetector;
import decrescendo.clonedetector.MethodCloneDetector;
import decrescendo.config.Config;
import decrescendo.db.DBManager;
import decrescendo.granularity.File;
import decrescendo.granularity.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.HashSet;

public class Main {
	private final static Logger log = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) throws Exception {
		try {
			long start = System.currentTimeMillis();
			Config.setConfig();
			DBManager.dbSetup();

			HashSet<File> files = new FileCloneDetector().execute(Config.targetPath);
			log.info(getMemoryInfo());

			HashSet<Method> methods = new HashSet<>();
			if (Config.method || Config.codeFragment) {
				methods = new MethodCloneDetector().execute(files);
				files.clear();
				log.info(getMemoryInfo());
			}

			if (Config.codeFragment) {
				if (Config.smithWaterman) {
					new CodeFragmentCloneDetectorSW().execute(methods);
				} else if (Config.suffix) {
					new CodeFragmentCloneDetectorST().execute(methods);
				}
				log.info(getMemoryInfo());
			}

			DBManager.closeDB();
			long stop = System.currentTimeMillis();
			double time = (double) (stop - start) / 1000D;
			log.info("Execution Time :{} s", time);
		} catch (Exception e) {
			DBManager.closeDB();
			e.printStackTrace();
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
		return "Java Memory Info : Sum =" + f1.format(total) + " "
				+ "Usage=" + f1.format(used) + " (" + f2.format(ratio) + "%) "
				+ "Max Usage=" + f1.format(max);
	}
}
