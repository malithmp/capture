package resolver_test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;

public class MultiThreadDataStoreTests {
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
		ArrayList<Integer> uniques = new ArrayList<Integer>();
		
		for(int i=0;i<numThreads;i++){
			RegNewServlet regnew=new RegNewServlet("t"+i%(numThreads/overlapconst));
			threads[i]=new Thread(regnew);
			if(!uniques.contains(i%(numThreads/overlapconst))){
				uniques.add(i%(numThreads/overlapconst));
			}
		}
		System.out.println(uniques.size());
		for(int i=0;i<numThreads;i++){
			threads[i].start();
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
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

		@Override
		public void run() {


		}

	}

	class MapServletArena implements Runnable{

		@Override
		public void run() {


		}

	}

	class AddUser implements Runnable{

		@Override
		public void run() {


		}

	}

	class RemoveUser implements Runnable{

		@Override
		public void run() {


		}

	}
}
