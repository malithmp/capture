package tester;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
		test1.test(3);
		// ----------------------------------------------------------
		
		
		// ----------------------------------------------------------
		SignupTest test2 = new SignupTest(base);
		test2.test(99);
		// ----------------------------------------------------------
	}
	
	public static void preTestInit(String base){
		// we need to setup server before tests
		String baseURL=base+"requesttype=admin&adminname=malithmp&password=meh&request=tempinit";
		URL target;
		try {
			target = new URL(baseURL);
			URLConnection connection = target.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
