package tester;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import resolver_test.RegisterInstituteTest;
import resolver_test.RegisterServletArenaTests;
import resolver_test.SigninTest;
import resolver_test.SignupTest;

public class T_TesterMain {
	// only one of the following is used.
	static String localhostbase="http://localhost:8080/Capture_Server/Resolver?";	// when server is own host
	static String lanbase="http://192.168.0.150:8080/Capture_Server/Resolver?";		// when server is in the same lan

	static String base = localhostbase;	//<<<<<<< SET THIS

	public static void main(String[] args){
		preTestInit(base);

		// ----------------------------------------------------------
		//T_WebsocketTestSuit test1 = new T_WebsocketTestSuit();
		//test1.websocketTest1();
		// ----------------------------------------------------------

		// ----------------------------------------------------------
		RegisterServletArenaTests test1 = new RegisterServletArenaTests(base);
		test1.test(10);
		// ----------------------------------------------------------


		// ----------------------------------------------------------
		SignupTest test2 = new SignupTest(base);
		test2.test(10);
		// ----------------------------------------------------------

		// ----------------------------------------------------------
		RegisterInstituteTest test4 = new RegisterInstituteTest(base);
		test4.test();
		// ----------------------------------------------------------
	}

	public static void preTestInit(String base){
		// we need to setup server before tests
		String baseURL=base;
		String data="";
		try{
			JSONObject obj=new JSONObject();
			obj.put("requesttype", "admin");
			obj.put("loggedin", "true");
			obj.put("adminname", "malithmp");
			obj.put("token", "t1mm3y");
			obj.put("request", "tempinit");
			data = obj.toString();
		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			HttpPost request = new HttpPost(baseURL);

			request.setEntity(new StringEntity(data));
			response = httpclient.execute(request);
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String serverResponseString = br.readLine();
			//System.out.println(serverResponseString);
			System.out.println(serverResponseString);

		}catch(Exception e){
			e.printStackTrace();
		}

	}
}
