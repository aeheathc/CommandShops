package com.aehdev.lib.multiDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.StringCharacterIterator;
import java.util.logging.Logger;

/**
 * Represents a MySQL database. Intended to be used as a {@link Database}.
 */
public class MySQL extends Database
{
	/** Hostname of the server having this database. */
	private String hostname = "localhost";
	
	/** Port number that the database server is listening on. */
	private String portnmbr = "3306";
	
	/** Username needed to login to the database. */
	private String username = "minecraft";
	
	/** Password needed to login to the database. */
	private String password = "";
	
	/** The Database Name. */
	private String database = "minecraft";
	
	/**
	 * Define a MySQL connection and open it for use.
	 * 
	 * @param log reference to the main logger
	 * @param prefix prefix for all log messages, should be the plugin name
	 * @param hostname Hostname of the server having this database.
	 * @param portnmbr Port number that the database server is listening on.
	 * @param database The Database Name.
	 * @param username Username needed to login to the database.
	 * @param password Password needed to login to the database.
	 */
	public MySQL(Logger log, String prefix, String hostname, String portnmbr, String database, String username, String password)
	{
		super(log,prefix,"[MySQL] ");
		this.hostname = hostname;
		this.portnmbr = portnmbr;
		this.database = database;
		this.username = username;
		this.password = password;
		open();
	}
	
	/**
	 * Closes the database connection when this object is destroyed.
	 */
	@Override
	protected void finalize()
	{
		close();
	}
	
	/**
	 * Check for the DB driver
	 * @return true if the DB driver was successfully loaded
	 */
	@Override
	protected boolean initialize()
	{
		try{
			Class.forName("com.mysql.jdbc.Driver");
			return true;
	    }catch(ClassNotFoundException e){
	    	this.writeError("Class not found in initialize(): " + e.getMessage() + ".", true);
	    	return false;
	    }
	}
	
	/**
	 * Open the database connection
	 * @return the Connection object for this database connection
	 */
	@Override
	public Connection open()
	{
		if(initialize())
		{
			String url = "";
		    try{
				url = "jdbc:mysql://" + this.hostname + ":" + this.portnmbr + "/" + this.database;
				this.connection = DriverManager.getConnection(url, this.username, this.password);
				return this.connection;
		    }catch(SQLException e){
		    	this.writeError(url,true);
		    	this.writeError("SQL exception in open(): " + e.getMessage() + ".", true);
		    }
		}
		return null;
	}
	
	/**
	 * Close the database connection.
	 */
	@Override
	public void close()
	{
		try{
			if(connection != null) connection.close();
		}catch(Exception e){
			this.writeError("Exception in close(): " + e.getMessage(), true);
		}
	}
	

	/**
	 * Check the status of the database connection.
	 * @return true if the connection is open and valid
	 */
	@Override
	public boolean checkConnection()
	{
		if(connection != null) return true;
		return false;
	}
	
	/**
	 * Sends a query to the SQL database.
	 * @param query the SQL query to send to the database.
	 * @param supressErrors whether to suppress error logging
	 * @return the table of results from the query.
	 */
	@Override
	public ResultSet query(String query, boolean suppressErrors)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			if(!connection.isValid(1))
			{
				connection.close();
				connection = null;
			}
		}catch(SQLException e1){
			connection = null;
		}
		if(connection == null) open();
		try{
		    statement = this.connection.createStatement();
		    if(query.trim().toUpperCase().startsWith("SELECT"))
		    	result = statement.executeQuery(query);
		    else
		    	statement.executeUpdate(query);
	    	return result;
		}catch(SQLException e){
			if(!suppressErrors)
				this.writeError("SQL exception in query(): " + e.getMessage() + " Query in full: " + query, false);
		}
		return result;
	}
	

	/**
	 * Make string safe for SQL query similarly to PHP's addslashes().
	 * 
	 * @param text the text to escape
	 * @return the string
	 * @return
	 */
	@Override
	public String escape(String text)
	{
		final StringBuffer sb = new StringBuffer( text.length() * 2 );
		final StringCharacterIterator iterator = new StringCharacterIterator( text );
		char character = iterator.current();
		while( character != StringCharacterIterator.DONE )
		{
			if( character == '"' ) sb.append( "\\\"" );
			else if( character == '\'' ) sb.append( "\\\'" );
			else if( character == '\\' ) sb.append( "\\\\" );
			else if( character == '\n' ) sb.append( "\\n" );
			else if( character == '{' ) sb.append( "\\{" );
			else if( character == '}' ) sb.append( "\\}" );
			else sb.append( character );
			character = iterator.next();
		}
		return sb.toString();
	}
}