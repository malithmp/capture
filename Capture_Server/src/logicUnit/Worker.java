package logicUnit;

public class Worker {
	// Multiple threads (each representing a user) enqueues work
	// Items are sorted according to the time it hit the server (or that time + offset for taskt that must be done in future... delayed log out. Or timebomb like events)
	// Single worker thread to sequentially process all work (Since all steps must be done in the order)
	// Items queued at the same time (same time stamp) will be processed sequentially(thread that locks the shared variable first will get its task queued first..)
	// Once the work is done, the caller is notified to collect the processed data and GTFO
	
	// Has a different thread to only read global variables ( This is used to visualization and debugging purposes)
	
	
	
	// Synchronized queue
	public void postTask(){
		// Post stuff
	}
	
	// ---------------------------DEBUG-----------------------------------------
	
	// Debugging purpose only
	public void debuggerGetMap(int startx, int starty, int height, int width){
		// Return a copy of the map that meet the specs
	}
	
	// Debugging purpose only
	public void debuggerGetPath(int startx, int starty, int height, int width){
		// Return a path (a curve)
	}
	
	// ---------------------------DEBUG-----------------------------------------
}