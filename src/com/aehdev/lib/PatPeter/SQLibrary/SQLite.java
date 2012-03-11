/**
 * SQLite
 * Inherited subclass for reading and writing to and from an SQLite file.
 * 
 * Date Created: 2011-08-26 19:08
 * @author PatPeter
 */
package com.aehdev.lib.PatPeter.SQLibrary;

/*
 * SQLite
 */
import java.io.File;
import java.sql.DatabaseMetaData;

/*
 * Both
 */
//import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.StringCharacterIterator;
import java.util.logging.Logger;

public class SQLite extends DatabaseHandler {
	public String location;
	public String name;
	private File sqlFile;
	
	public SQLite(Logger log, String prefix, String name, String location) {
		super(log,prefix,"[SQLite] ");
		this.name = name;
		this.location = location;
		File folder = new File(this.location);
		if (this.name.contains("/") ||
				this.name.contains("\\") ||
				this.name.endsWith(".db")) {
			this.writeError("The database name can not contain: /, \\, or .db", true);
		}
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		sqlFile = new File(folder.getAbsolutePath() + File.separator + name + ".db");
	}
	

	protected boolean initialize() {
		try {
		  Class.forName("org.sqlite.JDBC");
		  
		  return true;
		} catch (ClassNotFoundException e) {
		  this.writeError("You need the SQLite library " + e, true);
		  return false;
		}
	}
	
	@Override
	public Connection open() {
		if (initialize()) {
			try {
			  return DriverManager.getConnection("jdbc:sqlite:" +
					  	   sqlFile.getAbsolutePath());
			} catch (SQLException e) {
			  this.writeError("SQLite exception on initialize " + e, true);
			}
		}
		return null;
	}
	
	@Override
	public void close() {
		Connection connection = this.open();
		if (connection != null)
			try {
				connection.close();
			} catch (SQLException ex) {
				this.writeError("Error on Connection close: " + ex, true);
			}
	}
	
	@Override
	public Connection getConnection() {
		if (this.connection == null)
			return open();
		return this.connection;
	}
	
	@Override
	public boolean checkConnection() {
		Connection connection = this.open();
		if (connection != null)
			return true;
		return false;
	}
	
	@Override
	public ResultSet query(String query, boolean suppressErrors, Connection connection) throws SQLException
	{
		Statement statement = null;
		ResultSet result = null;
		
		boolean oneshot = connection == null;
		
		try {
			if(oneshot) connection = this.open();
			statement = connection.createStatement();
			
			switch (this.getStatement(query)) {
				case SELECT:
				result = statement.executeQuery(query);
				return result;
				
				case UPDATE:
				case DELETE:
				//we remove LIMITs from UPDATEs for compatibility
				// with SQLite libraries not compiled with SQLITE_ENABLE_UPDATE_DELETE_LIMIT
				int end = query.indexOf("LIMIT");
				if(end > 0) query = query.substring(0,end);
				statement.executeQuery(query);
				return result;
				
				default:
				statement.executeQuery(query);
				return result;	
			}
		} catch (SQLException ex) {
			if(!ex.getMessage().equals("query does not return ResultSet") && !suppressErrors)
			{
				this.writeError("Error at SQL Query: " + ex.getMessage() + " Query in full: " + query, false);
				throw ex;
			}
		}
		return result;
	}

	@Override
	PreparedStatement prepare(String query) {
		Connection connection = null;
		try
	    {
	        connection = open();
	        PreparedStatement ps = connection.prepareStatement(query);
	        return ps;
	    } catch(SQLException e) {
	        if(!e.toString().contains("not return ResultSet"))
	        	this.writeError("Error in SQL prepare() query: " + e.getMessage(), false);
	    }
	    return null;
	}
	
	@Override
	public boolean createTable(String query) {
		Connection connection = open();
		Statement statement = null;
		try {
			if (query.equals("") || query == null) {
				this.writeError("SQL Create Table query empty.", true);
				return false;
			}
			
			statement = connection.createStatement();
			statement.execute(query);
			return true;
		} catch (SQLException ex){
			this.writeError(ex.getMessage(), true);
			return false;
		}
	}
	
	@Override
	public boolean checkTable(String table) {
		DatabaseMetaData dbm = null;
		try {
			dbm = this.open().getMetaData();
			ResultSet tables = dbm.getTables(null, null, table, null);
			if (tables.next())
			  return true;
			else
			  return false;
		} catch (SQLException e) {
			this.writeError("Failed to check if table \"" + table + "\" exists: " + e.getMessage(), true);
			return false;
		}
	}
	
	@Override
	public boolean wipeTable(String table) {
		Connection connection = open();
		Statement statement = null;
		String query = null;
		try {
			if (!this.checkTable(table)) {
				this.writeError("Error at Wipe Table: table, " + table + ", does not exist", true);
				return false;
			}
			statement = connection.createStatement();
			query = "DELETE FROM " + table + ";";
			statement.executeQuery(query);
			return true;
		} catch (SQLException ex) {
			if(!ex.toString().contains("not return ResultSet"))
					this.writeError("Error at SQL Wipe Table Query: " + ex, false);
			return false;
		}
	}
	
	/**
	 * Make string safe for SQL query using dumb Pascal-style thing which is the only method sqlite supports,
	 * despite not being standard SQL, because they don't support backslash escaping because "it's not standard SQL".
	 * @param text the text to escape
	 * @return
	 */
	public String escape(String text)
	{
        final StringBuffer sb                   = new StringBuffer( text.length() * 2 );
        final StringCharacterIterator iterator  = new StringCharacterIterator( text );
  	  	char character = iterator.current();

        while( character != StringCharacterIterator.DONE )
        {
            if( character == '\'' ) sb.append( "\'\'" );
            else sb.append( character );
	            
            character = iterator.next();
        }
        return sb.toString();
	}
}