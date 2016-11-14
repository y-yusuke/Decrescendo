package decrescendo.db;

import decrescendo.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
	private static Connection connection;
	private static Statement statement;
	public static PreparedStatement insertDeletedMethodInfo;
	public static PreparedStatement insertDeletedSentenceInfo;
	public static PreparedStatement insertFileCloneInfo;
	public static PreparedStatement insertMethodCloneInfo;
	public static PreparedStatement insertCodeFragmentCloneInfo;
	public static PreparedStatement selectFileClonePath1;
	public static PreparedStatement selectFileClonePath2;
	public static PreparedStatement selectMethodClonePath1;
	public static PreparedStatement selectMethodClonePath2;
	public static PreparedStatement selectDeletedMethods;
	public static PreparedStatement selectDeletedSentences;

	private static Connection tmpconnection;
	private static Statement tmpstatement;

	public static void dbSetup() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + Config.outputPath);
		connection.setAutoCommit(false);
		statement = connection.createStatement();
		initialDB();
		createDB();
		createPrepareStatement();
	}

	private static void initialDB() throws SQLException {
		statement.executeUpdate("DROP TABLE IF EXISTS DELETE_METHODS");
		statement.executeUpdate("DROP TABLE IF EXISTS DELETE_SENTENCES");
		statement.executeUpdate("DROP TABLE IF EXISTS FILE_CLONES");
		statement.executeUpdate("DROP TABLE IF EXISTS METHOD_CLONES");
		statement.executeUpdate("DROP TABLE IF EXISTS CODEFRAGMENT_CLONES");
	}

	private static void createDB() throws SQLException {
		statement.executeUpdate("CREATE TABLE DELETE_METHODS("
				+ "PATH string, "
				+ "METHOD_NAME string, "
				+ "METHOD_NUMBER INTEGER, "
				+ "START_LINE INTEGER, "
				+ "END_LINE INTEGER, "
				+ "ORIGINAL_HASH string, "
				+ "NORMALIZED_HASH string)");
		statement.executeUpdate("CREATE INDEX mPathIndex ON DELETE_METHODS(PATH)");
		statement.executeUpdate("CREATE INDEX mNumIndex ON DELETE_METHODS(METHOD_NUMBER)");

		statement.executeUpdate("CREATE TABLE DELETE_SENTENCES("
				+ "PATH string, "
				+ "METHOD_NAME string, "
				+ "METHOD_NUMBER INTEGER, "
				+ "SENTENCE_NUMBER INTEGER, "
				+ "START_LINE INTEGER, "
				+ "END_LINE INTEGER, "
				+ "ORIGINAL_HASH BLOB, "
				+ "NORMALIZED_HASH BLOB)");
		statement.executeUpdate("CREATE INDEX sPathIndex ON DELETE_SENTENCES(PATH)");
		statement.executeUpdate("CREATE INDEX mNumIndex2 ON DELETE_SENTENCES(METHOD_NUMBER)");

		statement.executeUpdate("CREATE TABLE FILE_CLONES("
				+ "ID intege, "
				+ "PATH1 string, "
				+ "START_LINE1 INTEGER, "
				+ "END_LINE1 INTEGER, "
				+ "PATH2 string, "
				+ "START_LINE2 INTEGER, "
				+ "END_LINE2 INTEGER, "
				+ "CLONESET_ID INTEGER, "
				+ "TYPE INTEGER)");
		statement.executeUpdate("CREATE INDEX fcIndex1 ON FILE_CLONES(PATH1)");
		statement.executeUpdate("CREATE INDEX fcIndex2 ON FILE_CLONES(PATH2)");

		statement.executeUpdate("CREATE TABLE METHOD_CLONES("
				+ "ID INTEGER, "
				+ "PATH1 string, "
				+ "METHOD_NAME1 string, "
				+ "METHOD_NUMBER1 INTEGER, "
				+ "START_LINE1 INTEGER, "
				+ "END_LINE1 INTEGER, "
				+ "PATH2 string, "
				+ "METHOD_NAME2 string, "
				+ "METHOD_NUMBER2 INTEGER, "
				+ "START_LINE2 INTEGER, "
				+ "END_LINE2 INTEGER, "
				+ "CLONESET_ID INTEGER, "
				+ "TYPE INTEGER)");
		statement.executeUpdate("CREATE INDEX mcIndex1 ON METHOD_CLONES(PATH1)");
		statement.executeUpdate("CREATE INDEX mcIndex2 ON METHOD_CLONES(PATH2)");

		statement.executeUpdate("CREATE TABLE CODEFRAGMENT_CLONES("
				+ "ID INTEGER, "
				+ "PATH1 string, "
				+ "METHOD_NAME1 string, "
				+ "METHOD_NUMBER1 INTEGER, "
				+ "START_LINE1 INTEGER, "
				+ "END_LINE1 INTEGER, "
				+ "GAP_LINE1 string, "
				+ "PATH2 string, "
				+ "METHOD_NAME2 string, "
				+ "METHOD_NUMBER2 INTEGER, "
				+ "START_LINE2 INTEGER, "
				+ "END_LINE2 INTEGER, "
				+ "GAP_LINE2 string, "
				+ "CLONESET_ID INTEGER, "
				+ "TYPE INTEGER)");
	}

	private static void createPrepareStatement() throws SQLException {
		insertDeletedMethodInfo = connection.prepareStatement("INSERT INTO DELETE_METHODS VALUES (?, ?, ?, ?, ?, ?, ?)");

		insertDeletedSentenceInfo = connection.prepareStatement("INSERT INTO DELETE_SENTENCES VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

		insertFileCloneInfo = connection.prepareStatement("INSERT INTO FILE_CLONES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

		insertMethodCloneInfo = connection.prepareStatement("INSERT INTO METHOD_CLONES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		insertCodeFragmentCloneInfo = connection.prepareStatement("INSERT INTO CODEFRAGMENT_CLONES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		selectFileClonePath2 = connection.prepareStatement("SELECT PATH2 FROM FILE_CLONES WHERE PATH1 = ?;");

		selectFileClonePath1 = connection.prepareStatement("SELECT PATH1 FROM FILE_CLONES WHERE PATH2 = ?;");

		selectMethodClonePath2 = connection.prepareStatement("SELECT PATH2, METHOD_NUMBER2, METHOD_NAME2 FROM METHOD_CLONES WHERE PATH1 = ?  AND METHOD_NUMBER1 = ?;");

		selectMethodClonePath1 = connection.prepareStatement("SELECT PATH1, METHOD_NUMBER1, METHOD_NAME1 FROM METHOD_CLONES WHERE PATH2 = ?  AND METHOD_NUMBER2 = ?;");

		selectDeletedMethods = connection.prepareStatement("SELECT * FROM DELETE_METHODS WHERE PATH = ? AND METHOD_NUMBER = ?;");

		selectDeletedSentences = connection.prepareStatement("SELECT * FROM DELETE_SENTENCES WHERE PATH = ? AND METHOD_NUMBER = ?;");
	}

	public static void closeDB() throws SQLException {
		connection.commit();
		if (statement != null) statement.close();
		if (insertDeletedMethodInfo != null) insertDeletedMethodInfo.close();
		if (insertDeletedSentenceInfo != null) insertDeletedSentenceInfo.close();
		if (insertFileCloneInfo != null) insertFileCloneInfo.close();
		if (insertMethodCloneInfo != null) insertMethodCloneInfo.close();
		if (insertCodeFragmentCloneInfo != null) insertCodeFragmentCloneInfo.close();
		if (selectFileClonePath2 != null) selectFileClonePath2.close();
		if (selectFileClonePath1 != null) selectFileClonePath1.close();
		if (selectMethodClonePath2 != null) selectMethodClonePath2.close();
		if (selectMethodClonePath1 != null) selectMethodClonePath1.close();
		if (selectDeletedMethods != null) selectDeletedMethods.close();
		if (selectDeletedSentences != null) selectDeletedSentences.close();
		if (connection != null) connection.close();
	}

	public static void createInMemoryDB() throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		tmpconnection = DriverManager.getConnection("jdbc:sqlite:memory");
		tmpconnection.setAutoCommit(false);
		tmpstatement = tmpconnection.createStatement();
		tmpstatement.executeUpdate("CREATE TABLE TMP_CLONES("
				+ "PATH string, "
				+ "METHOD_NAME string, "
				+ "METHOD_NUMBER INTEGER, "
				+ "START_LINE INTEGER, "
				+ "END_LINE INTEGER, "
				+ "ORIGINAL_HASH string, "
				+ "NORMALIZED_HASH string)");
	}
}
