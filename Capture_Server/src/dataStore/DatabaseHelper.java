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
	// TODO TRY TO IMPLEMENT SOME CACHING FOR TABLES LIKE TABLE_INSTITUTEDOM
	// JDBC info https://bitbucket.org/xerial/sqlite-jdbc

	// Store all these data to a database. Keep these databases synchronized between all Resolver instances
	public static final String DBNAME="SERVER_PERSISTANT.db";
	public static final String TABLE_USERPASS = "USERPASS";					// Table that contains username, password hash and salt
	public static final String TABLE_USERDATA = "USERDATA";					// Table that contains the rest of the user profile information
	public static final String TABLE_INSTITUTE = "INSTITUTE";				// Table that contains the rest information about institutes

	public static final String COLUMN_USERPASS_ID = "ID";					// ID Column name in userpass table	
	public static final String COLUMN_USERPASS_USERNAME="USERNAME";			// Username Column name in userpass table
	public static final String COLUMN_USERPASS_HASH ="HASH";				// Hash Column name in userpass table
	public static final String COLUMN_USERPASS_SALT="SALT";					// Salt Column name in userpass table

	public static final String COLUMN_USERDATA_ID = "ID";					// ID Column name in userdata table	
	public static final String COLUMN_USERDATA_USERNAME="USERNAME";			// Username Column name in userdata table
	public static final String COLUMN_USERDATA_EMAIL ="EMAIL";				// Email Address Column name in userdata table
	public static final String COLUMN_USERDATA_FIRSTNAME="FNAME";			// First Name Column name in userdata table
	public static final String COLUMN_USERDATA_LASTNAME="LNAME";			// Last Name Column name in userdata table
	public static final String COLUMN_USERDATA_L1GROUP="L1GROUP";			// L1 Group Column name in userdata table
	public static final String COLUMN_USERDATA_L2GROUP="L2GROUP";			// L2 Group Column name in userdata table
	public static final String COLUMN_USERDATA_L3GROUP="L3GROUP";			// L3 Group Column name in userdata table
	public static final String COLUMN_USERDATA_HOME="HOME";					// Home Column name in userdata table

	public static final String COLUMN_INSTITUTE_ID = "ID";					// ID Column name in userdata table	
	public static final String COLUMN_INSTITUTE_INSTITUTENAME="INAME";		// Username Column name in userdata table
	public static final String COLUMN_INSTITUTE_DOMAIN="DOMAIN";			// Email Address Column name in userdata table
	public static final String COLUMN_INSTITUTE_DATA_FILE_PATH="PATH";		// Path to the file that contain additional data

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

		System.out.println("WARNING: "+DBNAME + " file created in /Users/malithmp/Development/eclipse/Eclipse.app/Contents/MacOS/");
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
					COLUMN_USERPASS_ID		+" INTEGER PRIMARY KEY AUTOINCREMENT," +
					COLUMN_USERPASS_USERNAME	+" TEXT NOT NULL UNIQUE, " + 
					COLUMN_USERPASS_SALT		+" TEXT NOT NULL, " + 
					COLUMN_USERPASS_HASH		+" TEXT NOT NULL)";

			statement.executeUpdate(query);

			// Create USERDATA table
			query = "CREATE TABLE " +
					TABLE_USERDATA 	+ "(" +
					COLUMN_USERDATA_ID		+" INTEGER PRIMARY KEY AUTOINCREMENT," +
					COLUMN_USERDATA_USERNAME	+" TEXT NOT NULL UNIQUE, " + 
					COLUMN_USERDATA_EMAIL		+" TEXT NOT NULL UNIQUE, " + 
					COLUMN_USERDATA_FIRSTNAME	+" TEXT, " + 
					COLUMN_USERDATA_LASTNAME	+" TEXT, " + 
					COLUMN_USERDATA_L1GROUP	+" TEXT, " + 
					COLUMN_USERDATA_L2GROUP	+" TEXT, " + 
					COLUMN_USERDATA_L3GROUP	+" TEXT, " + 
					COLUMN_USERDATA_HOME	+" TEXT)";

			statement.executeUpdate(query);


			// Create INSTITUTEDOMAIN table
			query = "CREATE TABLE " +
					TABLE_INSTITUTE 					+ "(" +
					COLUMN_INSTITUTE_ID				+" INTEGER PRIMARY KEY AUTOINCREMENT," +
					COLUMN_INSTITUTE_INSTITUTENAME	+" TEXT NOT NULL UNIQUE, " + 
					COLUMN_INSTITUTE_DATA_FILE_PATH	+" TEXT NOT NULL UNIQUE, " + 
					COLUMN_INSTITUTE_DOMAIN			+" TEXT NOT NULL UNIQUE)";

			statement.executeUpdate(query);

			statement.close();
		}
		//else{
		// Database already exists. Do nothing.
		// Or sing the daisy bell: http://www.youtube.com/watch?v=41U78QP8nBk
		//}
	}

	public synchronized boolean updateUserPass(String username, String hash, String salt) throws Exception{
		// Used when the user changes the password
		// ONLY CALLED INTERNALLY BY THE SERVLETS, USERS HAVE NO DIRECT ACCESS USING HTTP REQUESTS!!!
		// Used to update the user password
		// Check database to see if the username exists. If thats the case, update
		// Add username/password hash/salt if not 

		statement = connection.createStatement();

		// First check if the user is already in the database
		String query = "SELECT "		+
				COLUMN_USERPASS_ID		+" FROM "+
				TABLE_USERPASS	+ " WHERE " +
				COLUMN_USERPASS_USERNAME + "= \"" +
				username		+"\";";
		ResultSet result = statement.executeQuery(query);

		if(result!=null && result.next()!=false){
			// User data already in table
			// Update the data
			query = "UPDATE "	+
					TABLE_USERPASS 	+" SET " +
					COLUMN_USERPASS_HASH		+"= \""	+
					hash			+"\" ," +
					COLUMN_USERPASS_SALT		+"= \"" +
					salt			+"\" WHERE " +
					COLUMN_USERPASS_USERNAME +"= \"" +
					username		+"\";";
			statement.execute(query);
			statement.close();
			return true;
		}
		else{
			// User not in table
			return false;
		}

	}

	public synchronized boolean acquireUsername(String username, String hash, String salt) throws Exception{
		// Used by users who are signing up and trying to acquire a username
		// ONLY CALLED INTERNALLY BY THE SERVLETS, USERS HAVE NO DIRECT ACCESS USING HTTP REQUESTS!!!
		// Check database to see if the username exists. If thats the case that means the usernam is already taken. return false
		// Otherwise the username is unique so we can procede to adding it
		// Add username/password hash/salt if not 

		statement = connection.createStatement();

		// First check if the user is already in the database
		String query = "SELECT "		+
				COLUMN_USERPASS_ID		+" FROM "+
				TABLE_USERPASS	+ " WHERE " +
				COLUMN_USERPASS_USERNAME + "= \"" +
				username		+"\";";
		ResultSet result = statement.executeQuery(query);

		if(result!=null && result.next()!=false){
			// User data already in table
			// Username is not unique
			return false;
		}
		else{
			// User not in table
			// Add the username (and the password stuff)
			// Add the information to the table
			query = "INSERT INTO "	+
					TABLE_USERPASS	+"(" +
					COLUMN_USERPASS_ID		+"," +
					COLUMN_USERPASS_USERNAME	+"," +
					COLUMN_USERPASS_SALT		+"," +
					COLUMN_USERPASS_HASH 	+ ") VALUES(" +
					"null"			+",\"" +	/*ID is auto increment, so we dont care*/
					username		+"\",\"" +
					salt			+"\",\"" +
					hash			+"\");";
			statement.execute(query);
			statement.close();

			return true;
		}

	}

	public synchronized boolean addUser(/*User user*/User user) throws Exception{
		// When a user is registering, the following steps are taken
		// User submits the full report about their details
		// The mobile app verifies certain data, such as password strength, username character limits and what nots
		// Data is accepted and the username is looked up on the database, if it does not exist, we add it (USING THE UPDATEUSERPASS METHOD)..If the username already exists, it let the user know
		// Lets not be dicks and make our app in a way that every time this happens, user does not have to refill the entire form :D

		// If the user gets here , that means that they acquired a unique username
		// We first confirm this (that the username is actually in the table)
		// The add the user information to userData table

		// TODO If we (by any chance) plan to move to a thread safe model, MAKESURE to use locks before adding the username
		// TODO We acquire a lock, check if the username is unique, if it is, add to userpass table, the release the lock
		// TODO Do not remove the above 2 TODO comments until its done. 


		// can add user

		String query = "INSERT INTO "				+
				TABLE_USERDATA				+"(" +
				COLUMN_USERDATA_ID			+"," +
				COLUMN_USERDATA_USERNAME	+"," +
				COLUMN_USERDATA_EMAIL		+"," +
				COLUMN_USERDATA_L1GROUP		+"," +
				COLUMN_USERDATA_L2GROUP		+"," +
				COLUMN_USERDATA_L3GROUP		+"," +
				COLUMN_USERDATA_FIRSTNAME	+"," +
				COLUMN_USERDATA_LASTNAME	+"," +
				COLUMN_USERDATA_HOME 		+ ") VALUES(" +
				"null"			+",\"" +	/*ID is auto increment, so we dont care*/
				user.username		+"\",\"" +
				user.email			+"\",\"" +
				user.l1group		+"\",\"" +
				user.l2group		+"\",\"" +
				user.l3group		+"\",\"" +
				user.firstname		+"\",\"" +
				user.lastname		+"\",\"" +
				user.home			+"\");";
		statement.execute(query);
		statement.close();
		return false;

	}

	public synchronized String[] getSaltAndHash(String username) throws Exception{
		// Return the Hash/ Salt pair of a given username
		// Return null if user does not exist
		// String[0] = salt
		// String[1] = hash
		statement = connection.createStatement();
		String query = "SELECT "		+
				COLUMN_USERPASS_SALT		+" , "	+
				COLUMN_USERPASS_HASH		+" FROM "	+
				TABLE_USERPASS	+ " WHERE " +
				COLUMN_USERPASS_USERNAME + "= \""+
				username		+"\";";
		ResultSet result = statement.executeQuery(query);
		if(result!=null && result.next()!=false){
			String hash = result.getString(COLUMN_USERPASS_HASH);
			String salt = result.getString(COLUMN_USERPASS_SALT);
			statement.close();
			return new String[]{salt,hash};
		}
		else{
			// User Does Not Exist
			// return a null array
			statement.close();
			System.out.println("Username DNE");
			return null;

		}
	}

	public synchronized boolean addInstitute(String instituteName, String domain, String dataFilePath) throws Exception{
		// Add Institute name and Instite domain to database
		statement = connection.createStatement();

		// First check if the institute is already in the database
		String query = "SELECT "		+
				COLUMN_INSTITUTE_ID		+" FROM "+
				TABLE_INSTITUTE	+ " WHERE " +
				COLUMN_INSTITUTE_DOMAIN + "= \"" +
				domain		+"\";";
		ResultSet result = statement.executeQuery(query);

		if(result!=null && result.next()!=false){
			// Domain name data already in table (i.e already registered institute)
			// INstitute is not unique
			return false;
		}
		else{
			// Institute not in table
			query = "INSERT INTO "						+
					TABLE_INSTITUTE					+"(" +
					COLUMN_INSTITUTE_ID				+"," +
					COLUMN_INSTITUTE_INSTITUTENAME	+"," +
					COLUMN_INSTITUTE_DATA_FILE_PATH	+"," +
					COLUMN_INSTITUTE_DOMAIN 	+ ") VALUES(" +
					"null"			+",\"" +					/*ID is auto increment, so we dont care*/
					instituteName		+"\",\"" +
					dataFilePath		+"\",\"" +
					domain			+"\");";
			statement.execute(query);
			statement.close();

			return true;
		}
	}

	public synchronized String getInstituteName(String domain) throws Exception{
		// Return the Hash/ Salt pair of a given username
		// Return null if user does not exist
		// String[0] = salt
		// String[1] = hash
		statement = connection.createStatement();
		String query = "SELECT "						+
				COLUMN_INSTITUTE_INSTITUTENAME		+" FROM "	+
				TABLE_INSTITUTE						+ " WHERE " +
				COLUMN_INSTITUTE_DOMAIN 				+ "= \""+
				domain									+"\";";

		ResultSet result = statement.executeQuery(query);
		if(result!=null && result.next()!=false){
			String institutename = result.getString(COLUMN_INSTITUTE_INSTITUTENAME);
			statement.close();
			return institutename;
		}
		else{
			// User Does Not Exist
			// return a null array
			statement.close();
			System.out.println("Username DNE");
			return null;

		}
	}
}
