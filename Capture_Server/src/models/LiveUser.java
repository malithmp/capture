package models;

public class LiveUser {
	// Holds small amount of information about a user who is logged in. This is used to manage the session
	public String username;
	public String token;					// Session specific key that is generated using the passwordhash and time
	public LiveUser(String username, String tokens){
		this.username=username;
		this.token=token;
	}
	//TODO DATE AND TIME OF LOGIN
}
