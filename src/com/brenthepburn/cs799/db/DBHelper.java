package com.brenthepburn.cs799.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBHelper {
	private Properties dbProperties;
	private String dbName;
	private static DBHelper instance;

	private static final String strCreateTable = "create table CA.SIGNEDCERTS ("
			+ "    LASTNAME    VARCHAR(256), "
			+ "    FIRSTNAME   VARCHAR(256), "
			+ "	   EXPONENT    VARCHAR(256), "
			+ "    MODULUS	   VARCHAR(256), "
			+ "    TIMESTAMP   VARCHAR(256), "
			+ "    EMAIL       VARCHAR(30) NOT NULL PRIMARY KEY" + ")";

	private DBHelper() {
		dbName = "ca";
		setDBSystemDir();
		dbProperties = loadDBProperties();
		String driverName = dbProperties.getProperty("derby.driver");
		loadDatabaseDriver(driverName);
		if (!dbExists()) {
			createDatabase();
		}
	}

	public static DBHelper getInstance() {
		if (instance == null)
			instance = new DBHelper();
		return instance;
	}

	private void loadDatabaseDriver(String driverName) {
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	private static void setDBSystemDir() {
		System.setProperty("derby.system.home", "./db");
		File fileSystemDir = new File("./db");
		fileSystemDir.mkdir();
		try {
			System.out.println(fileSystemDir.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean dbExists() {
		boolean bExists = false;
		String dbLocation = getDatabaseLocation();
		File dbFileDir = new File(dbLocation);
		if (dbFileDir.exists()) {
			bExists = true;
		}
		return bExists;
	}

	public String getDatabaseUrl() {
		String dbUrl = dbProperties.getProperty("derby.url") + dbName;
		return dbUrl;
	}

	public String getDatabaseLocation() {
		String dbLocation = System.getProperty("derby.system.home") + "/"
				+ dbName;
		return dbLocation;
	}

	private Properties loadDBProperties() {
		InputStream dbPropInputStream = DBHelper.class
				.getResourceAsStream("db.properties");
		dbProperties = new Properties();
		try {
			dbProperties.load(dbPropInputStream);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return dbProperties;
	}

	private boolean createDatabase() {
		boolean bCreated = false;
		Connection dbConnection = null;

		String dbUrl = getDatabaseUrl();
		dbProperties.put("create", "true");

		try {
			dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
			bCreated = createTables(dbConnection);
			dbConnection.close();
		} catch (SQLException ex) {
		}
		dbProperties.remove("create");
		return bCreated;
	}

	private boolean createTables(Connection dbConnection) {
		boolean bCreatedTables = false;
		Statement statement = null;
		try {
			statement = dbConnection.createStatement();
			statement.execute(strCreateTable);
			bCreatedTables = true;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return bCreatedTables;
	}

	public void saveCert(Cert cert) throws CertException {
		String dbUrl = getDatabaseUrl();

		try {
			Connection dbConnection = DriverManager.getConnection(dbUrl,
					dbProperties);

			String stmt = "INSERT INTO CA.SIGNEDCERTS (LASTNAME, FIRSTNAME, EMAIL, MODULUS, EXPONENT, TIMESTAMP) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement statement = dbConnection.prepareStatement(stmt);
			statement.setString(1, cert.getlName());
			statement.setString(2, cert.getfName());
			statement.setString(3, cert.getEmail());
			statement.setString(4, cert.getModulus());
			statement.setString(5, cert.getKey());
			statement.setString(6, cert.getTimestamp());
			statement.executeUpdate();

			dbConnection.close();
		} catch (SQLException sqle) {
			throw new CertException(
					"Cannot create your cert.  There may be one generated for this email already.");
		}
	}

	public List<Cert> getAllCerts() throws SQLException {
		String dbUrl = getDatabaseUrl();
		Connection dbConnection = DriverManager.getConnection(dbUrl,
				dbProperties);
		List<Cert> listEntries = new ArrayList<Cert>();
		try {
			String stmt = "SELECT FIRSTNAME,LASTNAME,EMAIL, MODULUS, EXPONENT, TIMESTAMP from CA.SIGNEDCERTS";
			Statement queryStatement = dbConnection.createStatement();
			ResultSet results = queryStatement.executeQuery(stmt);
			while (results.next()) {
				Cert cert = new Cert();
				cert.setfName(results.getString(1));
				cert.setlName(results.getString(2));
				cert.setEmail(results.getString(3));
				cert.setModulus(results.getString(4));
				cert.setKey(results.getString(5));
				cert.setTimestamp(results.getString(6));
				listEntries.add(cert);
			}
			dbConnection.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return listEntries;
	}

	public void deleteCert(String email) throws SQLException {
		String dbUrl = getDatabaseUrl();
		Connection dbConnection = DriverManager.getConnection(dbUrl,
				dbProperties);
		try {
			PreparedStatement statement = dbConnection
					.prepareStatement("DELETE FROM CA.SIGNEDCERTS WHERE EMAIL = ?");
			statement.setString(1, email);
			statement.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	public Cert getCert(String email) throws SQLException {
		String dbUrl = getDatabaseUrl();
		Connection dbConnection = DriverManager.getConnection(dbUrl,
				dbProperties);
		try {
			String stmt = "SELECT FIRSTNAME,LASTNAME,EMAIL, MODULUS, EXPONENT, TIMESTAMP from CA.SIGNEDCERTS where email='"
					+ email + "'";
			Statement queryStatement = dbConnection.createStatement();
			ResultSet results = queryStatement.executeQuery(stmt);
			while (results.next()) {
				Cert cert = new Cert();
				cert.setfName(results.getString(1));
				cert.setlName(results.getString(2));
				cert.setEmail(results.getString(3));
				cert.setModulus(results.getString(4));
				cert.setKey(results.getString(5));
				cert.setTimestamp(results.getString(6));
				dbConnection.close();
				return cert;
			}
			dbConnection.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return null;
	}

}
