package resolver_test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

public class SimpleMultiThreadTest {
	// Make multiple simultaneous requests to the server to test the serverInternalData class's parallel capabilities
	// So we simultaneously
	// Register New Servlet
	// Register New Arena
	// Map Arenas To Servlets (Correcly and Incorreclty)
	// Add / Remove Active Users

	// TODO :
	// Once implemented test the removal of the above said stuff too


	public static final String baseURL="http://localhost:8080/Capture_Server/Resolver?requesttype=admin&adminname=malithmp&password=meh&action=";
	public void test(int numThreads, int overlapconst){
		// high overlapconst ==> move duplicate urls
		// lowest = 1 ; no overlaps
		// highelst = numThreads = almost all overlap except for first
		Thread[] threads = new Thread[numThreads];
		ArrayList<String> uniques1 = new ArrayList<String>();
		ArrayList<String> uniques2 = new ArrayList<String>();
		int threadsPerTest = numThreads/3;

		// Register New Servlets
		for(int i=0;i<threadsPerTest;i++){
			RegNewServlet regnews=new RegNewServlet("S"+i%(threadsPerTest/overlapconst));
			threads[i]=new Thread(regnews);								// First set of threads 0 to (n/3 -1)
			if(!uniques1.contains("S"+i%(threadsPerTest/overlapconst))){
				uniques1.add("S"+i%(threadsPerTest/overlapconst));
			}
		}
		System.out.println(uniques1.size());

		// Register New Arena Names
		for(int i=0;i<threadsPerTest;i++){
			RegNewArena regnewa=new RegNewArena("A"+i%(threadsPerTest/overlapconst));
			threads[threadsPerTest+i]=new Thread(regnewa);				// Second set of threads n/3 - (2n/3 -1)
			if(!uniques2.contains("A"+i%(threadsPerTest/overlapconst))){
				uniques2.add("A"+i%(threadsPerTest/overlapconst));
			}
		}
		System.out.println(uniques2.size());

		// MAP Arena to Servlets
		Random randomGenerator = new Random();
		for(int i=0;i<threadsPerTest;i++){
			int s = randomGenerator.nextInt(uniques1.size());
			int a = randomGenerator.nextInt(uniques2.size());
			MapServletArena map=new MapServletArena(uniques2.get(a),uniques1.get(s));
			
			threads[(2*threadsPerTest)+i]=new Thread(map);				// Second set of threads n/3 - (2n/3 -1)
			
			if(!uniques2.contains("A"+i%(threadsPerTest/overlapconst))){
				uniques2.add("A"+i%(threadsPerTest/overlapconst));
			}
		}
		System.out.println(uniques2.size());

		for(int i=0;i<numThreads;i++){
			threads[i].start();
		}


	}

	class RegNewServlet implements Runnable{
		//registerservlet
		String finalURL;
		RegNewServlet(String url){
			finalURL=baseURL+"registerservlet&URL="+url;
		}

		@Override
		public void run() {
			String inputLine;
			BufferedReader in;
			try{
				URL target = new URL(finalURL);
				URLConnection connection = target.openConnection();
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				while ((inputLine = in.readLine()) != null) 
					System.out.println(inputLine);


				in.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}	
	}

	class RegNewArena implements Runnable{
		String finalURL;

		RegNewArena(String arenaName){
			finalURL=baseURL+"registerarena&arena="+arenaName;
		}
		@Override
		public void run() {
			String inputLine;
			BufferedReader in;
			try{
				URL target = new URL(finalURL);
				URLConnection connection = target.openConnection();
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				while ((inputLine = in.readLine()) != null) 
					System.out.println(inputLine);


				in.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

	}

	class MapServletArena implements Runnable{
		String finalURL;

		MapServletArena(String arenaName,String servletName){
			finalURL=baseURL+"maparenaservlet&arena="+arenaName+"&servlet="+servletName;
		}
		@Override
		public void run() {
			String inputLine;
			BufferedReader in;
			try{
				URL target = new URL(finalURL);
				URLConnection connection = target.openConnection();
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				while ((inputLine = in.readLine()) != null) 
					System.out.println(inputLine);


				in.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
