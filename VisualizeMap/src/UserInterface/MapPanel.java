package UserInterface;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import Global.GlobalVar;

public class MapPanel extends JPanel {
	// All the tools to draw the map (for debugging purposes only)
	// 1- draw paths from arrays of (x,y) coordinates
	// 2- draw points of coordinates
	// TODO
	
	
	int unitsPerSide;							// Hight = Width = unitsPerSide. Map can be scaled to the inverse of this (1/unitsPerSide)
	int borderXTop,borderYTop;					// Margins for map area
	
	public MapPanel() {
		init();
	}
	
	public void init(){
		// initialize ALL the things!!!
		unitsPerSide=100;		// lets start by showing a 100x100 block.. user gets to zoom in/out
	}
	
	public void drawPoint(int x, int y, int size , Color color){
		Graphics g = getGraphics();
        g.setColor(color);
        // y axis needs to be inverted sinve its upside down by default
        y = GlobalVar.MAP_STARTY+GlobalVar.MAP_HEIGHT - y;
        g.fillOval(x, y, size, size);
	}

	public void drawCurve(int[][] path, int size , Color color){
		//get an array of form int [n][2] {(x1,y1),(x2,y2),(x3,y3)}
		int numPoints = path.length;
		if(numPoints <= 1){
			System.out.println("Err: Only 1 point provided.");
			return;
		}
		
		Graphics2D g = (Graphics2D) getGraphics();
		// get the first Point. connect second to first by line.. set previous to 2nd.. continue until its the last
		int[] previousPoint = path[0]; 	
		int  invertionOfset = GlobalVar.MAP_STARTY+GlobalVar.MAP_HEIGHT;
		for(int i=1;i<numPoints;i++){
			int[] currentPoint = path[i];
			//connect adgescent points with lines
			g.setColor(color);
			g.setStroke(new BasicStroke(size));
			
			g.drawLine(previousPoint[0], invertionOfset-previousPoint[1], currentPoint[0], invertionOfset-currentPoint[1]);
			previousPoint = currentPoint;
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setBackground(Color.BLUE);
	}
}
