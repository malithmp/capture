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
	public static void main(String[] args){
		preTestInit();
		
		// ----------------------------------------------------------
		//T_WebsocketTestSuit test1 = new T_WebsocketTestSuit();
		//test1.websocketTest1();
		// ----------------------------------------------------------
		
		// ----------------------------------------------------------
		//RegisterServletArenaTests test1 = new RegisterServletArenaTests();
		//test1.test(1);
		// ----------------------------------------------------------
		
		
		// ----------------------------------------------------------
		SignupTest test2 = new SignupTest();
		test2.test(1);
		// ----------------------------------------------------------
	}
	
	public static void preTestInit(){
		// we need to setup server before tests
		String baseURL="http://localhost:8080/Capture_Server/Resolver?requesttype=admin&adminname=malithmp&password=meh&request=tempinit";
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
