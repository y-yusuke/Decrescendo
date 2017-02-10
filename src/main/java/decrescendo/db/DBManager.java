package decrescendo.db;

import decrescendo.config.Config;

import java.sql.*;

public class DBManager {
	private static Connection connection_memory;
	private static Connection connection_storage;
	private static Statement statement_memory;
	private static Statement statement_storage;

	public static PreparedStatement insertFileCloneInfo_memory;
	public static PreparedStatement insertMethodCloneInfo_memory;

	private static PreparedStatement insertFileCloneInfo_storage;
	private static PreparedStatement insertMethodCloneInfo_storage;
	public static PreparedStatement insertCodeFragmentCloneInfo_storage;

	public static PreparedStatement insertDeletedMethodInfo;
	public static PreparedStatement insertDeletedSentenceInfo;
	public static PreparedStatement insertDeletedTokenInfo;
	public static PreparedStatement selectFileClonePath1;
	public static PreparedStatement selectFileClonePath2;
	public static PreparedStatement selectMethodClonePath1;
	public static PreparedStatement selectMethodClonePath2;
	public static PreparedStatement selectDeletedMethods;
	public static PreparedStatement selectDeletedSentences;
	public static PreparedStatement selectDeletedTokens;

	public static void dbSetup() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		connection_memory = DriverManager.getConnection("jdbc:sqlite::memory:");
		connection_memory.setAutoCommit(false);

		connection_storage = DriverManager.getConnection("jdbc:sqlite:" + Config.outputPath);
		connection_storage.setAutoCommit(false);

		statement_memory = connection_memory.createStatement();
		statement_storage = connection_storage.createStatement();

		initialTable();
		createTable();
		createPrepareStatement();
	}

	private static void initialTable() throws SQLException {
		statement_memory.executeUpdate("DROP TABLE IF EXISTS FILE_CLONES");
		statement_memory.executeUpdate("DROP TABLE IF EXISTS METHOD_CLONES");
		statement_memory.executeUpdate("DROP TABLE IF EXISTS DELETE_METHODS");
		statement_memory.executeUpdate("DROP TABLE IF EXISTS DELETE_SENTENCES");
		statement_memory.executeUpdate("DROP TABLE IF EXISTS DELETE_TOKENS");

		statement_storage.executeUpdate("DROP TABLE IF EXISTS FILE_CLONES");
		statement_storage.executeUpdate("DROP TABLE IF EXISTS METHOD_CLONES");
		statement_storage.executeUpdate("DROP TABLE IF EXISTS CODEFRAGMENT_CLONES");
	}

	private static void createTable() throws SQLException {
		statement_memory.executeUpdate("CREATE TABLE FILE_CLONES("
				+ "ID intege, "
				+ "PATH1 string, "
				+ "PATH2 string, "
				+ "CLONESET_ID INTEGER, "
				+ "TYPE INTEGER)");
		statement_memory.executeUpdate("CREATE INDEX fcIndex1 ON FILE_CLONES(PATH1)");
		statement_memory.executeUpdate("CREATE INDEX fcIndex2 ON FILE_CLONES(PATH2)");

		statement_memory.executeUpdate("CREATE TABLE METHOD_CLONES("
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
		statement_memory.executeUpdate("CREATE INDEX mcPathIndex1 ON METHOD_CLONES(PATH1)");
		statement_memory.executeUpdate("CREATE INDEX mcPathIndex2 ON METHOD_CLONES(PATH2)");

		statement_memory.executeUpdate("CREATE TABLE DELETE_METHODS("
				+ "PATH string, "
				+ "METHOD_NAME string, "
				+ "METHOD_NUMBER INTEGER, "
				+ "START_LINE INTEGER, "
				+ "END_LINE INTEGER, "
				+ "ORIGINAL_HASH string, "
				+ "NORMALIZED_HASH string)");
		statement_memory.executeUpdate("CREATE INDEX mPathIndex ON DELETE_METHODS(PATH)");

		statement_memory.executeUpdate("CREATE TABLE DELETE_SENTENCES("
				+ "PATH string, "
				+ "METHOD_NAME string, "
				+ "METHOD_NUMBER INTEGER, "
				+ "SENTENCE_NUMBER INTEGER, "
				+ "START_LINE INTEGER, "
				+ "END_LINE INTEGER, "
				+ "ORIGINAL_HASH BLOB, "
				+ "NORMALIZED_HASH BLOB)");
		statement_memory.executeUpdate("CREATE INDEX sPathIndex ON DELETE_SENTENCES(PATH)");

		statement_memory.executeUpdate("CREATE TABLE DELETE_TOKENS("
				+ "PATH string, "
				+ "METHOD_NAME string, "
				+ "METHOD_NUMBER INTEGER, "
				+ "START_LINE string, "
				+ "ORIGINAL_TOKEN string, "
				+ "NORMALIZED_TOKEN string)");
		statement_memory.executeUpdate("CREATE INDEX tPathIndex ON DELETE_TOKENS(PATH)");

		statement_storage.executeUpdate("CREATE TABLE FILE_CLONES("
				+ "ID intege, "
				+ "PATH1 string, "
				+ "PATH2 string, "
				+ "CLONESET_ID INTEGER, "
				+ "TYPE INTEGER)");
		statement_storage.executeUpdate("CREATE INDEX fcIndex ON FILE_CLONES(CLONESET_ID)");

		statement_storage.executeUpdate("CREATE TABLE METHOD_CLONES("
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
		statement_storage.executeUpdate("CREATE INDEX mcIndex ON METHOD_CLONES(CLONESET_ID)");

		statement_storage.executeUpdate("CREATE TABLE CODEFRAGMENT_CLONES("
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
		statement_storage.executeUpdate("CREATE INDEX cfcIndex ON CODEFRAGMENT_CLONES(CLONESET_ID)");
	}

	private static void createPrepareStatement() throws SQLException {
		insertDeletedMethodInfo = connection_memory.prepareStatement("INSERT INTO DELETE_METHODS VALUES (?, ?, ?, ?, ?, ?, ?)");
		insertDeletedSentenceInfo = connection_memory.prepareStatement("INSERT INTO DELETE_SENTENCES VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		insertDeletedTokenInfo = connection_memory.prepareStatement("INSERT INTO DELETE_TOKENS VALUES (?, ?, ?, ?, ?, ?)");

		insertFileCloneInfo_memory = connection_memory.prepareStatement("INSERT INTO FILE_CLONES VALUES (?, ?, ?, ?, ?)");
		insertMethodCloneInfo_memory = connection_memory.prepareStatement("INSERT INTO METHOD_CLONES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		selectFileClonePath2 = connection_memory.prepareStatement("SELECT PATH2 FROM FILE_CLONES WHERE PATH1 = ?;");
		selectFileClonePath1 = connection_memory.prepareStatement("SELECT PATH1 FROM FILE_CLONES WHERE PATH2 = ?;");

		selectMethodClonePath2 = connection_memory.prepareStatement("SELECT PATH2, METHOD_NUMBER2, METHOD_NAME2 FROM METHOD_CLONES WHERE PATH1 = ?  AND METHOD_NUMBER1 = ?;");
		selectMethodClonePath1 = connection_memory.prepareStatement("SELECT PATH1, METHOD_NUMBER1, METHOD_NAME1 FROM METHOD_CLONES WHERE PATH2 = ?  AND METHOD_NUMBER2 = ?;");

		selectDeletedMethods = connection_memory.prepareStatement("SELECT * FROM DELETE_METHODS WHERE PATH = ? AND METHOD_NUMBER = ?;");
		selectDeletedSentences = connection_memory.prepareStatement("SELECT * FROM DELETE_SENTENCES WHERE PATH = ? AND METHOD_NUMBER = ?;");
		selectDeletedTokens = connection_memory.prepareStatement("SELECT * FROM DELETE_TOKENS WHERE PATH = ? AND METHOD_NUMBER = ?;");

		insertFileCloneInfo_storage = connection_storage.prepareStatement("INSERT INTO FILE_CLONES VALUES (?, ?, ?, ?, ?)");
		insertMethodCloneInfo_storage = connection_storage.prepareStatement("INSERT INTO METHOD_CLONES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		insertCodeFragmentCloneInfo_storage = connection_storage.prepareStatement("INSERT INTO CODEFRAGMENT_CLONES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
	}

	public static void closeDB() throws SQLException {
		connection_memory.commit();

		try (ResultSet results = statement_memory.executeQuery("SELECT * FROM FILE_CLONES")) {
			while (results.next()) {
				insertFileCloneInfo_storage.setInt(1, results.getInt(1));
				insertFileCloneInfo_storage.setString(2, results.getString(2));
				insertFileCloneInfo_storage.setString(3, results.getString(3));
				insertFileCloneInfo_storage.setInt(4, results.getInt(4));
				insertFileCloneInfo_storage.setInt(5, results.getInt(5));
				insertFileCloneInfo_storage.addBatch();
			}
		}
		insertFileCloneInfo_storage.executeBatch();

		try (ResultSet results = statement_memory.executeQuery("SELECT * FROM METHOD_CLONES")) {
			while (results.next()) {
				insertMethodCloneInfo_storage.setInt(1, results.getInt(1));
				insertMethodCloneInfo_storage.setString(2, results.getString(2));
				insertMethodCloneInfo_storage.setString(3, results.getString(3));
				insertMethodCloneInfo_storage.setInt(4, results.getInt(4));
				insertMethodCloneInfo_storage.setInt(5, results.getInt(5));
				insertMethodCloneInfo_storage.setInt(6, results.getInt(6));
				insertMethodCloneInfo_storage.setString(7, results.getString(7));
				insertMethodCloneInfo_storage.setString(8, results.getString(8));
				insertMethodCloneInfo_storage.setInt(9, results.getInt(9));
				insertMethodCloneInfo_storage.setInt(10, results.getInt(10));
				insertMethodCloneInfo_storage.setInt(11, results.getInt(11));
				insertMethodCloneInfo_storage.setInt(12, results.getInt(12));
				insertMethodCloneInfo_storage.setInt(13, results.getInt(13));
				insertMethodCloneInfo_storage.addBatch();
			}
		}
		insertMethodCloneInfo_storage.executeBatch();

		if (statement_memory != null) statement_memory.close();
		if (insertDeletedMethodInfo != null) insertDeletedMethodInfo.close();
		if (insertDeletedSentenceInfo != null) insertDeletedSentenceInfo.close();
		if (insertDeletedTokenInfo != null) insertDeletedTokenInfo.close();
		if (insertFileCloneInfo_memory != null) insertFileCloneInfo_memory.close();
		if (insertMethodCloneInfo_memory != null) insertMethodCloneInfo_memory.close();
		if (selectFileClonePath2 != null) selectFileClonePath2.close();
		if (selectFileClonePath1 != null) selectFileClonePath1.close();
		if (selectMethodClonePath2 != null) selectMethodClonePath2.close();
		if (selectMethodClonePath1 != null) selectMethodClonePath1.close();
		if (selectDeletedMethods != null) selectDeletedMethods.close();
		if (selectDeletedSentences != null) selectDeletedSentences.close();
		if (selectDeletedTokens != null) selectDeletedTokens.close();
		if (connection_memory != null) connection_memory.close();

		connection_storage.commit();
		if (statement_storage != null) statement_storage.close();
		if (insertFileCloneInfo_storage != null) insertFileCloneInfo_storage.close();
		if (insertMethodCloneInfo_storage != null) insertMethodCloneInfo_storage.close();
		if (insertCodeFragmentCloneInfo_storage != null) insertCodeFragmentCloneInfo_storage.close();
		if (connection_storage != null) connection_storage.close();
	}
}
