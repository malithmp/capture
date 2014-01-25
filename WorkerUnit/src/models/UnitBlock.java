package models;

public class UnitBlock {
	/*
	 * A HxH (decide 'H' later TODO 10m?, We use a coefficient that convert lat and long to distance depending on current geo region) block of area of actual land. 
	 * A block contains a list of all the users, resources, buildings..etc in the block
	 * 
	 * For a given coordinate we can find the block which that coordinate sits
	 * 
	 * A block is represented by lat and long values and latHeight and longWidth values
	 * 	- lat and long pair defines the center of the UnitBlock
	 * 	- Height and Width define how big the UnitBlock is (Lat and Long lines are compressed close to the poles and expanded near the equator, This is why we need this)
	 *  - We use the method to approximate block sizes since arenas are relatively small compared to the world, but they can be anywhere in the world. 
	 *    We will have different scales for different geo locations
	 * 
	 * A Block own its left and bottom vertices
	 * 	  Block 1    Block 2
	 *  _____________________
	 *  |		^ |			 |
	 *  |	   (2)|			 |
	 *  |<-(1)    |<-(3)	 |
	 *  |_________|__________|
	 *  	^
	 *     (4)
	 *  
	 *  Block1 owns (1) and (4) .. (2) and (3) are owned by the adjecent blocks
	 *  In mathematical interpretation
	 *  
	 *  UnitBlock=>  bottomLat <= LAT < topLat  & leftLong <= LONG < rightLont
	 *  
	 * TODO: Has a readonly boolean that states if the unit block is shared among multiple arenas (so we can manage thread safety) 
	 **/
}
