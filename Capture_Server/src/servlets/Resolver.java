package servlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


//import net.sf.json.JSONObject;
import tools.Crypto;
import dataStore.DatabaseHelper;
import dataStore.Globals;
import dataStore.ServerInternalData;
import dataStore.ServerResponse;
import dataStore.User;
// Handles the http based request of the server

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
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		ServerResponse serverresponse = new ServerResponse(); // Collect status of the previous helper function //TODO recycle serverresponse object
		// Check if the request is for user connection/ debugger/admin request or General viewer request
		// If user connection (a user needs to play the game)serverinternaldparameters.get("URL")[0]ata
		// 		Authenticate and send back a token
		// 		Get Location and Arena requested by the user
		// 		Verify if the request can be completed (Current Location is too far away from the arena.. user is out of range)
		// 		If the above passes, Query all websocket servlets to see which one is serving that arena (may be done at a different stage: ie at startup of server.. or during operation)
		// 		Send the client with the specific URI to that servlet and send the servlet the same token that user got when they logged in
		// If debugger request. All handled by this servlet. no websocket gimics 
		//		Authenticate!!
		//		Query servlets according to request and send info back
		// If General Viewer Request TODO: Websocket of Regular?
		//		Check if current details are expired. 
		//		If expired Query all map info (limited detail), save the current info and send them to requester
		//		If data is relativly new, send it to the requester
		// If Admin request
		//		Authenticate!!!
		//		Run command straight away.

		// Convert the request parameters to a map so its easier for the back end
		Map<String, String[]> parameters =request.getParameterMap();
		if(Globals.LOUD) System.out.println("Thread: "+Thread.currentThread().getId());
		//Match request type
		if(parameters==null || !parameters.containsKey("requesttype")){
			// invalid request. Does not adhere to protocol. kindly ask them to GTFO!
			if(parameters==null){
				serverresponse.status=false;
				serverresponse.message="Protocol Error: no parameters provided!";
				sendResponse(response,serverresponse,null);
			}
			else{
				if(Globals.DEBUG) System.out.println("ERROR: Requesttype not provided!");
				serverresponse.status=false;
				serverresponse.message="Protocol Error: requesttype parameter not specified!";
				sendResponse(response,serverresponse,null);
			}	
		}
		// So far so good. Depending on the user  type we can now call the helper functions.
		else if(parameters.get("requesttype")[0].equals("user")){
			// A user who wants to play. 
			// We put this option first since its the most likely request to happen
			//TODO
			if(Globals.DEBUG) System.out.println("WARNING: parameters.get user NOT IMPLEMENTED!");
			handleUserGet(parameters,response,serverresponse);
			//System.out.println(parameters.get("c")[1]);
			//System.out.println(parameters.get("c")[0]);
		}
		else if(parameters.get("requesttype")[0].equals("general")){
			// A general user who wants to view the game. (not necessarily a signed up user) 
			// We put this option second since its the second most likely request to happen
			handleGeneralViewerGet(parameters,response);
		}
		else if(parameters.get("requesttype")[0].equals("debug")){
			// A debugger connection. // This must be authenticated as it has access to basically everything!!! 
			// We put this option 3rd since its much less likely request to happen

			//TODO:: AUTHENTICATE THIS SHIT!
			handleDebuggerGet(parameters,response);
			if(Globals.DEBUG) System.out.println("WARNING: DEBUG MODE NOT IMPLEMENTED!");
		}
		else if(parameters.get("requesttype")[0].equals("admin")){
			// An admin connection. // This too must be authenticated as it has access to basically everything!!! 
			// We put this option last since its the least likely request to happen

			//TODO:: AUTHENTICATE THIS SHIT!

			if(Globals.DEBUG) System.out.println("WARNING: ADMIN MODE NOT IMPLEMENTED!");
			handleAdminGet(parameters,response,serverresponse);
		}
		else{
			// Again. invalid request. kindly ask them to GTFO!
			if(Globals.LOUD) System.out.println("Invalid Protocol Request Detected!"+parameters.get("requesttype"));
			serverresponse.status=false;
			serverresponse.message="Protocol Error: invalid request: "+parameters.get("requesttype")+" !";
			sendResponse(response,serverresponse,null);
		} 
		// response.setContentType("text/html");
		// System.out.println("Got:"+request.getQueryString()+"::"+request.getParameter("cat")+"::"+request.getParameter("bleh"));
		// PrintWriter pw = response.getWriter();
		// pw.println("<h1>Hello World "+Thread.currentThread().getId()+"</h1>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServerResponse serverresponse = new ServerResponse();	// Collect status of the previous helper function //TODO recycle serverresponse object
		// Must only be handling inter-servlet or admin data
		// Admins may manually upload arenas/maps
		// Servlets may pass arenas/maps within themselves for load balancing purposes

		// If admin request, AUTHENTICATE!!! TODO
		// If inter-servlet communication AUTHENTICATE!! TODO
		// Convert the request parameters to a map so its easier for the back end
		//System.out.println("post got from"+ parameters.get("requester"));
		Map<String, String[]> parameters =request.getParameterMap();
		if(parameters==null || !parameters.containsKey("requesttype")){
			// invalid request. Does not adhere to protocol. kindly ask them to GTFO!
			if(parameters==null){
				if(Globals.DEBUG) System.out.println("ERROR: Parameters not provided!");
				serverresponse.status=false;
				serverresponse.message="Protocol Error: no parameters provided!";
				sendResponse(response,serverresponse,null);
			}
			else{
				if(Globals.DEBUG) System.out.println("ERROR: Requesttype not provided!");
				serverresponse.status=false;
				serverresponse.message="Protocol Error: requesttype parameter not specified!";
				sendResponse(response,serverresponse,null);
			}
		}
		else if(parameters.get("requesttype")[0].equals("admin")){
			handleAdminPost(parameters, request, response, serverresponse);
		}
		else if(parameters.get("requesttype")[0].equals("servlet")){
			if(Globals.DEBUG) System.out.println("WARNING: POST SERVLET NOT IMPLEMENTED!");

		}
		else if(parameters.get("requesttype")[0].equals("websocket")){
			if(Globals.DEBUG) System.out.println("WARNING: POST WEBSOCKET NOT IMPLEMENTED!");

		}
		else if(parameters.get("requesttype")[0].equals("user")){
			handleUserPost(parameters, request, response, serverresponse);
		}
		else{
			if(Globals.DEBUG) System.out.println("CRITICAL ERROR: HTTPPOST request type mismatch: provided :"+parameters.get("requesttype")[0]);
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

	//-------------END INIT---------------


	// ------------------------------- HELPER FUNCTIONS -------------------------------------------

	public void handleUserGet(Map<String, String[]> parameters, HttpServletResponse response, ServerResponse serverresponse){// Parameters passed by the HTTP GET
		// 		Read "loggedin" info. if false, either expect a signin or signup
		//		If true, Check against databases to see if this guy is legit
		//		Create access token for future communications. Let the other servelet instances know of this token. Set expiration time? TODO: expiriation time for token

		//		If its a joinmap request
		// 		Get Location and Arena requested by the user
		// 		Verify if the request can be completed (Current Location is too far away from the arena.. user is out of range)
		// 		If the above passes, Query all websocket servlets to see which one is serving that arena (may be done at a different stage: ie at startup of server.. or during operation)

		// 		Send the client with the specific URI to that servlet and send the servlet the same token that user got when they logged in

		if(parameters.get("loggedin")[0].equals("true")){
			// Most frequent option. Logged in user is requesting for an arena to play in
			// TODO
			// Check if the token is not expired, 
			// if it is, take user through the authentication process
			if(Globals.DEBUG) System.out.println("WARNING: logged in true : NOT IMPLEMENTED");
		}
		else if (parameters.get("loggedin")[0].equals("false")){
			// only allowed to signin or signup
			if(parameters.get("request")[0].equals("signin")){
				// Get the username and password, call the authenticate function to verify the username/password
				boolean authenticationStatus = authenticate(parameters.get("username")[0],parameters.get("password")[0],response,serverresponse);
			}
			//else if (parameters.get("request")[0].equals("signup")){
			//	THIS PART MOVED TO THE HTTPPOST area. 
			// 	User sends large amounts of data, its trouble to handle them as a string
			//	Move to HTTP post and get the complete object as a JSON string
			//}
		}

		if(Globals.DEBUG) System.out.println("WARNING: handleUserGet :PARTIALLY IMPLEMENTED!");
	}

	public void handleDebuggerGet(Map<String, String[]> parameters, HttpServletResponse response){// Parameters passed by the HTTP GET
		// Authenticate!!
		// Query servlets according to request and send info back
		if(Globals.LOUD) System.out.println("Debug Mode");
		try{
			if(Globals.LOUD) System.out.println("dVal = " +parameters.get("dVal")[0]);
		}catch(Exception e){
			if(Globals.LOUD) System.out.println("Debugmode exception caught");
		}
	}


	public void handleAdminGet(Map<String, String[]> parameters, HttpServletResponse response, ServerResponse serverresponse){// Parameters passed by the HTTP GET
		//TODO Authenticate!!
		// Query servlets according to request and send info bnValack
		if(Globals.DEBUG) System.out.println("INFO:Admin Mode: Welcome my Lords!");
		//TODO Authenticate!!!!!!!
		if(Globals.DEBUG) System.out.println("WARNING: Admin not authenticated");
		if(Globals.DEBUG) System.out.println("INFO:adminname:"+parameters.get("adminname")[0]+" password:" + parameters.get("password")[0]);

		//Admin task implementation

		if(parameters.get("request")[0].equals("registerservlet")){
			// Read the servlet URL and add it to the serverinternaldata data structure
			boolean status = serverinternaldata.registerNewServlet(parameters.get("URL")[0], serverresponse);
			if(Globals.DEBUG) System.out.println(parameters.get("URL")[0]);
			if(status){ // Let the admin know everything went well
				if(Globals.LOUD) System.out.println("Added Servlet");
				serverresponse.status=true;
				sendResponse(response, serverresponse,new String[]{"message","Servlet Registered"});
			}
			else{
				// Error message set inside serverinternaldata.registerNewServlet function
				sendResponse(response, serverresponse,null);
			}
		}
		else if(parameters.get("request")[0].equals("registerarena")){
			//Arena names are added at the time it was uploaded to the server (using a POST request)
			// TODO Makesure there is a way to map this name to the actual arena data.
			if(Globals.DEBUG) System.out.println("WARNING:sendMapToServlet() must be called before this!");
			if(Globals.DEBUG) System.out.println("WARNING:Assined map must be sent to the arena");
			if(Globals.DEBUG) System.out.println(parameters.get("arena")[0]);
			boolean status = serverinternaldata.registerNewArena(parameters.get("arena")[0], serverresponse);

			if(status){ // Let the admin know everything went well
				if(Globals.LOUD) System.out.println("Added Arena");
				sendResponse(response,serverresponse,new String[]{"message","Arena Registered"});
			}
			else{
				sendResponse(response,serverresponse,null);
			}
		}
		else if(parameters.get("request")[0].equals("maparenaservlet")){
			if(Globals.DEBUG) System.out.println(parameters.get("arena")[0]+"--"+ parameters.get("servlet")[0]);
			boolean status = serverinternaldata.mapArenaServlet(parameters.get("arena")[0], parameters.get("servlet")[0], serverresponse);
			if(status){ // Let the admin know everything went well
				sendResponse(response,serverresponse,new String[]{"message","Arena Servlet mapped"});
			}
			else{
				sendResponse(response,serverresponse,null);
			}
		}
		else if(parameters.get("request")[0].equals("tempinit")){
			// This is a temporary function.. remove once done
			boolean status = tempinit(response, serverresponse);
		}
		else if(parameters.get("request")[0].equals("tempgetdbpath")){
			// This is a temporary function.. remove once done
			File f = new File("a.a");
			sendResponse(response,serverresponse,new String[]{"message","Database Path: "+f.getAbsolutePath()});
		}
		return;
	}


	public boolean handleAdminPost(Map<String,String[]> parameters, HttpServletRequest request, HttpServletResponse response, ServerResponse serverresponse){

		if(Globals.DEBUG) System.out.println("AUTHENTICATE ADMIN POST! :"+parameters.get("adminname")[0]+"::"+parameters.get("password")[0]);
		System.out.println(parameters.keySet().toString());//TODO???? remove this 
		String inputData="";
		BufferedReader bufferedReader;

		try{
			// Read data from the post
			bufferedReader = request.getReader();
			inputData=bufferedReader.readLine();
		}catch(Exception e){
			// The above 2 lines are crucial for the rest of this function., Failure of any should be caught by this point and we should not (and cannot) continue
			serverresponse.status=false;
			serverresponse.message="Admin Post Crashed";
			return false;
		}

		if(parameters.get("request")[0].equals("registerinstitute")){
			// Map institute email domain to the institute name and save the map data (boundary) to a file

			boolean registered = registerInstitute(parameters.get("institutename")[0], parameters.get("institutedomain")[0], inputData, serverresponse);
			if(registered){
				sendResponse(response, serverresponse,new String[]{"message","Institure registered successfully"});
			}else{
				sendResponse(response,serverresponse,null);
			}
			
		}
		else{
			serverresponse.status=false;
			serverresponse.message="Invalid Admin Post Request!";
			sendResponse(response,serverresponse, null);
		}
		return false;
	}


	public boolean handleUserPost(Map<String,String[]> parameters, HttpServletRequest request, HttpServletResponse response , ServerResponse serverresponse){
		if(Globals.DEBUG) System.out.println("AUTHENTICATE USER POST!");

		if(parameters.get("loggedin")[0].equals("false")){
			if(parameters.get("request")[0].equals("signup")){
				// User needs to signup
				// User sends the data packaged in a user object json string
				String inputData="";
				BufferedReader r;
				try {
					r = request.getReader();
					inputData=r.readLine();			//r.readLine() will get the string of the entity we sent. ie. json string
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				if(Globals.DEBUG) System.out.println("UserPost:"+inputData);
				usersignup(inputData,response,serverresponse);
				return false;

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


	public void handleGeneralViewerGet(Map<String, String[]> parameters, HttpServletResponse response){// Parameters passed by the HTTP GET
		// Resolve the request.. What are they asking for?
		// TODO: if we are planning on using websockets, 
		// 		 Do websockety stuff
		// TODO: if we are sending data directly from here
		// 	 	 Check the time and see if the localmax size of http post  data (for that query) is expired
		// 		 If data is not expired, send it to them
		if(Globals.DEBUG) System.out.println("WARNING: NOT IMPLEMENTED!");
	}
	
	
	
	// ------------------------------- actual helper functions--------------------------------------------------

	private boolean registerInstitute(String instituteName, String instituteDomain, String inputData, ServerResponse serverresponse){
		if(inputData==null){				// little sanity test
			serverresponse.status=false;
			serverresponse.message="Input Data null, Sanity failed";
			return false;
		}
		String fname = Globals.FILE_DIR+instituteName+".map";
		if(Globals.DEBUG) System.out.println("INFO: Creating file"+fname);	
		if(Globals.DEBUG) System.out.println("INFO: Got = "+inputData);	

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
			bw.write(inputData);
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

	private boolean authenticate(String username, String password, HttpServletResponse response, ServerResponse serverresponse){
		// do syncronized accesses to the database and crypto and verify authenticity  
		// TODO DBhelper and crypto already provide synchronized access (due to either lack of thread safety in those libraries or to preserve ram usage)
		// Look into this!!
		// Called by all usermodes, admin/user via Server Internal function calls. No outer entity has direct access to this function

		// Get the Hash and the salt from the server for the user
		String[] saltHash = null;
		boolean authenticationStatus;
		try {
			saltHash = dbHelper.getSaltAndHash(username,serverresponse);
			if (saltHash == null){
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
		boolean status = serverinternaldata.addActiveUser(username,token, serverresponse);

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
				sb.append(extra[i]);
				sb.append("\":\"");
				sb.append(extra[i++]);
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

		// dummy add users to database
		System.out.println("WARNING: Adding dummy passwords to table. Remove immediately!");//TODO
		try {
			// create a salt
			// use that salt and the password to build the hash, then put that hash and the salt to the table
			String salt=crypto.getSalt(32);
			dbHelper.acquireUsername("malithr", crypto.getHash("pass1",salt), salt, serverresponse);
			salt=crypto.getSalt(32);
			dbHelper.acquireUsername("numalj", crypto.getHash("pass2",salt), salt, serverresponse);
			salt=crypto.getSalt(32);
			dbHelper.acquireUsername("harithay", crypto.getHash("pass3",salt), salt, serverresponse);

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