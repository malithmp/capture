package resolver_test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class RegisterServletArenaTests {
	// Make multiple simultaneous requests to the server to test the serverInternalData class's parallel capabilities
	// Test 1) So we make multiple threads go through the full arena servlet map cycle correcly 1-to-1
	// Test 2) So we make multiple threads go through the full arena servlet map cycle incorrectly


	public String baseURL;
	
	public RegisterServletArenaTests(String base){
		this.baseURL = base;
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
		String mapArenaServlet;

		TestBundle(String servletUrl,String arenaName){
			try{
				JSONObject obj1=new JSONObject();
				obj1.put("requesttype", "admin");
				obj1.put("loggedin", "true");
				obj1.put("adminname", "malithmp");
				obj1.put("token", "t1mm3y");
				obj1.put("request", "registerarena");
				obj1.put("arena", arenaName);
				
				
				JSONObject obj2 =new JSONObject();
				obj2.put("requesttype", "admin");
				obj2.put("loggedin", "true");
				obj2.put("adminname", "malithmp");
				obj2.put("token", "t1mm3y");
				obj2.put("request", "registerservlet");
				obj2.put("url", servletUrl);
				
				JSONObject obj3 =new JSONObject();
				obj3.put("requesttype", "admin");
				obj3.put("loggedin", "true");
				obj3.put("adminname", "malithmp");
				obj3.put("token", "t1mm3y");
				obj3.put("request", "maparenaservlet");
				obj3.put("servlet", servletUrl);
				obj3.put("arena", arenaName);
				

				registerArena	= obj1.toString();
				registerServlet = obj2.toString();
				mapArenaServlet = obj3.toString();
				
				//registerServlet=baseURL+"registerservlet&URL="+servletUrl;
				//registerArena=baseURL+"registerarena&arena="+arenaName;
				//MapArenaServlet=baseURL+"maparenaservlet&arena="+arenaName+"&servlet="+servletUrl;
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try{
				// First register the servlet
				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response;
				HttpPost request = new HttpPost(baseURL);

				request.setEntity(new StringEntity(registerServlet));
				response = httpclient.execute(request);
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String serverResponseString = br.readLine();
				//System.out.println(serverResponseString);
				System.out.println("RS : " + serverResponseString);
				
				// Second register the arena name
				httpclient = new DefaultHttpClient();
				request = new HttpPost(baseURL);

				request.setEntity(new StringEntity(registerArena));
				response = httpclient.execute(request);
				br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				serverResponseString = br.readLine();
				//System.out.println(serverResponseString);
				System.out.println("RA : " + serverResponseString);
				
				
				// Third and Finally we map the two
				httpclient = new DefaultHttpClient();
				request = new HttpPost(baseURL);

				request.setEntity(new StringEntity(mapArenaServlet));
				response = httpclient.execute(request);
				br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				serverResponseString = br.readLine();
				//System.out.println(serverResponseString);
				System.out.println("MAS : " + serverResponseString);
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}	
	}

}
