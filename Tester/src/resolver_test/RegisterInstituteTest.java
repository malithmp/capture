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

import resolver_test.SignupTest.UserSingup;

public class RegisterInstituteTest {
	// we register random users on the server and retrieve them simultaneously
	// Data must be sent as a JSON string of the following format
	// URL should follow the protocol
	// Payload is as follows
	// {"username":"user123","password":"pass123","firstname":"first123","lastname":"last123","home":"home123","email":"email123@institute.com"}
	String baseURL;

	public RegisterInstituteTest(String base) {
		this.baseURL = base;
	}
	
	public void test(){
		Thread[] threads = new Thread[4];
		ArrayList<Integer[]> points = new ArrayList<Integer[]>();
		
		points.add(new Integer[]{1,1});
		points.add(new Integer[]{2,2});
		points.add(new Integer[]{3,3});
		points.add(new Integer[]{4,4});
		points.add(new Integer[]{5,5});
		points.add(new Integer[]{6,6});
		points.add(new Integer[]{7,7});
		points.add(new Integer[]{8,8});
		points.add(new Integer[]{9,9});
		points.add(new Integer[]{10,10});
		// We create all user strings fisrt and then let all the threads do work..

//		for(int i=0;i<numThreads;i++){
//			UserSingup test =new UserSingup("User"+i,"Pass"+i,"First"+i,"Last"+i,"Home"+i,"Email"+i+doms.get(i%4));
//			threads[i]=new Thread(test);								// First set of threads 0 to (n/3 -1)
//		}
//
//		for(int i=0;i<numThreads;i++){
//			threads[i].start();
//		}
		
		RegisterInstitute test =new RegisterInstitute("UofTT","utorntoto.ca",new Integer[][]{points.get(7),points.get(0),points.get(7),points.get(5)});
		threads[0]=new Thread(test);
		
		RegisterInstitute test1 =new RegisterInstitute("Ryersson","ryersson.ca",new Integer[][]{points.get(1),points.get(2),points.get(3),points.get(4)});
		threads[1]=new Thread(test1);
		
		RegisterInstitute test2 =new RegisterInstitute("Eggyolk","egyolk.ca",new Integer[][]{points.get(9),points.get(0),points.get(7),points.get(2)});
		threads[2]=new Thread(test2);
		
		RegisterInstitute test3 =new RegisterInstitute("Waterpoo","uwaterpoo.ca",new Integer[][]{points.get(1),points.get(2),points.get(5),points.get(6)});
		threads[3]=new Thread(test3);
		
		threads[0].start();
		threads[1].start();
		threads[2].start();
		threads[3].start();

	}

	class RegisterInstitute implements Runnable{
		// Do a httpPost
		//{"username":"user123","password":"pass123","firstname":"first123","lastname":"last123","home":"home123","email":"email123@institute.com"}
		String data;
		//String finalURL;
		
		RegisterInstitute(String institutename,String instituteDomain, Integer[][] coordinates){
			//finalURL = baseURL+"institutename="+institutename+"&institutedomain="+instituteDomain;
			//System.out.println(finalURL);
			try{
				JSONObject obj=new JSONObject();
				
				
				for(int i=0;i<coordinates.length;i++){
					obj.put(Integer.toString(i),"("+coordinates[i][0]+","+coordinates[i][1]+")");
				}
				data = obj.toString();			// data package
				
				obj=new JSONObject();
				obj.put("requesttype", "admin");
				obj.put("loggedin", "true");
				obj.put("adminname", "malithmp");
				obj.put("token", "t1mm3y");
				obj.put("request", "registerinstitute");
				obj.put("institutename", institutename);
				obj.put("institutedomain", instituteDomain);
				obj.put("rawdata", data);
				
				data=obj.toString();
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
				System.out.println("RI : "+serverResponseString);

			}catch(Exception e){
				e.printStackTrace();
			}
		}	
	}

}
