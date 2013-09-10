package dataStore;

import java.io.Serializable;

public class Map implements Serializable{
	// Holds the map data.
	// Map is just a 2D array that contains Blocks.
	Block[][] theMap;
	// Map sizes may vary. Should be defined by the admins at the time they are created
	public Map(int height, int width){
		theMap = new Block[height][width];
	}
}

class Block implements Serializable{
	// An arena is made out of an array of 10m x 10m blocks 
	// block of area can hold anything suck as a building a user ..etc
	// Block is identified by its Lat/Long values of the top left corner
	float topLeftX,topLeftY;
	public Block(float x, float y){
		this.topLeftX=x;
		this.topLeftY=y;
	}
}
