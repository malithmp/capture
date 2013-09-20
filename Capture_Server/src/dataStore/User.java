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
	String passwordHashSalt;	// Salt that is used to secure hshed password even further: Randomly generated at time of authentication. Stored in plain sight because this is not secret.
								// This just makes hash lookup table attacks difficult. Even if this is compromized, The attacker has to build a new hash lookup table or brute force.
	
	//TODO
	//other user data
	
	public User(String username, String hashpass, String hashsalt){
		this.username=username;
		this.passwordHash = hashpass;
		this.passwordHashSalt=hashsalt;
		
		System.out.println("WARNING: User class not fully implemented");
	}
}
