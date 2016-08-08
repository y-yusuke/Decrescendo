package decrescendo.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {
	public static String targetDirectory;
	public static String language;
	public static boolean file;
	public static boolean method;
	public static boolean codefragment;
	public static int fMinTokens;
	public static int mMinTokens;
	public static int cfMinTokens;
	public static double gapRate;

	public static void setConfig() throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		try (FileInputStream fr = new FileInputStream("./decrescendo.properties")) {
			prop.load(fr);
		}
		targetDirectory = prop.getProperty("targetDirectory");
		if (targetDirectory == null)
			throw new RuntimeException();
		language = prop.getProperty("language");
		file = Boolean.valueOf(prop.getProperty("file"));
		method = Boolean.valueOf(prop.getProperty("method"));
		codefragment = Boolean.valueOf(prop.getProperty("codefragment"));
		fMinTokens = Integer.parseInt(prop.getProperty("fileMinTokens"));
		mMinTokens = Integer.parseInt(prop.getProperty("methodMinTokens"));
		cfMinTokens = Integer.parseInt(prop.getProperty("codefragmentMinTokens"));
		gapRate = Double.parseDouble(prop.getProperty("gapRate"));
	}
}
