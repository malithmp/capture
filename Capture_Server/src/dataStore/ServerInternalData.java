package dataStore;

import java.util.ArrayList;
import java.util.Hashtable;

public class ServerInternalData {
	// ALL ACCESS IS SYNCRONIZED!

	// 	Contains Information about other servlet instances as a Table
	ArrayList<ServletInfo> servletPool;

	// Contains Information about all the arenas being served
	ArrayList<String> arenaPool;
	
	// 	Contains mapping information about which arena is served by which servlet
	// 	arenaID -----> servletInfo
	Hashtable<String,ServletInfo> arenaServletMap;
	// The above said two data structures are pointing to the same servletInfo instances!!


	public ServerInternalData(){
		arenaServletMap = new Hashtable<String,ServletInfo>();
		arenaPool = new ArrayList<String>();
		servletPool = new ArrayList<ServletInfo>();
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
	
	public synchronized boolean mapArenaServlet(String arenaID,String URL){
		// Add a mapping between an arena and a servlet
		// First Check if arena is present
		if(arenaServletMap.get(URL)==null){
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
		// add the mapping
		arenaServletMap.put(arenaID, targetServlet);
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

// TODO create mapping between geographical area ----->  arena so we can dynamically create arenas