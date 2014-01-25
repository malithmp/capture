package logicUnit;

import java.util.ArrayList;

import tools.BoundaryBlockGenerator;

public class WorkerUnitFrontend {
	
	public static void main(String[] args){
		double[] offset = {0.0,0.0,.1,.1};
		double[] edge = {.01,.01,.05,.09};
		//edge = new double[]{.01,.09,.011,.01};
		//edge = new double[]{.01,.01,.09,.01};
		//double latScale=.02;
		//double longScale=.02;
		
		// big stuff for visualizing (20,50),(80,160)
		//edge = new double[]{2,8,58,108};
		//latScale=10.0;
		//longScale=10.0;
		
		//BoundaryBlockGenerator bbg = new BoundaryBlockGenerator();
		//bbg.coverLineToBlocks(offset, edge,latScale,longScale);
		
		
		ArrayList<double[]> edges = new ArrayList<double[]>();
		edges.add(new double[]{-161.0,12.0,51.0,65.0});
		
		double latScale=10.0;
		double longScale=10.0;
		
		BoundaryBlockGenerator bbg = new BoundaryBlockGenerator();
		bbg.getBoundary(edges, latScale, longScale);
	}
	
//	public static void main(String[] args){
//		ArrayList<double[]> edges = new ArrayList<double[]>();
//		edges.add(new double[]{-144.91040069839,-133.63332060801,94.148085990058,-86.60629493492});
//		
//		BoundaryBlockGenerator bbg = new BoundaryBlockGenerator();
//		bbg.fixEdges(edges);
//		
//		for(int i=0;i<edges.size();i++){
//			System.out.println(edges.get(i)[0]+","+edges.get(i)[1]+","+edges.get(i)[2]+","+edges.get(i)[3]);
//		}
//	}
//	
//	private void meh(){
//		// Serves multiple arenas
//
//		ArrayList<double[]> edges = new ArrayList<double[]>();
//		double[] random = new double[]{
//				-144.91040069839,
//				-133.63332060801,
//				94.148085990058,
//				-86.60629493492,
//				-84.384096667349,
//				177.6839800727,
//				-64.456877915401,
//				137.36773507547,
//				27.930727530239,
//				-179.72662087517,
//				-25.582387319572,
//				-35.569721504846,
//				131.6478386855,
//				-120.77976143955,
//				159.49111505388,
//				10.006486079659,
//				-27.274163666775,
//				154.85411378315,
//				-145.73550183593,
//				144.44398198484,
//		};
//		int bleh = random.length;
//		for(int i =0; i< bleh;i-=2){
//			System.out.println(i);
//			edges.add(new double[]{random[i++%bleh],random[i++%bleh],random[i++%bleh],random[i++%bleh]});
//		}
//
//		for(int i=0;i<edges.size();i++){
//			System.out.println(edges.get(i)[0]+","+edges.get(i)[1]+","+edges.get(i)[2]+","+edges.get(i)[3]+",");
//		}
//
//	}
}



