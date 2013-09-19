package dataStore;
import java.sql.*;
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

	// Store all these data to a database. Keep these databases synchronized between all Resolver instances

	
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
		connection = DriverManager.getConnection("jdbc:sqlite:users.db");
		statement = connection.createStatement();
		// Check if this is brandnew database, or we just reopened an existing one
		// We do this by searching for one table. if it exists, then this is not a brandnew DB
		// Else, we create all the tables
		String query = "SELECT name FROM sqlite_master WHERE type=\'table' AND name=\'table_name\';";
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
		}
		//else{
		// Database already exists. Do nothing.
		// Or sing the daisy bell: http://www.youtube.com/watch?v=41U78QP8nBk
		//}
	}
}
