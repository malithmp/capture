package resolver_test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

public class RegisterServletArenaTests {
	// Make multiple simultaneous requests to the server to test the serverInternalData class's parallel capabilities
	// Test 1) So we make multiple threads go through the full arena servlet map cycle correcly 1-to-1
	// Test 2) So we make multiple threads go through the full arena servlet map cycle incorrectly


	public String baseURL;

	public RegisterServletArenaTests(String base){
		this.baseURL = base+"requesttype=admin&adminname=malithmp&password=meh&request=";
	}
	
	public void test(int numThreads){
		
		Thread[] threads = new Thread[numThreads];
		
		// Test 1. Non Overlapping Arena and Servlets with correct 1 to 1 mapping
		for(int i=0;i<numThreads;i++){
			TestBundle test =new TestBundle("S"+i,"A"+i);
			threads[i]=new Thread(test);								// First set of threads 0 to (n/3 -1)
		}

		for(int i=0;i<numThreads;i++){
			threads[i].start();
		}


	}

	class TestBundle implements Runnable{
		// the following 3 strings are the URLs for the requests that are to be done parallelly
		String registerServlet;
		String registerArena;
		String MapArenaServlet;

		TestBundle(String servletUrl,String arenaName){
			registerServlet=baseURL+"registerservlet&URL="+servletUrl;
			registerArena=baseURL+"registerarena&arena="+arenaName;
			MapArenaServlet=baseURL+"maparenaservlet&arena="+arenaName+"&servlet="+servletUrl;
		}

		@Override
		public void run() {
			String inputLine;
			String returnCode="";
			BufferedReader in;
			try{
				// First register the servlet
				URL target = new URL(registerServlet);
				URLConnection connection = target.openConnection();
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				
				while ((inputLine = in.readLine()) != null) {
					returnCode=inputLine;
				}
				in.close();
				
				System.out.println("RS : " + returnCode);
				
				
				// Second register the arena name
				target = new URL(registerArena);
				connection = target.openConnection();
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while ((inputLine = in.readLine()) != null) {
					returnCode=inputLine;
				}
				in.close();
				
				System.out.println("RA : " + returnCode);
				
				
				// Third and Finally we map the two
				target = new URL(MapArenaServlet);
				connection = target.openConnection();
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while ((inputLine = in.readLine()) != null) {
					returnCode=inputLine;
				}
				in.close();
				
				System.out.println("MAS : " + returnCode);

				
			}catch(Exception e){
				e.printStackTrace();
			}
		}	
	}

}
