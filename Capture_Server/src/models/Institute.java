package models;

public class Institute{
	// This class holds key information about an institute
	// HOLDS NON REALTIME DATA. DATA IN THIS CLASS IS ONLY MODIFIED AT CREATION TIME AND RARELY WHEN WE NEED TO FIX AN ERROR MADE DURING WE CREATE IT
	// Its Name
	// Its email domain
	// Its boundary (as an array of coordinates in order)
	// These data will also be stored in a file and the path to that file will be stored in the database
	String name;
	String emaildomain;
	public Institute() {
		// TODO Auto-generated constructor stub
	}
}
