package dataStore;

public class ArenaMap {
	/*
	 * Contains all blocks that belong to an arena
	 * Has a DB access to the remaining UnitBlocks for that arena which are not in the Blocks
	 * 
	 * Takes care of kicking UnitBlocks to Disk(DB)
	 * Takes care of creating and destroying objects //TODO make sure we update the DB before deleting UnitBlocks so we dont lose information
	 * Has the logic to partition the entire ArenaMap in to rectangular partitions that
	 * 		1) Won't overlap
	 * 		2) Are actual rectangles (no empty spots in the middle)
	 * 		3) Won't cross the arena boundary
	 * 		4) Takes care of overlapping arena UnitBlocks (we have to keep a reference/URL to the other WorkerUnit that has this UnitBlock)
	 * 
	 * 
	 * */
}
