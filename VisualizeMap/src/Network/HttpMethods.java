package Network;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import Global.GlobalVar;

public class HttpMethods {

	public static String get(String[][] params) throws Exception{
		// get nx2 array array of key-value pairs
		// We add the stuff needed for the protocol manually (request type. app key..etc)
		// Finally return the String result obtained from the server
		// Since the map updates are run on a different thread (other than the UI thread) This  network operation will not cause the GUI to freeze.

		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(GlobalVar.SERVER);
		sb.append(":");
		sb.append(GlobalVar.PORT);
		sb.append(GlobalVar.PATH);
		if(params!=null){
			int numParams = params.length;
			for(int i=0;i<numParams;i++){
				if(params[i]!=null){
					System.out.println("adding query:"+params[i][0]+" = " +params[i][1]);
					sb.append("?");
					sb.append(params[i][0]);
					sb.append("=");
					sb.append(params[i][1]);
				}
			}
		}
		
		String finalurl = sb.toString();
		System.out.println("final URL = "+finalurl);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		HttpGet request = new HttpGet(finalurl);
		
		response = httpclient.execute(request);
		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String serverResponseString = br.readLine();
		System.out.println(serverResponseString);

		
		return serverResponseString;
	}

	
	public static String post(String[][] params,String data) throws Exception{
		// get nx2 array array of key-value pairs
		// get json string of the datastrcture we want to transfer
		// We add the stuff needed for the protocol manually (request type. app key..etc)
		
		// Manually set the first part of URL
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(GlobalVar.SERVER);
		sb.append(":");
		sb.append(GlobalVar.PORT);
		sb.append(GlobalVar.PATH);
		if(params!=null){
			int numParams = params.length;
			for(int i=0;i<numParams;i++){
				if(params[i]!=null){
					System.out.println("adding query:"+params[i][0]+" = " +params[i][1]);
					sb.append("?");
					sb.append(params[i][0]);
					sb.append("=");
					sb.append(params[i][1]);
				}
			}
		}
		String finalurl = sb.toString();
		System.out.println("final URL = "+finalurl);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		HttpPost request = new HttpPost(finalurl);
		
		request.setEntity(new StringEntity(data));
		response = httpclient.execute(request);
		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String serverResponseString = br.readLine();
		System.out.println(serverResponseString);

		return serverResponseString;

	}
}
