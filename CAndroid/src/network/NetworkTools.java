package network;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;


// This class holds functions needed to perform network operations without any of the hassles
public class NetworkTools {
	// Functions in this class will return false if anything network related goes wrong
	// such as No Internet connection. All other errors will be assigned to a variable passed to the function
	// Only to be used by a single thread (worked thread). Well make the whole class synchronised anyway

	private static String SERVER_HOST="http://10.0.2.2";
	private static String SERVER_PORT="8080";
	private static String SERVER_PATH="/Capture_Server/Resolver";
	private static String SERVER_USER_CONSTANT = "?requesttype=user&"; 								// the mobile app is only used by regular users 

	String url;
	HttpClient client;
	HttpGet request;
	HttpResponse httpResponse;

	public NetworkTools(){
		url = SERVER_HOST+":"+SERVER_PORT+SERVER_PATH+SERVER_USER_CONSTANT;
		client = new DefaultHttpClient();
		request = new HttpGet();
	}

	public synchronized void setURL(String host,int port,String path){
		SERVER_HOST = host;
		SERVER_PORT = Integer.toString(port); 
		SERVER_PATH = path;
		url = SERVER_HOST+":"+SERVER_PORT+SERVER_PATH+SERVER_USER_CONSTANT;
	}

	// ------------------------------ SERVER CALLS ------------------------------
	// We hit the URL and if an exception occur, we return false and post a hint about the error in response. (Non serverside errors
	// If we get a response from the server we set the response accordingly
	// Note that the first parameter if the response is ALWAYS the status variable from the server (true/false) and this is not to be mistaken with the return type of this method
	// The return type of this method indicate network errors and the response[0] represents any errors occured at server side. But se set the error mesage in the response[1] regardless



	public synchronized boolean signIn(String[] response, String username, String password){
		//HttpGet request = new HttpGet();
		URI uri;
		InputStream instream;
		String finalUrl = url+"loggedin=false&request=signin&username="+username+"&password="+password;
		//finalUrl = "http://www.google.ca/?gws_rd=cr&ei=udZiUuegA6PmyQHO5IGQBg";
		try {
			URL url = new URL(finalUrl);
			Log.d("Tag1","->"+url.toString());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			//con.setRequestProperty("User-Agent", USER_AGENT);
			
			int responseCode = con.getResponseCode();
			instream = con.getInputStream();
			
			try {
				Log.d("Tag1",""+instream.available());
				byte []reply = new byte[instream.available()];
				instream.read(reply);
				String jsonResponse = new String(reply);
				// we have the response from the server as a json string
				Log.d("Tag1","-->"+jsonResponse);
				JSONObject jObj = (JSONObject) JSONValue.parse(jsonResponse);
				response[0]=(String) jObj.get("status");
				response[1]=(String) jObj.get("message");
				//TODO : set the remaining stuff
			} finally{
				instream.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			response[0]="false";
			//TODO set the exception message to response[1]
			return false;
		}
		return true; 
	}
}