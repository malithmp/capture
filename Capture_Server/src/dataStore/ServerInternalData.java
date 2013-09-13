package dataStore;

import java.util.ArrayList;
import java.util.Hashtable;

public class ServerInternalData {
	// ALL ACCESS IS SYNCRONIZED!
	
	// Login data of administrators usernames and password hashes
	public static final String[] adminnames={"admin1","admin2","admin3"};
	public static final String[] adminpasshash={"hash1","hash2","hash3"};

	// 	Contains Information about other servlet instances as a Table
	ArrayList<ServletInfo> servletPool;

	// Contains Information about all the arenas being served
	ArrayList<String> arenaPool;
	
	// 	Contains mapping information about which arena is served by which servlet
	// 	arenaID -----> servletInfo
	Hashtable<String,ServletInfo> arenaServletMap;
	// The above said two data structures are pointing to the same servletInfo instances!!
	
	// Contains information about currently 'logged in and playing' users
	// Everytime a user logs in, we place some info about that user in this list
	// This is used for the sole purpose of keeping track of valid(unexpired) tokens
	ArrayList<ActiveUser> activeUsers;

	// TODO: Design the map!!
	// TODO: MAKE SURE THAT THIS LIST IS SMALL AS POSSIBLE.. MAPS CAN BE HUGE AND SERVERS DONT GIVE ALL THAT MUCH RAM TO SERVLETS!
	// Keeps all the arenas(The actual map details..this is the actual shit!) that are not yet been assigned to a websocket servlet
	// Reasons could be one of the following
	// Admin just uploaded the arenaMap to the resolver and did not assign it to a websocket servelt yet
	// A websocket servlet was stopped by and admin and the arenaMap was take back from it
	// A map is being reassigned from one ws servlet to another. the Resolver will be the middle man
	ArrayList<Object> arenaMaps;
	
	public ServerInternalData(){
		System.out.println("WARNING: Have you set the Admin Credentials, Right now we only have dummy usernames and password hashes");
		arenaServletMap = new Hashtable<String,ServletInfo>();
		arenaPool = new ArrayList<String>();
		servletPool = new ArrayList<ServletInfo>();
		arenaMaps = new ArrayList<Object>();
		activeUsers = new ArrayList<ActiveUser>();
		System.out.println("WARNING: UNIMPLEMENTED ITEM : Register own server instance in the ServerInternalData store");
	}

	public synchronized boolean registerNewServlet(String URL){
		// Add new servlet (possibly on a different computer/network or about this instance itself) information to this servlet instance
		// First Check if this servlet is already in the pool
		boolean hasDuplicate=false;
		for(int i=0; ((i<servletPool.size())&&(!hasDuplicate));i++){
			hasDuplicate |= servletPool.get(i).url.equals(URL);
		}

		if(!hasDuplicate){
			// Add that bitch to the list
			servletPool.add(new ServletInfo(URL));
			return true;
		}
		else{
			// This servlet is already registered
			System.out.println("Error! : Trying to add a duplicate servlet to the pool");
			return false;
		}
	}

	public synchronized boolean registerNewArena(String arenaName){
		// Check if this arena is already registered
		boolean hasDuplicate=false;
		for(int i=0; ((i<arenaPool.size())&&(!hasDuplicate));i++){
			hasDuplicate |= arenaPool.equals(arenaName);
		}

		if(!hasDuplicate){
			// Add that bitch to the list
			arenaPool.add(arenaName);
			return true;
		}
		else{
			// This servlet is already registered
			System.out.println("Error! : Trying to add a duplicate arena to the pool");
			return false;
		}
	}
	
	public synchronized boolean mapArenaServlet(String arenaName,String URL){
		// Add a mapping between an arena and a servlet
		
		// First Check if arena is present
		if(arenaServletMap.get(URL)!=null){
			// there is a mapping ==> there is an arena. Dotn want a duplicate
			return false;
		}
		
		// Then Check if the servlet is present
		ServletInfo targetServlet=null; 		// assume the worst
		boolean status = false;
		for(int i=0;i<servletPool.size();i++){
			targetServlet=servletPool.get(i);
			if(targetServlet.url.equals(URL)){
				status = true;
				break;
			}
		}
		if(status==false){
			// servlet is not present.. break the process
			return false;
		}
		
		// TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO Send the database to the websocket servlet
		System.out.println("WARNING!: Add mapping");
		// add the mapping
		arenaServletMap.put(arenaName, targetServlet);
		return true;
	}
	
	void unrgisterServlet(String URL){
		// only called when that sevlet is completely unused!
		// TODO do a safety check?
		// remove all mappings from the arenaServletMap map (this servlet should NOT be serving any arenas)

		// Check which servletInfo object corresponds to this URL
		// Remove the god damn mapping!
	}

	void unregisterArena(){
		// Arena is not active anymore. Remove mapping
		// i.e remove entry (TODO does that sevlet call this function or does this function ask the servelt to forget about that arena?)
	}
	
	void remapArena(){
		// Move arena from one servlet to another
		// TODO DO THIS LAST
	}

}

class ServletInfo{
	// Contains Information about other servlet instances as a TablearenaID
	//		Their load
	//		Their URI if they are not local. Null if they are local

	int load;
	String url;
	public ServletInfo(String url){
		this.url=url;
	}
	
}

class ActiveUser{
	// Holds small amount of information about a user who is logged in. This is used to manage the session
	String username;
	String token;					// Session specific key that is generated using the passwordhash and time
	//TODO DATE AND TIME OF LOGIN
	
}

// TODO create mapping between geographical area ----->  arena so we can dynamically create arenas