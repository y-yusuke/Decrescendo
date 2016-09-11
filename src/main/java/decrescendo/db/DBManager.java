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
	public static PreparedStatement mStatement;
	public static PreparedStatement sStatement;
	public static PreparedStatement fcStatement;
	public static PreparedStatement mcStatement;
	public static PreparedStatement cfcStatement;
	public static PreparedStatement searchfc1Statement;
	public static PreparedStatement searchfc2Statement;
	public static PreparedStatement searchmc1Statement;
	public static PreparedStatement searchmc2Statement;
	public static PreparedStatement searchdmStatement;
	public static PreparedStatement searchdsStatement;

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
		statement.executeUpdate("CREATE INDEX mpathIndex ON DELETE_METHODS(PATH)");
		statement.executeUpdate("CREATE INDEX mnumIndex ON DELETE_METHODS(METHOD_NUMBER)");

		statement.executeUpdate("CREATE TABLE DELETE_SENTENCES("
				+ "PATH string, "
				+ "METHOD_NAME string, "
				+ "METHOD_NUMBER INTEGER, "
				+ "SENTENCE_NUMBER INTEGER, "
				+ "START_LINE INTEGER, "
				+ "END_LINE INTEGER, "
				+ "ORIGINAL_HASH BLOB, "
				+ "NORMALIZED_HASH BLOB)");
		statement.executeUpdate("CREATE INDEX spathIndex ON DELETE_SENTENCES(PATH)");
		statement.executeUpdate("CREATE INDEX mnum2Index ON DELETE_SENTENCES(METHOD_NUMBER)");

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
		mStatement = connection.prepareStatement("INSERT INTO DELETE_METHODS VALUES (?, ?, ?, ?, ?, ?, ?)");

		sStatement = connection.prepareStatement("INSERT INTO DELETE_SENTENCES VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

		fcStatement = connection.prepareStatement("INSERT INTO FILE_CLONES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

		mcStatement = connection.prepareStatement("INSERT INTO METHOD_CLONES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		cfcStatement = connection.prepareStatement("INSERT INTO CODEFRAGMENT_CLONES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		searchfc1Statement = connection.prepareStatement("SELECT PATH2 FROM FILE_CLONES WHERE PATH1 = ?;");

		searchfc2Statement = connection.prepareStatement("SELECT PATH1 FROM FILE_CLONES WHERE PATH2 = ?;");

		searchmc1Statement = connection.prepareStatement("SELECT PATH2, METHOD_NUMBER2, METHOD_NAME2 FROM METHOD_CLONES WHERE PATH1 = ?  AND METHOD_NUMBER1 = ?;");

		searchmc2Statement = connection.prepareStatement("SELECT PATH1, METHOD_NUMBER1, METHOD_NAME1 FROM METHOD_CLONES WHERE PATH2 = ?  AND METHOD_NUMBER2 = ?;");

		searchdmStatement = connection.prepareStatement("SELECT * FROM DELETE_METHODS WHERE PATH = ? AND METHOD_NUMBER = ?;");

		searchdsStatement = connection.prepareStatement("SELECT * FROM DELETE_SENTENCES WHERE PATH = ? AND METHOD_NUMBER = ?;");
	}

	public static void closeDB() throws SQLException {
		connection.commit();
		if (statement != null) statement.close();
		if (mStatement != null) mStatement.close();
		if (sStatement != null) sStatement.close();
		if (fcStatement != null) fcStatement.close();
		if (mcStatement != null) mcStatement.close();
		if (cfcStatement != null) cfcStatement.close();
		if (searchfc1Statement != null) searchfc1Statement.close();
		if (searchfc2Statement != null) searchfc2Statement.close();
		if (searchmc1Statement != null) searchmc1Statement.close();
		if (searchmc2Statement != null) searchmc2Statement.close();
		if (searchdmStatement != null) searchdmStatement.close();
		if (searchdsStatement != null) searchdsStatement.close();
		if (connection != null) connection.close();
	}
}
