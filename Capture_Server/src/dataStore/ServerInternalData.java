package dataStore;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServerInternalData {
	// ALL ACCESS IS SYNCRONIZED!

	// 	Contains Information about other servlet instances as a Table
	ArrayList<ServletInfo> servletPool;

	// 	Contains mapping information about which arena is served by which servlet
	// 	arenaID -----> servletInfo
	Hashtable<Integer,ServletInfo> arenaServletMap;
	// The above said two data structures are pointing to the same servletInfo instances!!


	public ServerInternalData(){
		arenaServletMap = new Hashtable<Integer,ServletInfo>();
		servletPool = new ArrayList<ServletInfo>();
		System.out.println("WARNING: UNIMPLEMENTED ITEM : Register own server instance in the ServerInternalData store");
	}

	public synchronized boolean registerNewServlet(String URL){
		// Add new servlet (possible on a different computer/network or about this instance itself) information to this servlet instance
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

	public synchronized boolean makeArenaServletMapping(){
		// Add a mapping between an arena and a servlet
		// First Check if arena is present
		// Then Check if the servlet is present
		// TODO
		return true;
	}
	
	void unrgisterServlet(String URL){
		// only called when that sevlet is completely unused!
		// TODO do a safety check?
		// remove all mappings from the arenaServletMap map (this servlet should NOT be serving any arenas)

		// Check which servletInfo object corresponds to this URL
		// Remove the god damn mapping!
	}

	void closeArena(){
		// Arena is not active anymore. Remove mapping
		// i.e remove entry (TODO does that sevlet call this function or does this function ask the servelt to forget about that arena?)
	}

}

class ServletInfo{
	// Contains Information about other servlet instances as a Table
	//		Their load
	//		Their URI if they are not local. Null if they are local

	public ServletInfo(String url){
		this.url=url;
	}
	int load;
	String url;
}

// TODO create mapping between geographical area ----->  arena so we can dynamically create arenas
