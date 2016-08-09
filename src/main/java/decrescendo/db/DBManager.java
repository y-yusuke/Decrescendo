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
	public static PreparedStatement searchdmStatement;
	public static PreparedStatement searchmc1Statement;
	public static PreparedStatement searchmc2Statement;
	public static PreparedStatement searchdsStatement;

	public static void dbSetup() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + Config.outputDirectory);
		connection.setAutoCommit(false);
		statement = connection.createStatement();
		initialDB();
		createDB();
		createPrepareStatement();
	}

	private static void initialDB() throws SQLException {
		statement.executeUpdate("drop table if exists DELETE_METHODS");
		statement.executeUpdate("drop table if exists DELETE_SENTENCES");
		statement.executeUpdate("drop table if exists FILE_CLONES");
		statement.executeUpdate("drop table if exists METHOD_CLONES");
		statement.executeUpdate("drop table if exists CODEFRAGMENT_CLONES");
	}

	private static void createDB() throws SQLException {
		statement.executeUpdate("create table DELETE_METHODS("
				+ "PATH string, "
				+ "METHOD_NAME string, "
				+ "METHOD_NUMBER integer, "
				+ "START_LINE integer, "
				+ "END_LINE integer, "
				+ "ORIGINAL_HASH string, "
				+ "NORMALIZED_HASH string)");
		statement.executeUpdate("create index mpathIndex on DELETE_METHODS(PATH)");
		statement.executeUpdate("create index mnumIndex on DELETE_METHODS(METHOD_NUMBER)");

		statement.executeUpdate("create table DELETE_SENTENCES("
				+ "PATH string, "
				+ "METHOD_NAME string, "
				+ "METHOD_NUMBER integer, "
				+ "SENTENCE_NUMBER integer, "
				+ "START_LINE integer, "
				+ "END_LINE integer, "
				+ "ORIGINAL_HASH BLOB, "
				+ "NORMALIZED_HASH BLOB)");
		statement.executeUpdate("create index spathIndex on DELETE_SENTENCES(PATH)");
		statement.executeUpdate("create index mnum2Index on DELETE_SENTENCES(METHOD_NUMBER)");

		statement.executeUpdate("create table FILE_CLONES("
				+ "ID intege, "
				+ "PATH1 string, "
				+ "START_LINE1 integer, "
				+ "END_LINE1 integer, "
				+ "PATH2 string, "
				+ "START_LINE2 integer, "
				+ "END_LINE2 integer, "
				+ "CLONESET_ID integer, "
				+ "TYPE integer)");
		statement.executeUpdate("create index fcIndex1 on FILE_CLONES(PATH1)");
		statement.executeUpdate("create index fcIndex2 on FILE_CLONES(PATH2)");

		statement.executeUpdate("create table METHOD_CLONES("
				+ "ID integer, "
				+ "PATH1 string, "
				+ "METHOD_NAME1 string, "
				+ "METHOD_NUMBER1 integer, "
				+ "START_LINE1 integer, "
				+ "END_LINE1 integer, "
				+ "PATH2 string, "
				+ "METHOD_NAME2 string, "
				+ "METHOD_NUMBER2 integer, "
				+ "START_LINE2 integer, "
				+ "END_LINE2 integer, "
				+ "CLONESET_ID integer, "
				+ "TYPE integer)");
		statement.executeUpdate("create index mcIndex1 on METHOD_CLONES(PATH1)");
		statement.executeUpdate("create index mcIndex2 on METHOD_CLONES(PATH2)");

		statement.executeUpdate("create table CODEFRAGMENT_CLONES("
				+ "ID integer, "
				+ "PATH1 string, "
				+ "METHOD_NAME1 string, "
				+ "METHOD_NUMBER1 integer, "
				+ "START_LINE1 integer, "
				+ "END_LINE1 integer, "
				+ "GAP_LINE1 string, "
				+ "PATH2 string, "
				+ "METHOD_NAME2 string, "
				+ "METHOD_NUMBER2 integer, "
				+ "START_LINE2 integer, "
				+ "END_LINE2 integer, "
				+ "GAP_LINE2 string, "
				+ "CLONESET_ID integer, "
				+ "TYPE integer)");
	}

	private static void createPrepareStatement() throws SQLException {
		String sql = "insert into DELETE_METHODS values (?, ?, ?, ?, ?, ?, ?)";
		mStatement = connection.prepareStatement(sql);

		sql = "insert into DELETE_SENTENCES values (?, ?, ?, ?, ?, ?, ?, ?)";
		sStatement = connection.prepareStatement(sql);

		sql = "insert into FILE_CLONES values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		fcStatement = connection.prepareStatement(sql);

		sql = "insert into METHOD_CLONES values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		mcStatement = connection.prepareStatement(sql);

		sql = "insert into CODEFRAGMENT_CLONES values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?)";
		cfcStatement = connection.prepareStatement(sql);

		sql = "select PATH2 from FILE_CLONES where PATH1 = ?;";
		searchfc1Statement = connection.prepareStatement(sql);

		sql = "select PATH1 from FILE_CLONES where PATH2 = ?;";
		searchfc2Statement = connection.prepareStatement(sql);

		sql = "select PATH2, METHOD_NUMBER2 from METHOD_CLONES where PATH1 = ?  AND METHOD_NUMBER1 = ?;";
		searchmc1Statement = connection.prepareStatement(sql);

		sql = "select PATH1, METHOD_NUMBER1 from METHOD_CLONES where PATH2 = ?  AND METHOD_NUMBER2 = ?;";
		searchmc2Statement = connection.prepareStatement(sql);

		sql = "select * from DELETE_METHODS where PATH = ? AND METHOD_NUMBER = ?;";
		searchdmStatement = connection.prepareStatement(sql);

		sql = "select * from DELETE_SENTENCES where PATH = ? AND METHOD_NUMBER = ?;";
		searchdsStatement = connection.prepareStatement(sql);
	}

	public static void closeDB() throws SQLException {
		connection.commit();
		if(statement != null) statement.close();
		if(mStatement != null) mStatement.close();
		if(sStatement != null) sStatement.close();
		if(fcStatement != null) fcStatement.close();
		if(mcStatement != null) mcStatement.close();
		if(cfcStatement != null) cfcStatement.close();
		if(searchfc1Statement != null) searchfc1Statement.close();
		if(searchfc2Statement != null) searchfc2Statement.close();
		if(searchmc1Statement != null) searchmc1Statement.close();
		if(searchmc2Statement != null) searchmc2Statement.close();
		if(searchdmStatement != null) searchdmStatement.close();
		if(searchdsStatement != null) searchdsStatement.close();
		if(connection != null) connection.close();
	}
}
