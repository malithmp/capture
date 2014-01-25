package models;

public class Block {
	/* 
	 * A Block is a **rectangular** collection of UnitBlocks (To make partitioning the full arena easy)
	 * 
	*/
	
	UnitBlock[][] block;
	public int height;
	public int width;
	
	public Block(int width, int height){
		this.width=width;
		this.height=height;
		block = new UnitBlock[width][height];
	}
	
	public void populateBlock(UnitBlock[] uBlocks){
		// TODO read the array and puppulate them accordingly
	}
}
