package network;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;


// This class holds functions needed to perform network operations without any of the hassles
public class NetworkTools {
	// Functions in this class will return false if anything network related goes wrong
	// such as No Internet connection. All other errors will be assigned to a variable passed to the function
	// Only to be used by a single thread (worked thread). Well make the whole class synchronised anyway

	private static String SERVER_HOST="http://192.168.0.150";//remove this
	//private static String SERVER_HOST="http://10.0.2.2";
	private static String SERVER_PORT="8080";
	private static String SERVER_PATH="/Capture_Server/Resolver";
	private static String SERVER_USER_CONSTANT = "?requesttype=user&"; 								// the mobile app is only used by regular users 
	
	private String doGetResponse = new String();
	private String doPostResponse = new String();
	private static NetworkTools networktools;
	
	
	String url;
	HttpClient client;
	HttpGet request;
	HttpResponse httpResponse;

	
	public NetworkTools(){
		url = SERVER_HOST+":"+SERVER_PORT+SERVER_PATH+SERVER_USER_CONSTANT;
		client = new DefaultHttpClient();
		request = new HttpGet();
	}

	public static NetworkTools getInstance(){
		if(networktools == null){
			
			networktools = new NetworkTools();
		}
		return networktools;
		
		
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
				
		HttpGet request = new HttpGet();
		InputStream instream;
		//String error="Error",received="something";
		String finalUrl = url+"loggedin=false&request=signin&username="+username+"&password="+password;
		Log.i("Tag1","finalurl = "+finalUrl);
		
		
			
			Log.i("test","hello");
			if(doGET(finalUrl)){
				Log.i("Tag1","received in SignIn= "+doGetResponse);
				
				JSONObject jsonresponse = (JSONObject) JSONValue.parse(doGetResponse);
				response[0] = (String) jsonresponse.get("status");
				if(response[0].equals("true")){
					
					response[1] = (String) jsonresponse.get("token");
				}
				else{
					response[1] = (String) jsonresponse.get("message");
				}
				return true;
			}
			else{
				response[0] = "false";
				response[1] = "Error: Exception in doGet()";
				return false;
			}
			/*HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(finalUrl);
			// Execute HTTP Post Request
			//HttpResponse response = httpclient.execute(httppost);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			received = httpclient.execute(httpget, responseHandler);*/
			
			//HttpResponse response = httpclient.execute(httppost);

	}

	public synchronized boolean signUp(String[] response, String username,String emailAddress, String firstName, String lastName, String password){
		
		Log.i("Tag1"," in SignUp");
		/*
		 * Test values
		 * username = "User10";
		emailAddress = "user10@utoronto.ca";
		firstName = "First10";
		lastName = "Last10";
		password = "Pass10";*/
		
		JSONObject jsobj = new JSONObject();
		jsobj.put("username", username);
		jsobj.put("emailAddress", emailAddress);
		jsobj.put("firstName",firstName);
		jsobj.put("lastName", lastName);
		jsobj.put("password",password);
		
		String finalUrl = url+"loggedin=false&request=signup";
		//String post_url = "http://198.84.191.122:8088/Capture_Server/Resolver?requesttype=user&loggedin=false&request=signup";
		//String finalUrl = url+
		
		
		if(doPOST(finalUrl, jsobj)){
			
			JSONObject jsonresponse = (JSONObject) JSONValue.parse(doPostResponse);
			Log.i("SignUp"," jsonresponse = "+jsonresponse.toString());
			response[0] = (String) jsonresponse.get("status");
		
				
			if(response[0].equals("true")){
				response[1]=(String) jsonresponse.get("token");
				Log.d("Debug"," - SignUp server returned true");
			}
			else{
				response[1]=(String) jsonresponse.get("message");
				Log.d("Debug"," - SignUp server returned false: "+response[1]);
			}
			return true;
		}else{
			
			//Exception occurred in doPost;
			Log.d("Debug"," - Exception occurred in doPost: "+doPostResponse);	//TODO - check if received will contain response from doPost or just ""
			response[0] = "false";
			response[1] = "Exception occurred in doPost: "+doPostResponse;
			return false;
		}
		
		
		/*JSONObject jsobj = new JSONObject();
		jsobj.put("username", username);
		jsobj.put("emailAddress", emailAddress);
		jsobj.put("firstName",firstName);
		jsobj.put("lastName", lastName);
		jsobj.put("password",password);
		
		String post_url = "http://198.84.191.122:8088/Capture_Server/Resolver?requesttype=user&loggedin=false&request=signup";
		
		URL url;
		try {
			url = new URL(post_url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type","application/json");
			
			OutputStreamWriter outstream = new OutputStreamWriter(con.getOutputStream());
			outstream.write(URLEncoder.encode(jsobj.toString(),"UTF-8"));
			outstream.close();
			
			if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
				
				Log.d("Tag2","->HTTP_OK "+url.toString());
				
				
			}else{
				
				Log.d("Tag2","->HTTP_FAIL "+url.toString());
				
			}
			
			
			con.setRequestProperty("username", username);
			con.setRequestProperty("emailAddress", emailAddress);
			con.setRequestProperty("firstName",firstName);
			con.setRequestProperty("lastName", lastName);
			con.setRequestProperty("password",password);
			
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		
		
		
		return true;*/
	}
	
	
	public synchronized boolean testGet(){
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse res;
		//HttpGet httppost = new HttpGet("http://www.google.ca");
		HttpGet request = new HttpGet("http://198.84.191.122:8090/Capture_Server/Resolver?requesttype=admin&adminname=a&password=p&request=tempinit");
		//HttpGet request = new HttpGet("http://www.google.ca");
		Log.d("Tag1", "almost");

		try {
			res = httpclient.execute(request);
			Log.d("Tag1", ""+res.getStatusLine().getStatusCode());
			BufferedReader br = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
			Log.d("Tag1",br.readLine());
		} catch (ClientProtocolException e) {
			Log.d("Tag1", "1");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("Tag1", "2");
			e.printStackTrace();
		}
		return true; 
	}
	
	public synchronized boolean testPost(String data){
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse res;
		HttpPost request = new HttpPost("http://198.84.191.122:8090/Capture_Server/Resolver?requesttype=admin");
		Log.d("Tag1", "almost");
		
		try {
			request.setEntity(new StringEntity(data));
			res = httpclient.execute(request);
			Log.d("Tag1", ""+res.getStatusLine().getStatusCode());
			BufferedReader br = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
			Log.d("Tag1",br.readLine());
		} catch (ClientProtocolException e) {
			Log.d("Tag1", "1");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("Tag1", "2");
			e.printStackTrace();
		}
		return true; 
	}
	
	public boolean doGET(String url){
		
		Log.i("Tag1"," - doGET start");
		
		
		
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			doGetResponse = httpclient.execute(httpget, responseHandler);
			Log.i("Tag1"," - response in doGET = "+doGetResponse);
		} catch (ClientProtocolException e) {
			doGetResponse = "Error"+e.getMessage();
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			doGetResponse = "Error"+e.getMessage();
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	
	public boolean doPOST(String url, JSONObject jsobj){
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url); 
		try {
			httppost.setEntity(new StringEntity(jsobj.toJSONString()));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			doPostResponse = httpclient.execute(httppost, responseHandler);
			Log.i("doPost","response = "+doPostResponse);
		} catch (UnsupportedEncodingException e) {
			doPostResponse = "Error"+e.getMessage();
			e.printStackTrace();
			return false;
		} catch (ClientProtocolException e) {
			doPostResponse = "Error"+e.getMessage();
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			doPostResponse = "Error"+e.getMessage();
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
}