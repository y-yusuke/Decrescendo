package decrescendo.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
	public static String targetPath;
	public static String outputPath;
	public static String logPath;
	public static String language;
	public static boolean file;
	public static boolean method;
	public static boolean codeFragment;
	public static int fMinTokens;
	public static int mMinTokens;
	public static int cfMinTokens;
	public static double gapRate;
	public static boolean suffix;
	public static boolean smithWaterman;

	public static void setConfig() throws IOException {
		Properties prop = new Properties();
		try (FileInputStream fr = new FileInputStream("./decrescendo.properties")) {
			prop.load(fr);
		}
		targetPath = prop.getProperty("targetPath");
		outputPath = prop.getProperty("outputPath");
		logPath = prop.getProperty("logPath");
		language = prop.getProperty("language");
		file = Boolean.valueOf(prop.getProperty("file"));
		method = Boolean.valueOf(prop.getProperty("method"));
		codeFragment = Boolean.valueOf(prop.getProperty("codeFragment"));
		fMinTokens = Integer.parseInt(prop.getProperty("fileMinTokens"));
		mMinTokens = Integer.parseInt(prop.getProperty("methodMinTokens"));
		cfMinTokens = Integer.parseInt(prop.getProperty("codeFragmentMinTokens"));
		gapRate = Double.parseDouble(prop.getProperty("gapRate"));
		suffix = Boolean.valueOf(prop.getProperty("suffix"));
		smithWaterman = Boolean.valueOf(prop.getProperty("smithWaterman"));
	}
}
