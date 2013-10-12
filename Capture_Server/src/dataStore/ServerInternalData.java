package dataStore;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	// All users are granted an access token once they log in.
	// This access token is then passed to the websocket servlet the user will be working with and also to the the users themselves.
	// This is used for the sole purpose of keeping track of valid(unexpired) tokens
	// The entry will be removed once the user logs out
	// TODO: We keep the list on memory. If the server crashes, everyone has to relog in.
	Hashtable<String, ActiveUser> activeUsers;

	// TODO: Design the map!!
	// TODO: MAKE SURE THAT THIS LIST IS SMALL AS POSSIBLE.. MAPS CAN BE HUGE AND SERVERS DONT GIVE ALL THAT MUCH RAM TO SERVLETS!
	// Keeps all the arenas(The actual map details..this is the actual shit!) that are not yet been assigned to a websocket servlet
	// Reasons could be one of the following
	// Admin just uploaded the arenaMap to the resolver and did not assign it to a websocket servelt yet
	// A websocket servlet was stopped by and admin and the arenaMap was take back from it
	// A map is being reassigned from one ws servlet to another. the Resolver will be the middle man
	ArrayList<Object> arenaMaps;

	// LOCKS
	// Read Write lock for ServletPool list
	private final ReentrantReadWriteLock servletPoolReadWriteLock = new ReentrantReadWriteLock();
	private final Lock servletPoolReadLock  = servletPoolReadWriteLock.readLock();
	private final Lock servletPoolWriteLock = servletPoolReadWriteLock.writeLock();

	// Read Write Lock for ArenaPool list
	private final ReentrantReadWriteLock arenaPoolReadWriteLock = new ReentrantReadWriteLock();
	private final Lock arenaPoolReadLock  = arenaPoolReadWriteLock.readLock();
	private final Lock arenaPoolWriteLock = arenaPoolReadWriteLock.writeLock();

	// Read Write Lock for ArenaServletMap list
	private final ReentrantReadWriteLock arenaServletMapReadWriteLock = new ReentrantReadWriteLock();
	private final Lock arenaServletMapReadLock  = arenaServletMapReadWriteLock.readLock();
	private final Lock arenaServletMapWriteLock = arenaServletMapReadWriteLock.writeLock();

	// Read Write Lock for ActiveUsers list
	private final ReentrantReadWriteLock ActiveUserReadWriteLock = new ReentrantReadWriteLock();
	//private final Lock ActiveUserReadLock  = ActiveUserReadWriteLock.readLock();
	private final Lock ActiveUserWriteLock = ActiveUserReadWriteLock.writeLock();

	public ServerInternalData(){
		if(Globals.DEBUG) System.out.println("WARNING: Have you set the Admin Credentials, Right now we only have dummy usernames and password hashes");
		// DONE BITCH! NOW TEST THIS System.out.println("WARNING: BOTTLENECKS, BOTTLENECKS EVERYWHERE!: implement better locks to avoid locking for both reads and writes equally");
		arenaServletMap = new Hashtable<String,ServletInfo>();
		arenaPool = new ArrayList<String>();
		servletPool = new ArrayList<ServletInfo>();
		arenaMaps = new ArrayList<Object>();
		activeUsers = new Hashtable<String,ActiveUser>();
		if(Globals.DEBUG) System.out.println("WARNING: UNIMPLEMENTED ITEM : Register own server instance in the ServerInternalData store");
	}

	public boolean registerNewServlet(String URL){
		// This part must be atomic. Yes.. this part is correct. cant simplify any further
		servletPoolWriteLock.lock();
		// Add new servlet (possibly on a different computer/network or about this instance itself) information to this servlet instance
		// First Check if this servlet is already in the pool
		boolean hasDuplicate=false;
		for(int i=0; ((i<servletPool.size())&&(!hasDuplicate));i++){
			hasDuplicate |= servletPool.get(i).url.equals(URL);
		}

		if(!hasDuplicate){
			// Add that bitch to the list
			servletPool.add(new ServletInfo(URL));
			servletPoolWriteLock.unlock();
			return true;
		}
		else{
			// This servlet is already registered
			if(Globals.LOUD) System.out.println("Error! : Trying to add a duplicate servlet to the pool");
			servletPoolWriteLock.unlock();
			return false;
		}
	}

	public boolean registerNewArena(String arenaName){
		// This part must be atomic. Yes.. this part is correct. cant simplify any further
		arenaPoolWriteLock.lock();
		// Check if this arena is already registered
		boolean hasDuplicate=false;
		for(int i=0; ((i<arenaPool.size())&&(!hasDuplicate));i++){
			hasDuplicate |= arenaPool.get(i).equals(arenaName);
		}

		if(!hasDuplicate){
			// Add that bitch to the list
			arenaPool.add(arenaName);
			arenaPoolWriteLock.unlock();
			return true;
		}
		else{
			// This servlet is already registered
			if(Globals.LOUD) System.out.println("Error! : Trying to add a duplicate arena to the pool");
			arenaPoolWriteLock.unlock();
			return false;
		}
	}

	public boolean mapArenaServlet(String arenaName,String URL){
		// Add a mapping between an arena and a servlet
		// TODO If this is a remap. Then we need to remove the previous mapping manually before this step
		
		// First check if there is a mapping already in place
		
		// SYNC: We need to lock all 3 pools 
		arenaServletMapWriteLock.lock();
		
		if(arenaServletMap.get(URL)!=null){
			// there is a mapping ==> there is an arena. Dont want a duplicate
			if(Globals.LOUD)  System.out.println("ERROR:mapArenaServlet: OldMappingAlreadyExist");
			arenaServletMapWriteLock.unlock();
			return false;
		}
		

		// Second Check if arena is present
		boolean found=false;
		arenaPoolReadLock.lock();
		for(int i=0; ((i<arenaPool.size())&&(!found));i++){
			found |= arenaPool.get(i).equals(arenaName);
		}

		if(!found){
			// The arena is not present. Cant do the mapping
			if(Globals.LOUD)  System.out.println("ERROR:mapArenaServlet: ArenaNotFound");
			arenaPoolReadLock.unlock();
			arenaServletMapWriteLock.unlock();
			System.out.println("No Arena Pool");
			return false;
		}
		arenaPoolReadLock.unlock();

		// Then Check if the servlet is present
		ServletInfo targetServlet=null; 		// assume the worst
		//boolean status = false;
		servletPoolReadLock.lock();
		int k=0;
		for(int i=0; ((i<servletPool.size())&&(!found));i++){
			k=i;
			System.out.println(targetServlet.url);
			found |= targetServlet.url.equals(arenaName);
		}

		if(!found){
			// The arena is not present. Cant do the mapping
			if(Globals.LOUD) System.out.println("ERROR:mapArenaServlet: ArenaNotFound");
			servletPoolReadLock.unlock();
			arenaServletMapWriteLock.unlock();
			return false;
		}
		/*
		for(int i=0;i<servletPool.size();i++){
			targetServlet=servletPool.get(i);
			if(targetServlet.url.equals(URL)){
				status = true;
				break;
			}
		}*/
		servletPoolReadLock.unlock();
		
		//if(status==false){
			// servlet is not present.. break the process
		//	arenaServletMapWriteLock.unlock();
		//	return false;
		//}

		// TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO Send the database to the websocket servlet
		// add the mapping
		if(Globals.LOUD) System.out.println("WARNING!: Add mapping");
		arenaServletMap.put(arenaName, servletPool.get(k));
		arenaServletMapWriteLock.unlock();
		return true;
	}

	void unrgisterServlet(String URL){
		// TODO IMPLEMENT THIS
		// TODO THIS IS ONLY POSSIBLE IF THERE IS NO MAPPING BETWEEN ARENA AND THIS SERVELTS (Acquire Lock for sevletArenaMap)
		// remove all mappings from the arenaServletMap map (this servlet should NOT be serving any arenas)
		// only called when that sevlet is completely unused!

		// Check which servletInfo object corresponds to this URL
		// Remove the god damn mapping!
	}

	void unregisterArena(){
		// TODO THIS IS ONLY POSSIBLE IF THERE IS NO MAPPING BETWEEN THIS ARENA AND SERVELTS (Acquire Lock for sevletArenaMap)
		// TODO IMPLEMENT THIS
		// Arena is not active anymore. Remove mapping
		// i.e remove entry (TODO does that sevlet call this function or does this function ask the servelt to forget about that arena?)
	}

	void remapArena(){
		// TODO IMPLEMENT THIS
		// Move arena from one servlet to another
		// TODO DO THIS LAST
	}

	public boolean addActiveUser(String username, String token){
		// Add a newly logged in user to the pool
		// Take the current time, Create the activeUser object and add it to the list
		ActiveUserWriteLock.lock();
		if(Globals.DEBUG) System.out.println("WARNING: implement time feature for the active user");
		ActiveUser user = new ActiveUser(username, token);
		activeUsers.put(username, user);
		ActiveUserWriteLock.unlock();
		return false;

	}

	public boolean removeActiveUser(String username){
		// Remove auser from the pool (user signed out or tokemn expired?)
		ActiveUserWriteLock.lock();
		if(activeUsers.containsKey(username)){
			activeUsers.remove(username);
			ActiveUserWriteLock.unlock();
			return true;
		}
		else{
			//trying to remove user who does not exist
			if(Globals.LOUD) System.out.println("ERROR: Remving user that does not even exists!");
			ActiveUserWriteLock.unlock();
			return false;
		}

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
	public ActiveUser(String username, String tokens){
		this.username=username;
		this.token=token;
	}
	//TODO DATE AND TIME OF LOGIN

}

// TODO create mapping between geographical area ----->  arena so we can dynamically create arenas