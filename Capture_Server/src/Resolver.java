import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dataStore.ServerInternalData;
import net.sf.json.JSONObject;
// Handles the http based request of the server

@WebServlet(value="/Resolver", asyncSupported = true)
public class Resolver extends HttpServlet {
	// Responsible for accepting the initial client request and assigning a websocket servlet for
	// Future communications. 
	// Checks for loads of each servlet, data distribution and other stuff before making decision

	private static final long serialVersionUID = 1L;
	ServerInternalData serverinternaldata;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Resolver() {
		super();
		//Servlet specific initializations
		initdata();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Check if the request is for user connection/ debugger/admin request or General viewer request
		// If user connection (a user needs to play the game)serverinternaldata
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
		
		//Match request type
		if(parameters==null || !parameters.containsKey("requesttype")){
			// invalid request. Does not adhere to protocol. kindly ask them to GTFO!
			PrintWriter pw = response.getWriter();
			if(parameters==null){
				pw.println("<html><h1> How about No! </h1><p>Protocol Error: no parameters provided!</p></html>");
			}
			else{
				System.out.println("Error: Requesttype not provided!");
				pw.println("<html><h1> How about No! </h1><p>Protocol ErrornVal: requesttype parameter not specified!</p></html>");
			}	
		}
		// So far so good. Depending on the user  type we can now call the helper functions.
		else if(parameters.get("requesttype")[0].equals("user")){
			// A user who wants to play. 
			// We put this option first since its the most likely request to happen
			//TODO
			System.out.println("WARNING: NOT IMPLEMENTED!");
			//System.out.println(parameters.get("c")[1]);
			//System.out.println(parameters.get("c")[0]);
		}
		else if(parameters.get("requesttype")[0].equals("general")){
			// A general user who wants to view the game. (not necessarily a signed up user) 
			// We put this option second since its the second most likely request to happen
			handleGeneralViewer(parameters,response);
		}
		else if(parameters.get("requesttype")[0].equals("debug")){
			// A debugger connection. // This must be authenticated as it has access to basically everything!!! 
			// We put this option 3rd since its much less likely request to happen
			//TODO:: AUTHENTICATE THIS SHIT!
			handleDebugger(parameters,response);
			System.out.println("WARNING: DEBUG MODE NOT IMPLEMENTED!");
		}
		else if(parameters.get("requesttype")[0].equals("admin")){
			// An admin connection. // This too must be authenticated as it has access to basically everything!!! 
			// We put this option last since its the least likely request to happen
			//TODO:: AUTHENTICATE THIS SHIT!
			handleAdmin(parameters,response);
			System.out.println("WARNING: ADMIN MODE NOT IMPLEMENTED!");
		}
		else{
			// Again. invalid request. kindly ask them to GTFO!
			PrintWriter pw = response.getWriter();
			pw.println("<html><h1> How about No! </h1><p>Protocol Error: requesttype invalid!</p></html>");
			System.out.println("Invalid Protocol Request Detected!"+parameters.get("requesttype"));
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
		
	}
	//---------------INIT-----------------
	private void initdata(){
		serverinternaldata = new ServerInternalData();
	}
	//-------------END INIT---------------response

	// -------------HELPER FUNCTIONS---------------
	// -----------must be thread safe--------------

	public void handleGeneralViewer(Map<String, String[]> parameters, HttpServletResponse response){// Parameters passed by the HTTP GET
		// Resolve the request.. What are they asking for?
		// TODO: if we are planning on using websockets, 
		// 		 Do websockety stuff
		// TODO: if we are sending data directly from here
		// 	 	 Check the time and see if the localmax size of http post  data (for that query) is expired
		// 		 If data is not expired, send it to them
		System.out.println("WARNING: NOT IMPLEMENTED!");
	}
	public void handleUser(Map<String, String[]> parameters, HttpServletResponse response){// Parameters passed by the HTTP GET
		// 		Read authentication info
		//		Check against databases to see if this guy is legit
		//		Create access token for future communications. Let the other server instances know of this token. Set expiration time? TODO: expiriation time for token

		// 		Get Location and Arena requested by the user
		// 		Verify if the request can be comresponsepleted (Current Location is too far away from the arena.. user is out of range)

		// 		If the above passes, Query all websocket servlets to see which one is serving that arena (may be done at a different stage: ie at startup of server.. or during operation)

		// 		Send the client with the specific URI to that servlet and send the servlet the same token that user got when they logged in
		System.out.println("WARNING: NOT IMPLEMENTED!");
	}

	public void handleDebugger(Map<String, String[]> parameters, HttpServletResponse response){// Parameters passed by the HTTP GET
		// Authenticate!!
		// Query servlets according to request and send info back
		System.out.println("Debug Mode");
		try{
			System.out.println("dVal = " +parameters.get("dVal")[0]);
		}catch(Exception e){
			System.out.println("Debugmode exception caught");
		}
	}
	
	public void handleAdmin(Map<String, String[]> parameters, HttpServletResponse response){// Parameters passed by the HTTP GET
		// Authenticate!!
		// Query servlets according to request and send info bnValack
		System.out.println("Admin Mode: Welcome my Lords!");
		//TODO Authenticate!!!!!!!
		System.out.println("WARNING: Admin not authenticated");
		System.out.println("adminname:"+parameters.get("adminname")[0]+" password:" + parameters.get("password")[0]);
		System.out.println(parameters.get("URL")[0]);
		//Admin task implementation
		if(parameters.get("action")[0].equals("registerservlet")){
			// Read the servlet URL and add it to the serverinternaldata data structure
			boolean status = serverinternaldata.registerNewServlet(parameters.get("URL")[0]);
			if(status){ // Let the admin know everything went well
				try {
					PrintWriter pw = response.getWriter();
					pw.println("status=true");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			else{
				try {
					PrintWriter pw = response.getWriter();
					pw.println("status=false");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if(parameters.get("action")[0].equals("mapservlet")){
			//TODO reigster (arenaID) (servletURL)
		}
		else if(parameters.get("action")[0].equals("registerarena")){
			//TODO MOVE THIS TO THE DOPOST PART SINCE THIS CONTAINS BINARY DATA 
			//TODO add the data to the secified websocket servlet
			//TODO add the name to the frontend
		}
		
		return;
	}
}