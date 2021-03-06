package dataStore;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import models.InstituteSpot;
import models.LiveAdmin;
import models.LiveUser;
import models.ServerResponse;

public class ServerInternalData {
	// ALL ACCESS IS SYNCRONIZED!
	// MUST HOLD NON PERSISTANT DATA. STUFF THAT WONT MAKE A BIG DEAL OF THE SERVER CRASHES. STUFF LIKE THE CURRENT LIVE USERS (PEOPLE CAN JUST LOG BACK IN)
	// OR STUFF THAT THE ADMINS CAN QUICKLY RELOAD (LIKE SERVLET URLS AND MAPPINGS)
	// TODO MAKE METHODS TO PERIODICALLY BACK UP THESE STUFF SO IN CASE THE SERVLET CLOSES, WE CAN GO BACK TO A SNAPSHOT (NOT LOSE *ALL* THE INTERMEDIARY DATA)

	// Contains admin information( AdminName -> (salt, hash) ). We only keep this in the server binary
	// We create a dummy admin first, which will then be used to create the actual admin. Then this one will be removed
	// TODO: REMOVE THE FIRST ADMIN WITH THE REAL ONES
	// Current admin username: th30n3
	// current admin password: BananasAreG00d
	Hashtable<String, String[]> adminCredentials; // thread safety provided by liveUserReadWriteLock.

	// Currently logged in admins list
	Hashtable<String,LiveAdmin> adminPool;
	
	// 	Contains Information about other servlet instances as a Table
	ArrayList<ServletInfo> servletPool;

	// Contains Information about all the arenas being served
	ArrayList<String> arenaPool;

	// 	Contains mapping information about which arena is served by which servlet
	// 	arenaID -----> servletInfo
	Hashtable<String,ServletInfo> arenaServletMap;
	// The above said two data structures are pointing to the same servletInfo instances!!

	// Contains information about currently 'logged in' users
	// Everytime a user logs in, we place some info about that user in this list
	// All users are granted an access token once they log in.
	// This access token is then passed to the websocket servlet the user will be working with and also to the the users themselves.
	// This is used for the sole purpose of keeping track of valid(unexpired) tokens
	// The entry will be removed once the user logs out
	// TODO: We keep the list on memory. If the server crashes, everyone has to relog in.
	Hashtable<String, LiveUser> liveUsers;

	// TODO: Design the map!!
	// TODO: MAKE SURE THAT THIS LIST IS SMALL AS POSSIBLE.. MAPS CAN BE HUGE AND SERVERS DONT GIVE ALL THAT MUCH RAM TO SERVLETS!
	// Keeps all the arenas(The actual map details..this is the actual shit!) that are not yet been assigned to a websocket servlet
	// Reasons could be one of the following
	// Admin just uploaded the arenaMap to the resolver and did not assign it to a websocket servelt yet
	// A websocket servlet was stopped by and admin and the arenaMap was take back from it
	// A map is being reassigned from one ws servlet to another. the Resolver will be the middle man
	ArrayList<Object> arenaMaps;

	// Maps emailDomain -> instituteSpots
	// Resolver orders L1 team spots and caches them here. And when a user registers, resolver hands one spot from the cache. When Resolver runs out of spots, it
	// reorders them from the database. Always order balanced numbers: 100 team 1 spots and 100 team 2 spots
	// All access needs to be synchronized by the caller
	Hashtable<String,InstituteSpot> spots;	// spots[0] = 100 and spots[1] = 0 means that when resolver is assigning teams , it will assign the next 100 users to team 0 and then reorder for more spots

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

	// Read Write Lock for LiveUser list (ALSO FOR ADMINS)
	private final ReentrantReadWriteLock liveUserReadWriteLock = new ReentrantReadWriteLock();
	private final Lock liveUserReadLock  = liveUserReadWriteLock.readLock();
	private final Lock liveUserWriteLock = liveUserReadWriteLock.writeLock();

	//	// Read Write Lock for Email-Institute map
	//	private final ReentrantReadWriteLock emailInstituteMapReadWriteLock = new ReentrantReadWriteLock();
	//	private final Lock emailInstituteMapReadLock  = emailInstituteMapReadWriteLock.readLock();
	//	private final Lock emailInstituteMapWriteLock = emailInstituteMapReadWriteLock.writeLock();

	// Read Write Lock for Email-Institute map :  WRITES MIGHT HAPPEN EXTREAMELY RARELY! (WHEN AN ADMIN REGISTERS A NEW INSTITUTION!)
	private final ReentrantReadWriteLock spotReservationL2ReadWriteLock = new ReentrantReadWriteLock();
	//private final Lock spotReservationL2ReadLock  = spotReservationL2ReadWriteLock.readLock();
	private final Lock spotReservationL2WriteLock = spotReservationL2ReadWriteLock.writeLock();



	public ServerInternalData(){
		if(Globals.DEBUG) System.out.println("WARNING: Have you set the Admin Credentials, Right now we only have dummy usernames and password hashes");
		// DONE BITCH! NOW TEST THIS System.out.println("WARNING: BOTTLENECKS, BOTTLENECKS EVERYWHERE!: implement better locks to avoid locking for both reads and writes equally");
		arenaServletMap = new Hashtable<String,ServletInfo>();
		arenaPool = new ArrayList<String>();
		servletPool = new ArrayList<ServletInfo>();
		arenaMaps = new ArrayList<Object>();
		liveUsers = new Hashtable<String,LiveUser>();
		spots = new Hashtable<String, InstituteSpot>();
		adminPool = new Hashtable<String, LiveAdmin>();
		adminCredentials =  new Hashtable<String, String[]>();
		
		if(Globals.DEBUG) System.out.println("WARNING: UNIMPLEMENTED ITEM : Register own server instance in the ServerInternalData store");

	}

	public boolean registerNewServlet(String URL, ServerResponse serverresponse){
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
			serverresponse.status=false;
			serverresponse.message="Duplicate servlet";
			return false;
		}
	}

	public boolean registerNewArena(String arenaName, ServerResponse serverresponse){
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
			serverresponse.status=false;
			serverresponse.message="Duplicate arena";
			return false;
		}
	}

	public boolean mapArenaServlet(String arenaName,String URL,ServerResponse serverresponse){
		// Add a mapping between an arena and a servlet
		// TODO If this is a remap. Then we need to remove the previous mapping manually before this step

		// First check if there is a mapping already in place

		// SYNC: We need to lock all 3 pools 
		arenaServletMapWriteLock.lock();

		if(arenaServletMap.get(URL)!=null){
			// there is a mapping ==> there is an arena. Dont want a duplicate
			if(Globals.LOUD)  System.out.println("ERROR:mapArenaServlet: OldMappingAlreadyExist");
			arenaServletMapWriteLock.unlock();
			serverresponse.status=false;
			serverresponse.message="Already mapped";
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
			if(Globals.DEBUG)System.out.println("ERROR:No Arena in Pool!");
			serverresponse.status=false;
			serverresponse.message="Arena is not in the arena pool!";
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
			// The servlet is not present. Cant do the mapping
			if(Globals.LOUD) System.out.println("ERROR:mapArenaServlet: ServletNotFound");
			servletPoolReadLock.unlock();
			arenaServletMapWriteLock.unlock();
			serverresponse.status=false;
			serverresponse.message="Servlet is not in the arena pool!";
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

	public void unrgisterServlet(String URL){
		// TODO IMPLEMENT THIS
		// TODO THIS IS ONLY POSSIBLE IF THERE IS NO MAPPING BETWEEN ARENA AND THIS SERVELTS (Acquire Lock for sevletArenaMap)
		// remove all mappings from the arenaServletMap map (this servlet should NOT be serving any arenas)
		// only called when that sevlet is completely unused!

		// Check which servletInfo object corresponds to this URL
		// Remove the god damn mapping!
	}

	public void unregisterArena(){
		// TODO THIS IS ONLY POSSIBLE IF THERE IS NO MAPPING BETWEEN THIS ARENA AND SERVELTS (Acquire Lock for sevletArenaMap)
		// TODO IMPLEMENT THIS
		// Arena is not active anymore. Remove mapping
		// i.e remove entry (TODO does that sevlet call this function or does this function ask the servelt to forget about that arena?)
	}

	public void remapArena(){
		// TODO IMPLEMENT THIS
		// Move arena from one servlet to another
		// TODO DO THIS LAST
	}

	public boolean addLiveUser(String username, String token,ServerResponse serverresponse){
		// Add a newly logged in user to the pool
		// TODO Take the current time, Create the liveUser object and add it to the list
		liveUserWriteLock.lock();
		if(Globals.DEBUG) System.out.println("WARNING: implement time feature for the live user");
		LiveUser user = new LiveUser(username, token);
		liveUsers.put(username, user);
		liveUserWriteLock.unlock();
		return true;
	}

	public boolean addLiveAdmin(String adminName, String token,ServerResponse serverresponse){
		// Exact same procedure as adding a user
		// Add a newly logged in admin to the pool.
		// TODO Take the current time, Create the liveAdmin object and add it to the list
		liveUserWriteLock.lock();
		LiveAdmin admin = new LiveAdmin(adminName, token);
		adminPool.put(adminName, admin);
		liveUserWriteLock.unlock();
		return true;
	}

	public boolean isAdminValid(String adminname, String token){
		// confirms the admin is logged in
		boolean isLegit;
		liveUserReadLock.lock();
		LiveAdmin admin = adminPool.get(adminname);
		if(admin==null){
			liveUserReadLock.unlock();
			return false;
		}
		// have entry, check if the tokens match
		isLegit = token.equals(admin.token);
		liveUserReadLock.unlock();
		return isLegit;
	}
	
	public boolean isUserValid(String username, String token){
		// confirms the iser is logged in
		boolean isLegit;
		liveUserReadLock.lock();
		LiveUser user = liveUsers.get(username);
		if(user==null){
			liveUserReadLock.unlock();
			return false;
		}
		// have entry, check if the tokens match
		isLegit = token.equals(user.token);
		liveUserReadLock.unlock();
		return isLegit;
	}
	
	public boolean modifyAdminCredentials(String adminname, String salt, String hash, int command){
		// command 	1 : add
		//			2 : remove
		//			3 : update
 		liveUserWriteLock.lock();
		if(command==1){
			adminCredentials.put(adminname, new String[]{salt,hash});
			liveUserWriteLock.unlock();
			return true;
		}
		else if(command ==2){
			adminCredentials.remove(adminname);
			liveUserWriteLock.unlock();
			return true;
		}
		else if(command ==3){
			if(adminCredentials.containsKey(adminname)){
				adminCredentials.remove(adminname);
				adminCredentials.put(adminname, new String[]{salt,hash});
				liveUserWriteLock.unlock();
				return true;
			}
			else{
				liveUserWriteLock.unlock();
				return false;
			}
		}
		else{
			liveUserWriteLock.unlock();
			return false;
		}
	}
	
	public String[] getAdminCredentials(String adminname){
		// returns {salt, hash}
		String[] result=null;
		liveUserReadLock.lock();
		result=adminCredentials.get(adminname);
		liveUserReadLock.unlock();
		return result;
	}
	
	public boolean removeLiveUser(String username){
		// Remove auser from the pool (user signed out or tokemn expired?)
		liveUserWriteLock.lock();
		if(liveUsers.containsKey(username)){
			liveUsers.remove(username);
			liveUserWriteLock.unlock();
			return true;
		}
		else{
			//trying to remove user who does not exist
			if(Globals.LOUD) System.out.println("ERROR: Remving user that does not even exists!");
			liveUserWriteLock.unlock();
			return false;
		}

	}
	
	public int getSpot(String instituteDomain){
		// Check if the institute is on the hashtable (cached in ram)
		int team;
		spotReservationL2WriteLock.lock();
		InstituteSpot instituteSpot;
		if(spots.get(instituteDomain)!=null){
			// If it is, then use that to get a spot
			instituteSpot = spots.get(instituteDomain);
			team = instituteSpot.getL1Team();	// if spots ranout. this will return -1. Then the Resolver should reload the spots
			spotReservationL2WriteLock.unlock();
			return team;
		}
		// By the time we get to this part, We know for a fact that the Institute is legit (we add L2 before we add L1 teams)
		// So we create one and add it
		InstituteSpot i = new InstituteSpot(instituteDomain);
		i.reloadSpots(Globals.RELOAD_SPOT,Globals.RELOAD_SPOT);
		spots.put(instituteDomain, i);
		team = i.getL1Team();				// This will never failed. We just added it 100 spots!!!
		spotReservationL2WriteLock.unlock();
		return team;

	}

	
	public void reloadSpots(String instituteDomain, int team1, int team2){
		spotReservationL2WriteLock.lock();
		spots.get(instituteDomain).reloadSpots(team1, team2);;
		spotReservationL2WriteLock.unlock();
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

// TODO create mapping between geographical area ----->  arena so we can dynamically create arenas