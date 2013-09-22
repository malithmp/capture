package dataStore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DatabaseHelper {
	// Handle all persistent data
	// Example:	user database (including the name and password)
	//			inactive maps
	//			inactive servlet data
	// TODO Googling did not give a clear answer to the question "is SQLite Thread Safe", So using syncronized access for ALL the actions
	// TODO When/If we replace SQLite with a proper Database Management System (hope not) remove the synchronized keywords
	// TODO Since these databases accesses are not frequent, it probably wont matter that much
	// TODO DB access will occur when user logs in, Map is unloaded/unloaded. 
	// TODO Time consuming synchronized tasks such as Maps loading/unloading happen when the servers are not serving users
	// TODO Note; this map loading/unloading is not the map passing between servlets. This is persistent storage.

	// JDBC info https://bitbucket.org/xerial/sqlite-jdbc
	
	// Store all these data to a database. Keep these databases synchronized between all Resolver instances
	public static final String DBNAME="users.db";
	public static final String TABLE_USERPASS = "USERPASS";
	public static final String COLUMN_ID = "ID";
	public static final String COLUMN_USERNAME="USERNAME";
	public static final String COLUMN_HASH ="HASH";
	public static final String COLUMN_SALT="SALT";
	
	Connection connection = null;
	Statement statement = null;
	ResultSet result = null;

	// We dont know if SQLite is really thread safe. Since this function is not called so often, make it syncronized
	public synchronized void initDatabase() throws Exception{
		// Create Databses.Create Tables.
		// Called only on startup of the servlet
		// This opens an existing database, or creates a new one if it does not already exist

		// Open database
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:"+DBNAME);
	    
		System.out.println("WARNING: users.db file created in /Users/malithmp/Development/eclipse/Eclipse.app/Contents/MacOS/");
		statement = connection.createStatement();
		// Check if this is brandnew database, or we just reopened an existing one
		// We do this by searching for one table. if it exists, then this is not a brandnew DB
		// Else, we create all the tables
		String query = "SELECT name FROM sqlite_master WHERE type=\'table' AND name=\'"+TABLE_USERPASS+"\';";
		System.out.println(query);
		result=null;
		result = statement.executeQuery(query);
		if(result==null){
			// Something wet wrog executing the query
			System.out.println("WARNING: SQL execution failed!");
			System.out.println("\t for query: "+query);
		}
		else if(result.next() == false){
			// The result set is empty, That means the table is not there. So its safe to assume that the database itself is not there
			// create one
			// Requred fields Read the database.txt file for info

			// Create USERPASS table
			System.out.println("INFO: Creating Tables");
			query = "CREATE TABLE " +
					TABLE_USERPASS 	+ "(" +
					COLUMN_ID		+" INTEGER PRIMARY KEY AUTOINCREMENT," +
					COLUMN_USERNAME	+" TEXT NOT NULL UNIQUE, " + 
					COLUMN_SALT		+" TEXT NOT NULL, " + 
					COLUMN_HASH		+" TEXT NOT NULL)";
			statement.executeUpdate(query);
			statement.close();
		}
		//else{
			// Database already exists. Do nothing.
			// Or sing the daisy bell: http://www.youtube.com/watch?v=41U78QP8nBk
		//}
	}
	
	public synchronized boolean updateUserPass(String username, String hash, String salt) throws Exception{
		// This table only hold username - password related data only
		// ONLY CALLED INTERNALLY BY THE SERVLETS, USERS HAVE NO DIRECT ACCESS USING HTTP REQUESTS!!!
		// A valid username must be acquired beforehand: see addUser
		// WE ASSUME THE USERNAME IS LEGIT (SINCE ITS A SERVER INTERNAL CALL AND COULD BE CALLED ONLY BY LEGIT USERS)
		// Check database to see if the username exists. If thats the case, update
		// Add username/password hash/salt if not 
		
		statement = connection.createStatement();
		
		// First check if the user is already in the database
		String query = "SELECT "		+
							COLUMN_ID		+" FROM "+
							TABLE_USERPASS	+ " WHERE " +
							COLUMN_USERNAME + "= \"" +
							username		+"\";";
		ResultSet result = statement.executeQuery(query);
		
		if(result!=null && result.next()!=false){
			// User data already in table
			// Update the data
			query = "UPDATE "	+
						TABLE_USERPASS 	+" SET " +
						COLUMN_HASH		+"= \""	+
						hash			+"\" ," +
						COLUMN_SALT		+"= \"" +
						salt			+"\" WHERE " +
						COLUMN_USERNAME +"= \"" +
						username		+"\";";
			boolean status = statement.execute(query);
			statement.close();
			return status;
		}
		else{
			// User not in table
			// Add the information to the table
			query = "INSERT INTO "	+
						TABLE_USERPASS	+"(" +
						COLUMN_ID		+"," +
						COLUMN_USERNAME	+"," +
						COLUMN_SALT		+"," +
						COLUMN_HASH 	+ ") VALUES(" +
						"null"			+",\"" +	/*ID is auto increment, so we dont care*/
						username		+"\",\"" +
						salt			+"\",\"" +
						hash			+"\");";
			
			boolean status = statement.execute(query);
			statement.close();
			return status;
		}
		
	}
	
	public synchronized void addUser(/*User user*/String username) throws Exception{
		// When a user is registering, the following steps are taken
		// User submits the full report about his/her details
		// The mobile app verifys certain data, such as password strength, username character limits and what nots
		// Data is accepted and the username is looked up on the database, if it does not exist, we add it
		// If the username already exists, we let the user know
		// Lets not be dicks and make our app in a way that every time this happens, user does not have to refill the entire form :D
		
		// The add the user information to one table
		// password information to the userpass table
		// we can then query the userpass table with the username
		System.out.println("WARNING: addUser not implemented");
		statement = connection.createStatement();
		String query = "SELECT "		+
				COLUMN_ID		+" FROM "+
				TABLE_USERPASS	+ " WHERE " +
				COLUMN_USERNAME + "= \"" +
				username		+"\";";
		ResultSet result = statement.executeQuery(query);
		if(result!=null && result.next()!=false){
			System.out.println("E: "+result.getInt(COLUMN_ID));
		}
		else{
			System.out.println("DNE");
		}
		
	}
}
