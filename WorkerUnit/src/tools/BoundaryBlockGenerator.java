package tools;

import java.util.ArrayList;

public class BoundaryBlockGenerator {
	/*
	 * Given an array of all the Unit Blocks and the boundary lines (as coor1dinates), this will pick the UnitBlocks that would cover the arena
	 * 
	 * Logic
	 * 	- Input an array of lines
	 * 	- Input the array of all blocks
	 * 	- We take a line, then surround it with a rectangle of **boxes** the same size of the UnitBlock 
	 * 	- Check if the line is increasing or decreasing from left to right
	 * 	- if increasing, we take the right block and get its top right vertex coordinates (X0,Y0) and bottom right vertex (X0,Y1)
	 * 	- Then we calculate the intersect of the line(extended) and the right edge(extended) (X0,Y2), ; X0 since same y plane
	 * 		
	 * 			(X0,Y0)___/______
	 * 				  |  /		 |
	 * 				  | /   	 |
	 * 				  |/		 |
	 * 		   (X0,Y2)/			 |
	 * 				 /|__________|
	 * 				/(X0,Y1)
	 * 	
	 * 	- if at X0:  Y1 < Y2 < Y0  , that means the line intersects, It is in the boundary, We pick that **box**
	 * 	- This box directly correlate to the UnitBlock. We do an extra verification step to see if the **box** we got has a corresponding UnitBlock, If not we got a UnitBlock out of the arena
	 * 	  boundary. 
	 * 
	 * */

	public void getBoundary(ArrayList<double[]> edges, double latScale, double longScale){
		// find the max and min Lat and Long of the edges
		double maxLat, minLat, maxLong, minLong;

		System.out.println("Before: "+edges.size());
		// fix the edges
		fixEdges(edges);
		System.out.println("After: "+edges.size());

		// For each edge, we calculate the offset (how many blocks are to the left of starting point of the edge from -180)
		for(int i=0; i<edges.size();i++){
			System.out.println(edges.get(i)[0]+","+edges.get(i)[1]+","+edges.get(i)[2]+","+edges.get(i)[3]);
		}

	}

	public  ArrayList<Integer[]> coverLineToBlocks(double[] offset, double[] edge, double latScale, double longScale){
		/*
		 * Converts a given line to a set of blocks. sort of like pixelating using the above mentioned logic
		 * offsets variable holds the amount of blocks we cropped (from bottom left) to isolate the edge. When we are returning indices, we need to add this offset as well
		 * 
		 * We first take the LAT/LONG min/max values (offset) and the scale. We can then index UnitBlocks using integers rather than using the actual lat/long double values
		 * 
		 * Then we compute the line equation for the edge in <Lat> = M * <long> + C form ;  M = gradient : C = intersect
		 * We can easily substitute intersecting long values and check if the edge falls into a specific UnitBlock
		 * */
		ArrayList<Integer[]> blockIndeces = new ArrayList<Integer[]>();

		boolean increasing = (edge[3]-edge[1])>=0;	// if the edge is increasing i.e. m >=0  

		// Compute line equation parameters ( in the scaled version of the axis)
		double gradient = ((edge[3]-edge[1])/(edge[2]-edge[0]))*(longScale/latScale);			// we scale the gradient according to the lat/long scale
		double intersect = (edge[1]/latScale)-(gradient*(edge[0]/longScale));					// use 1st point to calculate intersect


		// first block calculation is and edge case, we do this outside the loop..
		// first block (0,0) is obviously picked
		// blockIndeces.add(0,(int)(offset[0]/longScale));
		// blockIndeces.add(1,(int)(offset[1]/latScale));


		System.out.println("Grad:"+gradient+"::: intersect:"+intersect);
		// (0,0) is always picked
		int lastDetectedY;
		int index=0;
		int longMargin = (int)((edge[2]-edge[0])/longScale); 	// # of blocks along long axis (width of array)
		int latMargin = (int)((edge[3]-edge[1])/latScale);		// # of blocks along lat axis (height of array)
		int currentY=0;
		// then we move through the line adding items
		System.out.println("Here we go");

		// starting point is a bit tricky. since it may not cross the initial x=0 line
		if(increasing){
			// add bottom left
			System.out.println("00:Adding => Long:"+index+"  Lat:"+currentY);
			blockIndeces.add(index,new Integer[]{index,currentY});
			lastDetectedY = 0;
		}else{
			// add top left
			latMargin*=-1;
			System.out.println("01:Adding => Long:"+index+"  Lat:"+latMargin);
			blockIndeces.add(index,new Integer[]{index,latMargin});
			lastDetectedY = latMargin;
		}

		for(index=1;index<=longMargin;index++){
			// substitute long to the line equation and add where it lands to the array
			currentY = (int)(((gradient*index)+intersect));
			System.out.println("02:Adding => Long:"+index+"  Lat:"+currentY);
			blockIndeces.add(index,new Integer[]{index,(int)(currentY)});
			//System.out.println("Lat:"+i+" Long:"+lastDetectedY);
			if(increasing){
				for(int k=lastDetectedY+1; k<=currentY;k++){
					//System.out.println("loop with lastDetectedY="+lastDetectedY+"   currentY="+currentY);
					// mark all the blocks
					/*
					 * This is easier to draw than explain so, draw the line connecting (0,0) and (1,6) a piece of paper and see the blocks it sweeps
					 * we can easily compute 0,0 and 1,6 then we have to connect everything from (0,1) to (0,5).. this is what this loop does
					 * 
					 * */				
					System.out.println("03:Adding => Long:"+(index-1)+"   Lat:"+k);
					blockIndeces.add(index,new Integer[]{(index-1),k});
				}
			}else{
				for(int k=lastDetectedY-1; k>=currentY;k--){
					//System.out.println("loop with lastDetectedY="+lastDetectedY+"   currentY="+currentY);
					// mark all the blocks
					/*
					 * This is easier to draw than explain so, draw the line connecting (0,0) and (1,6) a piece of paper and see the blocks it sweeps
					 * we can easily compute 0,0 and 1,6 then we have to connect everything from (0,1) to (0,5).. this is what this loop does
					 * 
					 * */				
					System.out.println("04:Loop Adding => Long:"+(index-1)+"   Lat:"+k);
					blockIndeces.add(index,new Integer[]{(index-1),k});
				}
			}
			lastDetectedY = currentY;
		}

		// The end point of the edge is also a bit tricky (we based the previous loop on its long axis steps. But the last point may not necessarily intersect with the last long line
		// We need to do this manually
		// Add the top left corner (or top bottom corner, if the line is decreasing) , then add everything inbetween the lastDetectedY to currentY

		if(increasing){
			// add the top right
			System.out.println("05:Adding => Long:"+longMargin+"  Lat:"+latMargin);
			currentY=latMargin;
			blockIndeces.add(index,new Integer[]{longMargin,latMargin});
			for(int k=lastDetectedY+1; k<currentY;k++){
				System.out.println("06:Adding => Long:"+longMargin+"   Lat:"+k);
				blockIndeces.add(index,new Integer[]{longMargin,k});
				//System.out.println("loop with lastDetectedY="+lastDetectedY+"   currentY="+currentY);
				// mark all the blocks
				/*
				 * This is easioeer to draw than explain so, draw the line connecting (0,0) and (1,6) a piece of paper and see the blocks it sweeps
				 * we can easily compute 0,0 and 1,6 then we have to connect everything from (0,1) to (0,5).. this is what this loop does
				 * 
				 * */				

			}
		}else{
			// bottom right
			System.out.println("07:Adding => Long:"+longMargin+"   Lat:"+0);
			blockIndeces.add(index,new Integer[]{0,latMargin});
			currentY=0;
			for(int k=lastDetectedY-1; k>currentY;k--){
				System.out.println("08:Adding => Long:"+longMargin+"   Lat:"+k);
				blockIndeces.add(index,new Integer[]{longMargin,k});
				//System.out.println("loop with lastDetectedY="+lastDetectedY+"   currentY="+currentY);
				// mark all the blocks
				/*
				 * This is easioeer to draw than explain so, draw the line connecting (0,0) and (1,6) a piece of paper and see the blocks it sweeps
				 * we can easily compute 0,0 and 1,6 then we have to connect everything from (0,1) to (0,5).. this is what this loop does
				 * 
				 * */				

			}
		}

		System.out.println("-------------------------------------");
		for(int i=0;i<blockIndeces.size();i++){
			System.out.print(blockIndeces.get(i)[0]+","+blockIndeces.get(i)[1]+",");
		}
		return null;
	}

	public void fixEdges(ArrayList<double[]> edges){
		/*
		 * This function has 2 purposes
		 * 1) Correct edge direction of the edge so that the points are ordered so the lines take the short path (and not the path around the world)
		 * 2) Split the edges that pass through - and 180(-180) to correct the axis issue due to wrap around
		 * 
		 * Longitude lines are wrapped around, i.e it goes from -180 to +180 with those two numbers meeting in one part of the globle (180 = -180) 
		 * This discontinuity makes things a whole lot complicated if an edge happens to cross the boundary
		 * Therefore what we do is to break the edge in to two segments if they cross this boundary
		 * 			170   175   180=-180  -175  -170
		 * 								
		 *  						|  /(-179,22)
		 *  						| /
		 *  						|/							(Long,Lat) Format to identify a point
		 *  				(180,21)+ (-180,21)
		 *  					   /|
		 * 						  /	|
		 * 				(179,20) /	|
		 *
		 * We transform the edge [(179, 20),(-179,22)]  ----- to 2 edges ----> [(179,20), (180,21)] and [(-179,22), (-180,21)] which are continuous
		 * This is a bit if an overhead so we can skip this step if the arena is far away from the discontinuity line
		 * 
		 * */
		int len = edges.size();
		for(int i=0;i<len;i++){
			// The line may cross the two critical long lines that are 0 and 180, we capture these cases and split the edge in to 2 edges

			double[] current = edges.get(i);	// edge = (double Long0, double Lat0, double Long1, double Lat1)			
			if(Math.abs(current[2] - current[0]) <= 180){		// Crossing the 0 degree longitude line
				double LongIntersectAt0=(current[1]*current[2] - current[0]*current[3])/(current[2]-current[0]);
				// New edge with from x0,y0 to intersect
				double[] temp = new double[]{current[0],current[1],0.0,LongIntersectAt0};			// -ve to 0
				// Old truncated edge from intersect to x1,yq										// 0 to +ve
				current[0]=0;
				current[1]=LongIntersectAt0;
				edges.add(temp);
			}
			else if(Math.abs(current[2] - current[0]) > 180){	// Crossing the 180 (-180) degree longitude line
				System.out.println(current[0]+","+current[1]+","+current[2]+","+current[3]);
				double LongIntersectAt180=0;

				// New edge with from x0,y0 to intersect
				if(current[0] >= 0){ // close to +180 side
					LongIntersectAt180=(180*(current[1]+current[3])+current[1]*current[2]-current[0]*current[3])/(360+current[2]-current[0]);
					double[] temp = new double[]{current[0],current[1],180.0,LongIntersectAt180}; 	// +ve to 180
					// Old truncated edge from x1,y1 to intersect									// -ve to -180 
					current[0]=-180.0;
					current[1]=LongIntersectAt180;
					edges.add(temp);
				}else{				// close to -180 side
					LongIntersectAt180=(180*(current[1]+current[3])-current[1]*current[2]+current[0]*current[3])/(360+current[0]-current[2]);
					double[] temp = new double[]{current[0],current[1],-180.0,LongIntersectAt180};	// -ve to -180
					// Old truncated edge from intersect to x1,y1									// +ve to 180
					current[0]=current[2];
					current[1]=current[3];
					current[2]=180.0;
					current[3]=LongIntersectAt180;
					edges.add(temp);
				}
			}
		}
	}
}
