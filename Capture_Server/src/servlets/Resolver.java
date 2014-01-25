package servlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.ServerResponse;
import models.User;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;





//import net.sf.json.JSONObject;
import tools.Crypto;
import dataStore.DatabaseHelper;
import dataStore.Globals;
import dataStore.ServerInternalData;

@WebServlet(value="/Resolver", asyncSupported = true)
public class Resolver extends HttpServlet {
	// Responsible for accepting the initial client request and assigning a websocket servlet for
	// Future communications. 
	// Checks for loads of each servlet, data distribution and other stuff before making decision

	private static final long serialVersionUID = 1L;
	ServerInternalData serverinternaldata;
	Crypto crypto;
	DatabaseHelper dbHelper;

	public Resolver() {
		super();
		//Servlet specific initializations
		if(!Globals.DEBUG) System.out.println("DEBUG FLAG IS TURNED OFF. WARNINGS SUPPRESSED!");
		if(!Globals.LOUD) System.out.println("LOUD FLAG IS TURNED OFF. NO INFORMATION WILL BE PRINTED");

		initdata();			// initialize data
		initCrypt();		// initialize hashing related classes
		initDB();			// initialize database helper and create/initilaze/open database
		initAccounts();		// Create initial accounts such as admin accounts

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		/*
		 * We are not using httpGet anymore
		 * ServerResponse serverresponse = new ServerResponse(); // Collect status of the previous helper function //TODO recycle serverresponse object
		 * Convert the request parameters to a map so its easier for the back end
		 * Map<String, String[]> parameters =request.getParameterMap();
		 * Match request type
		 * if(parameters==null || !parameters.containsKey("requesttype"))
		 * 
		 * "http://localhost:8080/Capture_Server/Resolver?requesttype=admin&adminname=malithmp&password=meh&request=tempinit"
		 */

		// Just to remind anyone that HTTPGET is history
		ServerResponse serverresponse = new ServerResponse();
		serverresponse.status=false;
		serverresponse.message="Protocol Error: Incorrect Input!";
		sendResponse(response,serverresponse,null);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//------// Check if the request is for user connection/ debugger/admin request or General viewer request
		//------// If user connection (a user needs to play the game)
		//------// 		Authenticate and send back a token
		//------// 		Get Location and Arena requested by the user
		//------// 		Verify if the request can be completed (Current Location is too far away from the arena.. user is out of range)
		//------// 		If the above passes, Query all websocket servlets to see which one is serving that arena (may be done at a different stage: ie at startup of server.. or during operation)
		//------// 		Send the client with the specific URI to that servlet and send the servlet the same token that user got when they logged in
		//------// If debugger request. All handled by this servlet. no websocket gimics 
		//------//		Authenticate!!
		//------//		Query servlets according to request and send info back
		//------// If General Viewer Request TODO: Websocket of Regular?
		//------//		Check if current details are expired. 
		//------//		If expired Query all map info (limited detail), save the current info and send them to requester
		//------//		If data is relativly new, send it to the requester
		//------// If Admin request
		//------//		Authenticate!!!
		//------//		Run command straight away.

		if(Globals.LOUD) System.out.println("Thread: "+Thread.currentThread().getId());
		ServerResponse serverresponse = new ServerResponse();	// Collect status of the previous helper function //TODO recycle serverresponse object
		// If admin request, AUTHENTICATE!!! TODO
		// If inter-servlet communication AUTHENTICATE!! TODO
		// If user communication AUTHENTICATE TODO

		// Read data from the post request
		String inputData="";
		BufferedReader bufferedReader;

		try{
			// Read data from the post
			bufferedReader = request.getReader();
			inputData=bufferedReader.readLine();
		}catch(Exception e){
			// The above 2 lines are crucial for the rest of this function., Failure of any should be caught by this point and we should not (and cannot) continue
			serverresponse.status=false;
			serverresponse.message="No data received";
			sendResponse(response,serverresponse,null);
			return;
		}

		JSONObject jObj = (JSONObject) JSONValue.parse(inputData);
		if(jObj==null || jObj.isEmpty()){
			// invalid request. Does not adhere to protocol. kindly ask them to GTFO!
			if(Globals.DEBUG) System.out.println("ERROR: Parameters not provided!");
			serverresponse.status=false;
			serverresponse.message="Protocol Error: no parameters provided!";
			sendResponse(response,serverresponse,null);
			return;
		}
		else if(!jObj.containsKey("requesttype")){
			// invalid request. Does not adhere to protocol. kindly ask them to GTFO!
			if(Globals.DEBUG) System.out.println("ERROR: Invalid Request!");
			serverresponse.status=false;
			serverresponse.message="Protocol Error. Invalid Request!";
			sendResponse(response,serverresponse,null);
			return;
		}

		if(((String)jObj.get("requesttype")).equals("user")){
			handleUserPost(jObj, response, serverresponse);
		}

		else if(((String)jObj.get("requesttype")).equals("admin")){
			handleAdminPost(jObj, response, serverresponse);
		}

		else if(((String)jObj.get("requesttype")).equals("general")){
			// A general user who wants to view the game. (not necessarily a signed up user) 
			handleGeneralViewerPost(null,response);
			if(Globals.DEBUG) System.out.println("WARNING: GENERAL MODE NOT IMPLEMENTED!");
		}

		else if(((String)jObj.get("requesttype")).equals("debug")){
			// A debugger connection. // This must be authenticated as it has access to basically everything!!! 
			handleDebuggerPost(null,response);
			if(Globals.DEBUG) System.out.println("WARNING: DEBUG MODE NOT IMPLEMENTED!");
		}

		else{
			if(Globals.DEBUG) System.out.println("CRITICAL ERROR: HTTPPOST request type mismatch: provided :"+(String)jObj.get("requesttype"));
			return;
		}

		return;
	}
	//---------------INIT-----------------
	//-------OPTIONALLY THREAD SAFE-------

	private void initdata(){
		if(Globals.DEBUG) System.out.println("INFO: Initializing data...");
		serverinternaldata = new ServerInternalData();	
	}

	private void initCrypt(){
		if(Globals.DEBUG) System.out.println("INFO: Initializing crypto...");
		try {
			crypto = new Crypto();
		} catch (NoSuchAlgorithmException e) {
			if(Globals.DEBUG) System.out.println("CRITICAL ERROR: Hashing algorithms failed to load. Its not safe here");
			e.printStackTrace();
			return;
		}
	}

	private void initDB(){
		if(Globals.DEBUG) System.out.println("INFO: Initializing database...");
		dbHelper = new DatabaseHelper();
		try {
			dbHelper.initDatabase();
		} catch(Exception e) {
			if(Globals.DEBUG) System.out.println("CRITICAL ERROR: Database failed to load. Abandon ship!");
			e.printStackTrace();
			return;
		}
	}

	private void initAccounts(){
		// Create the very first admin with the following credentials
		// Current admin username: th3_0n3
		// current admin password: BananasAreG00d
		// TODO: REMOVE THE FIRST ADMIN WITH THE REAL ONES [ adminName-> (salt, hash)]
		serverinternaldata.modifyAdminCredentials("th3_0n3", "36d5c61af959e7e38bb69e1cc297b63028c5c257a5db25d60c8bff90442bf631",
				"6dcf27c9d74d6b82a898dd803559be59a3bf34ec8bb7d9ec479d2e8290279c624dd4c3a2a4d589c56da8aa8bdaee4c1373db7397d23c7b7fafc8bfa0abe8e126", 1);
		if(Globals.DEBUG) System.out.println("WARNING: Creating First Admin...");
	}
	//-------------END INIT---------------


	// ------------------------------- HELPER FUNCTIONS -------------------------------------------

	public void handleDebuggerPost(JSONObject jObj, HttpServletResponse response){// Parameters passed by the HTTP GET
		// Authenticate!!
		// Query servlets according to request and send info back
		if(Globals.LOUD) System.out.println("Debug Mode");
		try{
			if(Globals.LOUD) System.out.println("dVal");
		}catch(Exception e){
			if(Globals.LOUD) System.out.println("Debugmode exception caught");
		}
	}


	public boolean handleAdminPost(JSONObject jObj,  HttpServletResponse response, ServerResponse serverresponse){
		//TODO Authenticate!!
		if(Globals.DEBUG) System.out.println("INFO:adminname:"+(String)jObj.get("adminname")+" token:" + (String)jObj.get("token"));

		if(((String)jObj.get("loggedin")).equals("true")){
			if(!serverinternaldata.isAdminValid((String)jObj.get("adminname"), (String)jObj.get("token"))){
				// Admin request with invalid credentials
				if(Globals.DEBUG)System.out.println("WARNING: ADMIN REQUETS WITH INVALID CREDENTIALS");
				serverresponse.status=false;
				serverresponse.message="Illegal request  o8e45";
				sendResponse(response, serverresponse,null);
				return false;
			}

			if(Globals.DEBUG) System.out.println("INFO:Admin Mode: Welcome my Lords!");
			if(((String)jObj.get("request")).equals("registerinstitute")){
				// Map institute email domain to the institute name and save the map data (boundary) to a file
				boolean registered = registerInstitute(((String)jObj.get("institutename")), ((String)jObj.get("institutedomain")), ((String)jObj.get("rawdata")), serverresponse);
				if(registered){
					sendResponse(response, serverresponse,new String[]{"message","Institure registered successfully"});
					return true;
				}else{
					sendResponse(response,serverresponse,null);
					return false;
				}
			}
			else if(((String)jObj.get("request")).equals("registerservlet")){
				// Read the servlet URL and add it to the serverinternaldata data structure
				boolean status = serverinternaldata.registerNewServlet((String)jObj.get("url"),serverresponse);
				if(status){ // Let the admin know everything went well
					if(Globals.LOUD) System.out.println("Servlet Added");
					serverresponse.status=true;
					sendResponse(response, serverresponse,new String[]{"message","Servlet Registered"});
					return true;
				}
				else{
					// Error message set inside serverinternaldata.registerNewServlet function
					sendResponse(response, serverresponse,null);
					return false;
				}
			}
			else if(((String)jObj.get("request")).equals("registerarena")){
				//Arena names are added at the time it was uploaded to the server (using a POST request)
				// TODO Makesure there is a way to map this name to the actual arena data.
				if(Globals.DEBUG) System.out.println("WARNING:sendMapToServlet() must be called before this!");
				if(Globals.DEBUG) System.out.println("WARNING:Assined map must be sent to the arena");
				if(Globals.DEBUG) System.out.println((String)jObj.get("arena"));
				boolean status = serverinternaldata.registerNewArena((String)jObj.get("arena"), serverresponse);

				if(status){ // Let the admin know everything went well
					if(Globals.LOUD) System.out.println("Added Arena");
					sendResponse(response,serverresponse,new String[]{"message","Arena Registered"});
					return true;
				}
				else{
					sendResponse(response,serverresponse,null);
					return false;
				}
			}
			else if(((String)jObj.get("request")).equals("maparenaservlet")){
				if(Globals.DEBUG) System.out.println(((String)jObj.get("arena"))+"--"+ ((String)jObj.get("servlet")));
				boolean status = serverinternaldata.mapArenaServlet(((String)jObj.get("arena")), ((String)jObj.get("servlet")), serverresponse);
				if(status){ // Let the admin know everything went well
					sendResponse(response,serverresponse,new String[]{"message","Arena Servlet mapped"});
					return true;
				}
				else{
					sendResponse(response,serverresponse,null);
					return false;
				}
			}
			else if(((String)jObj.get("request")).equals("addadmin")){
				// Add another admin
				String adminname = (String)jObj.get("adminname");
				String password = (String)jObj.get("password");
				addAdmin(adminname, password, response, serverresponse);
				return true;
			}
			
			else if(((String)jObj.get("request")).equals("tempinit")){
				// This is a temporary function.. remove once done
				boolean status = tempinit(response, serverresponse);
				return false;
			}
			else if(((String)jObj.get("request")).equals("tempgetdbpath")){
				// This is a temporary function.. remove once done
				File f = new File("a.a");
				sendResponse(response,serverresponse,new String[]{"message","Database Path: "+f.getAbsolutePath()});
				return false;
			}
			else{
				serverresponse.status=false;
				serverresponse.message="Invalid Admin Post Request!";
				sendResponse(response,serverresponse, null);
				return false;
			}
		}
		else if (((String)jObj.get("loggedin")).equals("false")){
			// TODO Admin authentication!!!
			if(((String)jObj.get("request")).equals("signin")){
				authenticateAdmin((String)jObj.get("adminname"),(String)jObj.get("password"), response, serverresponse);
			}else{
				serverresponse.status=false;
				serverresponse.message="Invalid Admin Post Request!";
				sendResponse(response,serverresponse, null);
			}
			return false;
		}
		else{
			serverresponse.status=false;
			serverresponse.message="Invalid Admin Post Request!";
			sendResponse(response,serverresponse, null);
			return false;
		}


	}


	public boolean handleUserPost(JSONObject jObj, HttpServletResponse response , ServerResponse serverresponse){
		// 		Read "loggedin" info. if false, either expect a signin or signup
		//		If true, Check against databases to see if this guy is legit
		//		Create access token for future communications. Let the other servelet instances know of this token. Set expiration time? TODO: expiriation time for token

		//		If its a joinmap request
		// 		Get Location and Arena requested by the user
		// 		Verify if the request can be completed (Current Location is too far away from the arena.. user is out of range)
		// 		If the above passes, Query all websocket servlets to see which one is serving that arena (may be done at a different stage: ie at startup of server.. or during operation)

		// 		Send the client with the specific URI to that servlet and send the servlet the same token that user got when they logged in
		if(Globals.DEBUG) System.out.println("AUTHENTICATE USER POST!");

		if(((String)jObj.get("loggedin")).equals("true")){
			// Most frequent option. Logged in user is requesting for an arena to play in
			// TODO
			// Check if the token is not expired, 
			// if it is, take user through the authentication process
			if(Globals.DEBUG) System.out.println("WARNING: logged in true : NOT IMPLEMENTED");
			if(!serverinternaldata.isUserValid((String)jObj.get("username"), (String)jObj.get("token"))){
				// Admin request with invalid credentials
				if(Globals.DEBUG)System.out.println("WARNING: USER REQUETS WITH INVALID CREDENTIALS");
				serverresponse.status=false;
				serverresponse.message="Illegal request  o8e46";
				sendResponse(response, serverresponse,null);
				return false;
			}
			return false;//TODO
		}

		else if(((String)jObj.get("loggedin")).equals("false")){
			// only allowed to signin or signup

			if(((String)jObj.get("request")).equals("signup")){
				// User needs to signup
				// User sends the data packaged in a user object json string
				if(Globals.DEBUG) System.out.println("UserPost:"+(String)jObj.get("rawdata"));
				usersignup((String)jObj.get("rawdata"),response,serverresponse);
				return false;
			}
			else if(((String)jObj.get("loggedin")).equals("signin")){
				// Get the username and password, call the authenticate function to verify the username/password
				boolean authenticationStatus = authenticateUser((String)jObj.get("username"),(String)jObj.get("password"),response,serverresponse);
				//TODO what to do with the authenticationStatus ?
				return true;
			}
			else{
				serverresponse.status=false;
				serverresponse.message="Illegal POST request for loggedin=false";
				sendResponse(response,serverresponse, null);
				return false;
			}
		}
		else{
			serverresponse.status=false;
			serverresponse.message="Illegal POST request";
			sendResponse(response,serverresponse, null);
			return false;
		}

	}


	public void handleGeneralViewerPost(JSONObject jObj, HttpServletResponse response){
		// Resolve the request.. What are they asking for?
		// TODO: if we are planning on using websockets, 
		// 		 Do websockety stuff
		// TODO: if we are sending data directly from here
		// 	 	 Check the time and see if the localmax size of http post  data (for that query) is expired
		// 		 If data is not expired, send it to them
		if(Globals.DEBUG) System.out.println("WARNING: NOT IMPLEMENTED!");
	}



	// ------------------------------- actual helper functions--------------------------------------------------

	private boolean registerInstitute(String instituteName, String instituteDomain, String rawData, ServerResponse serverresponse){
		if(rawData==null){				// little sanity test
			serverresponse.status=false;
			serverresponse.message="Input Data null, Sanity failed";
			return false;
		}
		String fname = Globals.FILE_DIR+instituteName+".map";
		if(Globals.DEBUG) System.out.println("INFO: Creating file"+fname);	
		if(Globals.DEBUG) System.out.println("INFO: Got = "+rawData);	

		try{
			File file = new File(fname);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				if(Globals.DEBUG) System.out.println("INFO: Creating file: "+fname);	
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			// in line #1 of file, we store the insitute name
			// in line #2 of file, we store the institute domain
			// the rest of the file contains the json string that holds the coordinates of the boundary (as an array of coordinates in order)
			bw.write(instituteName);	// line #1 of = the insitute name
			bw.write("\n");
			bw.write(instituteDomain);		// line #1 of = the insitute domain
			bw.write("\n");
			bw.write(rawData);
			bw.close();

			// Add these to the database
			dbHelper.addInstitute(instituteName, instituteDomain, fname);

		}catch(Exception e){
			serverresponse.status=false;
			serverresponse.message="Could not write to file";
			return false;
		}
		return true;
	}


	private boolean authenticateUser(String username, String password, HttpServletResponse response, ServerResponse serverresponse){
		// do syncronized accesses to the database and crypto and verify authenticity  
		// TODO DBhelper and crypto already provide synchronized access (due to either lack of thread safety in those libraries or to preserve ram usage)
		// Look into this!!
		// Called by all usermodes, admin/user via Server Internal function calls. No outer entity has direct access to this function

		// Get the Hash and the salt from the server for the user
		String[] saltHash = null;
		try {
			saltHash = dbHelper.getSaltAndHash(username,serverresponse);
			if (saltHash == null){
				serverresponse.status=false;
				serverresponse.message="Authentication Failed";				// Hiding real reason of failure since it can expose a vulnerability
				if(Globals.LOUD) System.out.println("INFO: AUTH FAIL");
				sendResponse(response,serverresponse,null);	
				return false;
			}

			String hash = crypto.getHash(password, saltHash[0]);			// Append the salt and hash the password
			if (!hash.equals(saltHash[1])){									// Check generated hash against the local hash (one that was generated at the time user logged in!) //TODO IMPLEMENT CAHNGE PASSWORD FEATURE?
				// Hashes didnt match. Auth fail
				// Username and passwords dont match
				if(Globals.LOUD) System.out.println("INFO: AUTH FAIL");
				serverresponse.status=false;
				serverresponse.message="Authentication Failed";				// Hiding real reason of failure since it can expose a vulnerability
				sendResponse(response,serverresponse,null);
				return false;
			}

		} catch (Exception e) {
			serverresponse.status=false;
			serverresponse.message="Exception in DBHelper";
			if(Globals.DEBUG) System.out.println("CRITICAL ERROR: Authentication Procedure Caused an exception. Potential Crypto or DB issue");
			e.printStackTrace();
			return false;
		}

		// If we made it to this part, Auth failed and nothing crashed :)
		// auth succeeded. Generate an access token

		if(Globals.LOUD) System.out.println("INFO: AUTH PASS");
		if(Globals.DEBUG) System.out.println("WARNING: Access token is a randomg string, Implement something better");
		String token = crypto.getSalt(32);

		// Keep track of the access token until the user logs out
		boolean status = serverinternaldata.addLiveUser(username,token, serverresponse);

		// Send the access token to the user.
		if(status==true){
			// Return the access token 
			sendResponse(response,serverresponse,new String[]{"token",token});
			return true;
		}
		else{
			serverresponse.status=false;
			serverresponse.message="wut";
			sendResponse(response,serverresponse,null);
			//TODO So far there is no way this could happen. But check whenever serverinternaldata.addActiveUser is updated
			return false;
		}
	}


	private boolean authenticateAdmin(String adminname, String password, HttpServletResponse response, ServerResponse serverresponse){
		String[] cred = serverinternaldata.getAdminCredentials(adminname);
		boolean status = false;
		try{
			String hash = crypto.getHash(password,cred[0]);
			if(hash.equals(cred[1])){
				// hashes matched
				status=true;
				String token = crypto.getSalt(32);		//acquire token
				// Keep track of the access token until the admin logs out
				serverinternaldata.addLiveAdmin(adminname,token, serverresponse);
				sendResponse(response, serverresponse, new String[]{"token",token});
			}
			else{
				serverresponse.status=false;
				serverresponse.message="Admin Authentication Failed";				// Hiding real reason of failure since it can expose a vulnerability
				sendResponse(response,serverresponse,null);
				if(Globals.LOUD) System.out.println("INFO: ADMIN AUTH FAIL");
			}
		}catch(Exception e){
			serverresponse.status=false;
			serverresponse.message="Admin Authentication Exception";				// Hiding real reason of failure since it can expose a vulnerability
			sendResponse(response,serverresponse,null);
			if(Globals.LOUD) System.out.println("INFO: ADMIN AUTH Exception");
		}

		return status;
	}


	private boolean addAdmin(String adminname, String password, HttpServletResponse response, ServerResponse serverresponse){
		String salt = null;
		String hash = null;
		if(serverinternaldata.getAdminCredentials(adminname) != null){
			// there is already an entry for this adminname
			serverresponse.status=false;
			serverresponse.message="Admin exists for this username";
			sendResponse(response, serverresponse, null);
			return false;
		}

		try{
			salt = crypto.getSalt(32);
		}catch(Exception e){
			serverresponse.status=false;
			serverresponse.message="Crypto Exception";
			sendResponse(response, serverresponse, null);
		}
		
		if(salt==null || hash==null){
			serverresponse.status=false;
			serverresponse.message="Error in crypto";
			sendResponse(response, serverresponse, null);
			return false;
		}

		serverinternaldata.modifyAdminCredentials(adminname, salt, hash, 1);
		serverresponse.status=true;
		sendResponse(response, serverresponse, new String[]{"message","Admin Added"});
		
		return false;

	}

	
	private boolean usersignup(String inputData,HttpServletResponse response, ServerResponse serverresponse){

		JSONObject jObj = (JSONObject) JSONValue.parse(inputData);
		// TODO makesure DBHelper does everything in a threadsafe way
		// First we Hash the password and prepare everything needed to add a username to the userpass table
		String salt = crypto.getSalt(32);
		String hash = crypto.getHash((String)jObj.get("password"), salt);
		String username = (String)jObj.get("username");

		// Second we add the user to the userpass table. This ensures we have a unique username. If this fails we let the user know it failed
		try {
			boolean added = dbHelper.acquireUsername(username,hash,salt,serverresponse);
			if(added==false){
				sendResponse(response,serverresponse,null);
				return false;
			}

			// assign teams
			// Use the email address to identify the institute
			String email = (String)jObj.get("email");
			// We assume that the email address sent is of the correct format. We have to trust the app to do the validataion
			// We know that the email address is of this form by now <username>@<domain>
			StringTokenizer stokenzr = new StringTokenizer(email, "@");
			String domain = stokenzr.nextToken();		// eatup the username part
			domain = stokenzr.nextToken();			// get the domain part

			String l2 = null;
			l2 = dbHelper.getInstituteName(domain,serverresponse);
			if (l2 == null){
				// institute was not found in the database.
				sendResponse(response,serverresponse,null);
				return false;
			}

			if(Globals.DEBUG) System.out.println("UserTeam:"+l2);
			User user = new User((String)jObj.get("username"), 
					email, 
					-1, 
					"l2", 
					"l3", 	
					(String)jObj.get("firstname"), 
					(String)jObj.get("lastname"), 
					(String)jObj.get("home"));

			// We have an L2 team. Now get an L1 Team for that L2 team
			int l1 = serverinternaldata.getSpot(domain);	// use the domain name to get a L1 team
			if(l1<0){
				// Ran out of spots. We have to check if we still can register more users // TODO: probably we dont need this
				serverinternaldata.reloadSpots(domain, 100, 100);
				l1 = serverinternaldata.getSpot(domain);	// now that its reloaded. get the L1 team
			}

			//TODO IMPLEMENT L3 TEAM LOGIC HERE!
			user.l1group = l1;
			user.l2group = l2;

			// WE are all set. Store this data on the database and we have ourselves a legit registered user
			dbHelper.addUser(user);
			// give user the good news
			sendResponse(response,serverresponse,new String[]{"l1grp",Integer.toString(user.l1group),"l2grp",user.l2group});
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			serverresponse.status=false;
			serverresponse.message="DB crashed";
			sendResponse(response,serverresponse, null);
			return false;
		}
	}


	public boolean sendMapToServlet(String targetURL,Object mapOfArena){
		// This must be implemented once everything is done. 
		// This will send map data to the servlet
		// Websocket Servlets can accept http POST requests to we can send the map to them using HTTP 
		// Then once this is done, we know that the servlet is capable of serving the arena 
		if(Globals.DEBUG) System.out.println("WARNING: sendMapToServlet NOT IMPLEMENTED");
		if(Globals.DEBUG) System.out.println("Dummy function pretends to send the file to the websocket sevlet, but in reality its already there!");
		if(Globals.DEBUG) System.out.println("INFO: WS Servlets must only get their map data from this method!");
		return true;
	}


	public void sendResponse(HttpServletResponse response,ServerResponse serverresponse, String[] extra){
		// this guy sends all the responses generated by the helper functions
		// extra is a 1D string with key-value pairs on i and i+1 object
		StringBuilder sb = new StringBuilder();
		sb.append("\"status\":");
		if(!serverresponse.status){				// for this case, we dont care about the content of extra
			sb.append("\"false\",");
			sb.append("\"message\":");
			sb.append("\"");
			sb.append(serverresponse.message);
			sb.append("\"");
		}else{									// for this case, we dont care about the content of serverresponse.message
			sb.append("\"true\"");
			for(int i=0;i<extra.length;i++){
				sb.append(",\"");
				sb.append(extra[i++]);
				sb.append("\":\"");
				sb.append(extra[i]);
				sb.append("\"");
			}
		}

		try {
			PrintWriter pw = response.getWriter();
			pw.println(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public boolean tempinit(HttpServletResponse response, ServerResponse serverresponse){
		//TODO This should be removed immediately after the development phase

		boolean status = true;
		//TODO debug call to initialize dummy maps and arenas
		// this will bypass most of the hassles during the starting the server
		if(Globals.DEBUG) System.out.println("WARNING!: DEBUG ONLY FUNCTION CALLED!: tempInit");
		// Assume websocket servlet is already up and runnint
		String websocketUrl = "http://localhost:8080/Capture_Server/WS";
		if(Globals.DEBUG) System.out.println("\tWARNING: Assuming webosocket servlet is up and the url is: "+websocketUrl);

		// Assume the map has been transfered to resolver temporary map area
		if(Globals.DEBUG) System.out.println("\tWARNING:<<<<<<<<<<<<<<<<<<<<<<<<00000sadasjhasshdksadkjas k");
		// Send that to the websocket servlet


		String arenaname = "home";
		System.out.println("\tWARNING: Assuming websocket servlet has the arena/map preloaded and has the name: "+arenaname);
		// Register the arena (in this servlet)
		status &= serverinternaldata.registerNewArena(arenaname, serverresponse);
		System.out.println("\tWARNING:registerNewArena:"+status);
		// Register the websocket servlet
		status &= serverinternaldata.registerNewServlet(websocketUrl, serverresponse);
		System.out.println("\tWARNING:registerNewServlet:"+status);
		// Map the arena to the servlet
		status &= serverinternaldata.mapArenaServlet(arenaname, websocketUrl, serverresponse);
		System.out.println("\tWARNING:mapArenaServlet:"+status);

		// dummy add users and admins to database
		System.out.println("WARNING: Adding dummy passwords to table. Remove immediately!");//TODO
		try {
			// create a salt
			// use that salt and the password to build the hash, then put that hash and the salt to the table
			String salt=crypto.getSalt(32);
			dbHelper.acquireUsername("usermalithr", crypto.getHash("pass1",salt), salt, serverresponse);
			salt=crypto.getSalt(32);
			dbHelper.acquireUsername("usernumalj", crypto.getHash("pass2",salt), salt, serverresponse);
			salt=crypto.getSalt(32);
			dbHelper.acquireUsername("userharithay", crypto.getHash("pass3",salt), salt, serverresponse);

			dbHelper.addInstitute("UofT","utoronto.ca","./UofT.map");
			dbHelper.addInstitute("Ryerson","ryerson.ca","./Ryerson.map");
			dbHelper.addInstitute("York","yorku.ca","./York.map");
			dbHelper.addInstitute("Waterloo","uwaterloo.ca","./Waterloo.map");

			sendResponse(response,serverresponse, new String[]{"message","TempInit Called"});
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("WARNING: Adding dummy institute email domain mappings");
		return status;
	}
}