package models;

public class User {
	// Object that represents a user
	// Username
	// Other user related information..maybe needed for their profile
	// User Level related stuff
	//		Level
	//		Weapons
	//		Unlocks and other stuff
	// Avatar
	
	// A temporary/smalle version of a auser is declared in ServrInternalData.java as ActiveUser
	// This is only a representation of a user who is logged int. These two have a little in common
		
	public String username;
	public String email;
	public int l1group;		// Level 1 is just a number
	public String l2group;		// Level 2 is just the institution
	public String l3group;		// Level 3 is not defined yet //TODO
	public String firstname;
	public String lastname;
	public String home;
	
	
	//TODO
	//other user data
	public User(String username,String email,int l1group,String l2group,String l3group,String firstname,	String lastname,String home){
		this.username=username;
		this.email=email;
		this.l1group=l1group;
		this.l2group=l2group;
		this.l3group=l3group;
		this.firstname=firstname;
		this.lastname=lastname;
		this.home=home;
	}
}
