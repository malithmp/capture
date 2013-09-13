package dataStore;

public class User {
	// Object that represents a user
	// Username
	// Password Hash <We dont want to save the plaintext password here do we?>
	// Other user related information..maybe needed for their profile
	// User Level related stuff
	//		Level
	//		Weapons
	//		Unlocks and other stuff
	// Avatar
	
	// THIS DATA IS STORED IN A DATABASE AND IS SHARED ACROSS ALL RESOLVER INSTANCES!
	
	String username;
	String passwordHash;
	//TODO
	
	public User(String username, String hashpass){
		this.username=username;
		this.passwordHash = hashpass;
		System.out.println("WARNING: User class not fully implemented");
	}
}
