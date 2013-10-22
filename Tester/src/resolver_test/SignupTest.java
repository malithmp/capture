package resolver_test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class SignupTest {
	// we register random users on the server and retrieve them simultaneously
	// Data must be sent as a JSON string of the following format
	// URL should follow the protocol
	// Payload is as follows
	// {"username":"user123","password":"pass123","firstname":"first123","lastname":"last123","home":"home123","email":"email123@institute.com"}

	public void test(int numThreads){

		Thread[] threads = new Thread[numThreads];
		// We create all user strings fisrt and then let all the threads do work..

		for(int i=0;i<numThreads;i++){
			UserSingup test =new UserSingup("User"+i,"Pass"+i,"First"+i,"Last"+i,"Home"+i,"Email"+i+"@utoronto.ca");
			threads[i]=new Thread(test);								// First set of threads 0 to (n/3 -1)
		}

		for(int i=0;i<numThreads;i++){
			threads[i].start();
		}


	}

	class UserSingup implements Runnable{
		// Do a httpPost
		//{"username":"user123","password":"pass123","firstname":"first123","lastname":"last123","home":"home123","email":"email123@institute.com"}
		String data;
		String baseURL="http://localhost:8080/Capture_Server/Resolver?requesttype=user&loggedin=false&request=signup";

		UserSingup(String user,String pass, String first, String last, String home, String email){
			try{
				JSONObject obj=new JSONObject();
				obj.put("username",user);
				obj.put("password",pass);
				obj.put("firstname",first);
				obj.put("lastname",last);
				obj.put("home",home);
				obj.put("email",email);
				data = obj.toString();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try{
				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response;
				HttpPost request = new HttpPost(baseURL);

				request.setEntity(new StringEntity(data));
				response = httpclient.execute(request);
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String serverResponseString = br.readLine();
				//System.out.println(serverResponseString);

				System.out.println("PASSED!");

			}catch(Exception e){
				e.printStackTrace();
			}
		}	
	}


}
