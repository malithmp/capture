package dataStore;

class InstituteSpot {
	// WE CAN ASSUME ALL FUNCTION CALLS TO THIS CLASS ARE ATOMIC SINCE THE SERVERINTERNAL DATA CLASS WILL HOLD A LOCK BEFORE CALLING THIS
	// Each resolver instance will have a table of this class object. // WE CAN ASSUME ALL FUNCTION CALLS TO THIS CLASS ARE ATOMIC SINCE THE SERVERINTERNAL DATA CLASS WILL HOLD A WRITE LOCK BEFORE CALLING THIS
	// This object helps server pick a random L1 team to a user in a way that all teams have a balanced number of users
	// Resolver orders L1 team spots and caches them here. And when a user registers, resolver hands one spot from the cache. When Resolver runs out of spots, it
	// reloads. Always order balanced numbers: 100 team 1 spots and 100 team 2 spots
	String instituteName;
	Integer[] spots = new Integer[2];	// spots[0] = 100 and spots[1] = 0 means that when resolver is assigning teams , it will assign the next 100 users to team 0 and then reorder for more spots
	int counter;		// incremment and mod by team numbers to alternate between teams to balance load. Nothing much here!
	
	public InstituteSpot(String name) {
		this.instituteName=name;
		counter=0;
	}
		
	public int getL1Team(){
		// WE CAN ASSUME ALL FUNCTION CALLS TO THIS CLASS ARE ATOMIC SINCE THE SERVERINTERNAL DATA CLASS WILL HOLD A WRITE LOCK BEFORE CALLING THIS
		// return 0 or a positive number (team number) if we found spot. 
		// returns -1 if we are out of spots to return. The server will have to re order spots and then call this function again
		int team = Math.abs((counter++)%2);
		if(spots[team]>0){
			spots[team]--;
			return team;
		}
		else{
			team = (team+1)%2;
			if(spots[team]>0){
				spots[team]--;
				return team;
			}
			else{
				return -1;
			}
		}
	}
	
	public void reloadSpots(int team1, int team2){
		// WE CAN ASSUME ALL FUNCTION CALLS TO THIS CLASS ARE ATOMIC SINCE THE SERVERINTERNAL DATA CLASS WILL HOLD A WRITE LOCK BEFORE CALLING THIS
		// we fill equal amounts for each team
		spots[0]=team1;
		spots[1]=team2;
		//reloaded
	}
	
	
}
