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
				sendResponse("{\"status\":\"false\",\"message\":\"Protocol Error: no parameters provided\"}",response);
			}
			else{
				if(Globals.DEBUG) System.out.println("ERROR: Requesttype not provided!");
				sendResponse("{\"status\":\"false\",\"message\":\"Protocol Error nVal: requesttype parameter not specified!\"}",response);
			}	
		}
		// So far so good. Depending on the user  type we can now call the helper functions.
		else if(parameters.get("requesttype")[0].equals("user")){
			// A user who wants to play. 
			// We put this option first since its the most likely request to happen
			//TODO
			if(Globals.DEBUG) System.out.println("WARNING: parameters.get user NOT IMPLEMENTED!");
			handleUserGet(parameters,response);
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
			handleAdminGet(parameters,response);
		}
		else{
			// Again. invalid request. kindly ask them to GTFO!
			if(Globals.LOUD) System.out.println("Invalid Protocol Request Detected!"+parameters.get("requesttype"));
			sendResponse("{\"status\":\"false\",\"message\":\"Protocol Error: requesttype invalid!\"}",response);
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
				sendResponse("{\"status\":\"false\",\"message\":\"Protocol Error: no parameters provided!\"}",response);
			}
			else{
				if(Globals.DEBUG) System.out.println("ERROR: Requesttype not provided!");
				sendResponse("{\"status\":\"false\",\"message\":\"requesttype parameter not specified!\"}",response);
			}
		}
		else if(parameters.get("requesttype")[0].equals("admin")){
			handleAdminPost(parameters, request, response);
		}
		else if(parameters.get("requesttype")[0].equals("servlet")){
			if(Globals.DEBUG) System.out.println("WARNING: POST SERVLET NOT IMPLEMENTED!");

		}
		else if(parameters.get("requesttype")[0].equals("websocket")){
			if(Globals.DEBUG) System.out.println("WARNING: POST WEBSOCKET NOT IMPLEMENTED!");

		}
		else if(parameters.get("requesttype")[0].equals("user")){
			handleUserPost(parameters, request, response);
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

	//-------------END INIT---------------response


	// -------------HELPER FUNCTIONS---------------
	// -----------must be thread safe--------------

	public boolean authenticate(String username, String password){
		// do syncronized accesses to the database and crypto and verify authenticity  
		// TODO DBhelpwe and crypto already provide synchronized access (due to either lack of threa safety in those libraries or to preserve ram usage)
		// Look into this!!
		// Called by all usermodes, admin/user via Server Internal function calls. No outer entity has direct access to this function

		// Get the Hash and the salt from the server for the user
		String[] saltHash = null;
		try {
			saltHash = dbHelper.getSaltAndHash(username);
			if (saltHash == null){
				return false;
			}
			String hash = crypto.getHash(password, saltHash[0]);

			if (hash.equals(saltHash[1])){
				return true;
			}
			else{
				return false;
			}
		} catch (Exception e) {
			if(Globals.DEBUG) System.out.println("CRITICAL ERROR: Authentication Procedure Caused an exception. Potential Crypto or DB issue");
			e.printStackTrace();
		}

		// Append the salt and hash the password
		// Check generated hash against the local hash (one that was generated at the time user logged in!) //TODO CAHNGE PASSWORD?
		return false;
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

	public void handleUserGet(Map<String, String[]> parameters, HttpServletResponse response){// Parameters passed by the HTTP GET
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
				// Return the access token 
				boolean authenticationStatus = authenticate(parameters.get("username")[0],parameters.get("password")[0]);
				if(!authenticationStatus){
					// Auth failed, let the user know
					if(Globals.LOUD) System.out.println("INFO: AUTH FAIL");
					sendResponse("{\"status\":\"false\",\"message\":\"Authentication Failed\"}",response);
				}
				else{
					// auth succeeded. Generate an access token
					if(Globals.LOUD) System.out.println("INFO: AUTH PASS");
					// Send the access token to the user.
					// Keep track of the access token until the user logs out
					if(Globals.DEBUG) System.out.println("WARNING: Access token is a randomg string, Implement something better");
					String token = crypto.getSalt(32);
					boolean status = serverinternaldata.addActiveUser(parameters.get("username")[0],token);
					if(status==true){
						sendResponse("{\"status\":\"true\",\"token\":\""+token+"\"}",response);
					}
					else{
						sendResponse("{\"status\":\"false\",\"message\":\"Authentication Failed\"}",response);
					}

				}

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

	public void handleAdminGet(Map<String, String[]> parameters, HttpServletResponse response){// Parameters passed by the HTTP GET
		// Authenticate!!
		// Query servlets according to request and send info bnValack
		if(Globals.DEBUG) System.out.println("INFO:Admin Mode: Welcome my Lords!");
		//TODO Authenticate!!!!!!!
		if(Globals.DEBUG) System.out.println("WARNING: Admin not authenticated");
		if(Globals.DEBUG) System.out.println("INFO:adminname:"+parameters.get("adminname")[0]+" password:" + parameters.get("password")[0]);

		//Admin task implementation

		if(parameters.get("request")[0].equals("registerservlet")){
			// Read the servlet URL and add it to the serverinternaldata data structure
			boolean status = serverinternaldata.registerNewServlet(parameters.get("URL")[0]);
			if(Globals.DEBUG) System.out.println(parameters.get("URL")[0]);
			if(status){ // Let the admin know everything went well
				if(Globals.LOUD) System.out.println("Added Servlet");
				sendResponse("{\"status\":\"true\",\"message\":\"Servlet Registered\"}",response);
			}
			else{
				sendResponse("{\"status\":\"false\",\"message\":\"Servlet Registration Failed\"}",response);
			}
		}
		else if(parameters.get("request")[0].equals("registerarena")){
			//Arena names are added at the time it was uploaded to the server (using a POST request)
			// TODO Makesure there is a way to map this name to the actual arena data.
			if(Globals.DEBUG) System.out.println("WARNING:sendMapToServlet() must be called before this!");
			if(Globals.DEBUG) System.out.println("WARNING:Assined map must be sent to the arena");
			if(Globals.DEBUG) System.out.println(parameters.get("arena")[0]);
			boolean status = serverinternaldata.registerNewArena(parameters.get("arena")[0]);

			if(status){ // Let the admin know everything went well
				if(Globals.LOUD) System.out.println("Added Arena");
				sendResponse("{\"status\":\"true\",\"message\":\"Arena Registered\"}",response);
			}
			else{
				sendResponse("{\"status\":\"false\",\"message\":\"Arena Registration Failed\"}",response);
			}
		}
		else if(parameters.get("request")[0].equals("maparenaservlet")){
			if(Globals.DEBUG) System.out.println(parameters.get("arena")[0]+"--"+ parameters.get("servlet")[0]);
			boolean status = serverinternaldata.mapArenaServlet(parameters.get("arena")[0], parameters.get("servlet")[0]);
			if(status){ // Let the admin know everything went well
				sendResponse("{\"status\":\"true\",\"message\":\"Arena Servlet Mapped\"}",response);
			}
			else{
				sendResponse("{\"status\":\"false\",\"message\":\"Arena Servlet Mapping Failed\"}",response);
			}
		}
		else if(parameters.get("request")[0].equals("tempinit")){
			// This is a temporary function.. remove once done
			boolean status = tempinit(response);
		}
		else if(parameters.get("request")[0].equals("tempgetdbpath")){
			// This is a temporary function.. remove once done
			File f = new File("a.a");
			sendResponse("{\"status\":\"true\",\"message\":\" Database Path: "+f.getAbsolutePath()+"\"}",response);
		}
		return;
	}

	public boolean handleAdminPost(Map<String,String[]> parameters, HttpServletRequest request, HttpServletResponse response){
		if(Globals.DEBUG) System.out.println("AUTHENTICATE ADMIN POST! :"+parameters.get("adminname")[0]+"::"+parameters.get("password")[0]);

		System.out.println(parameters.keySet().toString());
		if(parameters.get("request")[0].equals("registerinstitute")){
			// Map institute email domain to the institute name and save the map data (boundary) to a file
			String inputData="";
			BufferedReader r;
			String iname = parameters.get("institutename")[0];
			String idom = parameters.get("institutedomain")[0];
			String fname = Globals.FILE_DIR+iname+".map";
			try{
				// Read data from the post
				r = request.getReader();
				inputData=r.readLine();
				
				if(Globals.DEBUG) System.out.println("INFO: Creating file"+fname);	
				if(Globals.DEBUG) System.out.println("INFO: Got = "+inputData);	
				
				
				File file = new File(fname);
				
				// if file doesnt exists, then create it
				if (!file.exists()) {
					if(Globals.DEBUG) System.out.println("INFO: Creating file: "+fname);	
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				// in line #1 of file we store the insitute name
				// in line #2 of file we store the institute domain
				// the rest of the file contains the json string that holds the coordinates of the boundary (as an array of coordinates in order)
				bw.write(iname);	// line #1 of = the insitute name
				bw.write("\n");
				bw.write(idom);	// line #1 of = the insitute domain
				bw.write("\n");
				bw.write(inputData);
				bw.close();
				
				dbHelper.addInstitute(iname, idom, fname);
				// Add these to the database
				sendResponse("{\"status\":\"true\",\"message\":\"Registered Institute successfully\"}",response);
				return true;
			}catch(Exception e){
				//TODO BE MORE SPECIFIC ABOUT THE ERROR MESSAGE. TELL USER WHAT WENT WRONG
				sendResponse("{\"status\":\"false\",\"message\":\"Register Institute Exception Occured! WHY?\"}",response);
				return false;
			}
			//			else if(parameters.get("request")[0].equals("registerinstitute")){
			//				// Map institute email domain to the institute name
			//				try {
			//					boolean status = dbHelper.addInstituteDomain(parameters.get("institutename")[0],parameters.get("institutedomain")[0]);
			//					if(status){ // Let the admin know everything went well
			//						sendResponse("{\"status\":\"true\",\"message\":\"Institute Registered\"}",response);
			//					}
			//					else{
			//						sendResponse("{\"status\":\"false\",\"message\":\"Institute Registration Failed\"}",response);
			//					}
			//				} catch (Exception e) {
			//					sendResponse("{\"status\":\"false\",\"message\":\"Register Institute Exception Occured! WHY?\"}",response);
			//				}
			//			}
		}
		else{
			sendResponse("{\"status\":\"false\",\"message\":\"Invalid Admin Post Request\"}", response);
		}
		//		String inputData="";
		//		BufferedReader r;
		//		try {
		//			r = request.getReader();
		//			inputData=r.readLine();
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}//r.readLine() will get the string of the entity we sent. ie. json string
		//		if(Globals.DEBUG) System.out.println("GOT:"+inputData);
		//		Gson gsn = new Gson();
		//		ArrayList<String>data = gsn.fromJson(inputData,ArrayList.class);
		//		if(data==null){
		//			if(Globals.DEBUG) System.out.println("ERROR:its dead jim");
		//			return false;
		//		}
		//		if(Globals.DEBUG) System.out.println("len"+data.size());
		//		for(int i=0;i<data.size();i++){
		//			if(Globals.DEBUG) System.out.println(""+i+"="+data.get(i));
		//		}
		//		if(Globals.DEBUG) System.out.println("data hash:\n"+data.hashCode());

		return false;
	}

	public boolean handleUserPost(Map<String,String[]> parameters, HttpServletRequest request, HttpServletResponse response){
		if(Globals.DEBUG) System.out.println("AUTHENTICATE USER POST!");
		if(parameters.get("loggedin")[0].equals("false")){
			if(parameters.get("request")[0].equals("signup")){
				// User needs to signup
				// User sends the data packaged in a user object json string
				String inputData="";
				BufferedReader r;
				try {
					r = request.getReader();
					inputData=r.readLine();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//r.readLine() will get the string of the entity we sent. ie. json string
				if(Globals.DEBUG) System.out.println("UserPost:"+inputData);

				JSONObject jObj = (JSONObject) JSONValue.parse(inputData);
				// TODO makesure DBHelper does everything in a threadsafe way
				// First we Hash the password and prepare everything needed to add a username to the userpass table
				String salt = crypto.getSalt(32);
				String hash = crypto.getHash((String)jObj.get("password"), salt);
				// Second we add the user to the userpass table. This ensures we have a unique username. If this fails we let the user know it failed
				try {
					boolean added = dbHelper.acquireUsername((String)jObj.get("username"),hash,salt);
					if(added==false){
						sendResponse("{\"status\":\"false\",\"message\":\"Username "+(String)jObj.get("username")+ " Already In Use\"}", response);
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
					l2 = dbHelper.getInstituteName(domain);

					if (l2 == null){
						// institute was not found in the database.
						sendResponse("{\"status\":\"false\",\"message\":\" "+"Your institute "+domain+ " is not registered yet\"}", response);
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
					sendResponse("{\"status\":\"true\",\"message\":\"" + "Sucessfully Registered to " + user.l2group+ ":"+user.l1group+ "}", response);

				} catch (Exception e) {
					e.printStackTrace();
					sendResponse("{\"status\":\"false\",\"message\":\"Username "+(String)jObj.get("username")+ " Already In Use\"}", response);
					return false;
				}
				// If it passed, we proceed with adding the remaining data to the tables
				sendResponse("{\"status\":\"true\",\"message\":\"Username "+(String)jObj.get("username")+ " Added to the Database\"}", response);
				return true;
				//System.out.println("Address is:"+jObj.get("Address"));

			}
			else{
				sendResponse("{\"status\":\"false\",\"message\":\"Username " +parameters.get("request")[0]+" is not legal for POST" + "\"}", response);
				return false;
			}
		}
		else{
			sendResponse("{\"status\":\"false\",\"message\":\"Username " +parameters.get("loggedin")[0]+" is not legal for POST" + "\"}", response);
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

	public void sendResponse(String replyString, HttpServletResponse response){
		// this guy sends all the responses generated by the helper functions
		try {
			PrintWriter pw = response.getWriter();
			pw.println(replyString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean tempinit(HttpServletResponse response){
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
		status &= serverinternaldata.registerNewArena(arenaname);
		System.out.println("\tWARNING:registerNewArena:"+status);
		// Register the websocket servlet
		status &= serverinternaldata.registerNewServlet(websocketUrl);
		System.out.println("\tWARNING:registerNewServlet:"+status);
		// Map the arena to the servlet
		status &= serverinternaldata.mapArenaServlet(arenaname, websocketUrl);
		System.out.println("\tWARNING:mapArenaServlet:"+status);

		// dummy add users to database
		System.out.println("WARNING: Adding dummy passwords to table. Remove immediately!");//TODO
		try {
			// create a salt
			// use that salt and the password to build the hash, then put that hash and the salt to the table
			String salt=crypto.getSalt(32);
			dbHelper.acquireUsername("malithr", crypto.getHash("pass1",salt), salt);
			salt=crypto.getSalt(32);
			dbHelper.acquireUsername("numalj", crypto.getHash("pass2",salt), salt);
			salt=crypto.getSalt(32);
			dbHelper.acquireUsername("harithay", crypto.getHash("pass3",salt), salt);

			dbHelper.addInstitute("UofT","utoronto.ca","./UofT.map");
			dbHelper.addInstitute("Ryerson","ryerson.ca","./Ryerson.map");
			dbHelper.addInstitute("York","yorku.ca","./York.map");
			dbHelper.addInstitute("Waterloo","uwaterloo.ca","./Waterloo.map");

			sendResponse("{\"status\":\"true\",\"message\":\" "+"TempInitCalled\"}", response);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("WARNING: Adding dummy institute email domain mappings");
		return status;
	}
}