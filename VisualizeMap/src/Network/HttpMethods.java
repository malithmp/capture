package Network;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

		DefaultHttpClient httpclient = new DefaultHttpClient();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(GlobalVar.SERVER+":"+GlobalVar.PORT).setPath(GlobalVar.PATH);
		// add query parameters
		if(params!=null){
			int numParams = params.length;
			for(int i=0;i<numParams;i++){
				if(params[i]!=null){
					System.out.println("adding query:"+params[i][0]+" = " +params[i][1]);
					builder.addParameter(params[i][0], params[i][1]);
					//query = query+params[i][0]+"="+params[i][1];
				}
			}
		}
		URI uri = builder.build();
		HttpGet httpget = new HttpGet(uri);

		System.out.println(httpget.getURI());

		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream instream = entity.getContent();
			try {
				// do something useful
			} finally {
				instream.close();
			}
		}

		return "";
	}

	public static String post(String[][] params,String data) throws Exception{
		// get nx2 array array of key-value pairs
		// get json string of the datastrcture we want to transfer
		// We add the stuff needed for the protocol manually (request type. app key..etc)
		DefaultHttpClient httpclient = new DefaultHttpClient();
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(GlobalVar.SERVER+":"+GlobalVar.PORT).setPath(GlobalVar.PATH);
		// add query parameters
		if(params!=null){
			int numParams = params.length;
			for(int i=0;i<numParams;i++){
				if(params[i]!=null){
					System.out.println("adding query:"+params[i][0]+" = " +params[i][1]);
					builder.addParameter(params[i][0], params[i][1]);
					//query = query+params[i][0]+"="+params[i][1];
				}
			}
		}
		URI uri = builder.build();
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setEntity(new StringEntity(data));
		HttpResponse response = httpclient.execute(httppost);
		
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream instream = entity.getContent();
			try {
				// do something useful
			} finally {
				instream.close();
			}
		}
		return "";
	}
}
