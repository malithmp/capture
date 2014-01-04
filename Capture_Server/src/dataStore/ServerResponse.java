package dataStore;

public class ServerResponse {
	// This object is passed to helper function to store the state and the message
	// TODO makesure this object is recycled
	public boolean status;
	public String message;
	public ServerResponse(){
		status=true;
		message="";
	}
}
