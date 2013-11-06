package activities;

import network.NetworkTools;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.capture.candroid.R;

public class LoginPage extends Activity implements OnClickListener {

	private boolean developerMode = false;
	
	private EditText username;
	private EditText password;
	private ProgressBar pb;
	private Button btn;
	//private NetworkTools networktools;		// THERE MUST BE ONLY 1 INSTANCE OF THIS OBJECT TROUGHOUT THE ENTIRE APP. IT IS THREADSAFE(EACH NETWORK OPERATION IS ATOMIC)
	Context context;
	//public static File file;
	//FileWriter f_out;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_page);

		username = (EditText) findViewById(R.id.editText_Username);
		password = (EditText) findViewById(R.id.editText_password);
		pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(View.GONE);
		btn = (Button) findViewById(R.id.button_Login);
		btn.setOnClickListener(this);
		///networktools = new NetworkTools();
		//networktools.setURL("http://10.0.2.2", 8080,"/Capture_Server/Resolver");		// Dont need to set this if the default values are set up. I just put this here to let you know it exists
		//networktools.setURL("http://192.168.0.106", 8080,"/Capture_Server/Resolver");		// Dont need to set this if the default values are set up. I just put this here to let you know it exists
		//networktools.setURL("http://198.84.191.122", 8088,"/Capture_Server/Resolver");
		NetworkTools.getInstance().setURL("http://198.84.191.122", 8088,"/Capture_Server/Resolver");
		
		context = this;
		//File file = new File("file.txt");
		/*try {
			//f_out = new FileWriter(file,trcontextue);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login_page, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if(username.getText().toString().length()<1){
			Toast.makeText(this, "Please Enter a Username", Toast.LENGTH_LONG).show();
		}
		else if(password.getText().toString().length()<1){
			Toast.makeText(this, "Please Enter a Password", Toast.LENGTH_LONG).show();
		}
		else{
			pb.setVisibility(View.VISIBLE);
			new MyAsyncTask().execute(username.getText().toString(),password.getText().toString());	
		}
	}

	private class MyAsyncTask extends AsyncTask<String, Integer, Double>{

		private String received;
		private String error = null;
		String[] response;
		@Override
		protected Double doInBackground(String... params) {
			response = new String[2];
			
			//TODO decide whether we want boolean flag = networktools.signIn(..) since sign in returns true/false
			Log.i("Tag1"," - SignIn call");
			NetworkTools.getInstance().signIn(response, params[0], params[1]);
			Log.i("Tag1","after sign in = "+response[0]+" token = "+response[1]);
//			String url = "";
//			StringBuilder sb_url = new StringBuilder();
//
//			sb_url.append("http://198.84.191.122:8088/Capture_Server/Resolver?");
//			sb_url.append("requesttype=");
//			sb_url.append("user");
//			sb_url.append("&");
//			sb_url.append("loggedin=");
//			sb_url.append("false");
//			sb_url.append("&");
//			sb_url.append("request=");
//			sb_url.append("signin");
//			sb_url.append("&");
//			sb_url.append("username=");
//			sb_url.append(username.getText().toString());
//			sb_url.append("&");
//			sb_url.append("password=");
//			sb_url.append(password.getText().toString());
//
//			url = sb_url.toString();
//
//
//			//String url2 = "http://198.84.191.122:8088/Capture_Server/Resolver?requesttype=user&loggedin=false&request=signin&username="+username.getText().toString()+"&password=" + password.getText().toString(); 
//			//url = "";
//			//url = "http://www.example.com";
//
//
//			try {
//				//url = URLEncoder.encode(url,"UTF-8");
//
//				//System.out.println("URL = "+url);
//
//
//
//				//f_out.write(url2);
//				//f_out.close();
//
//				String initialize_url = "http://198.84.191.122:8088/Capture_Server/Resolver?requesttype=admin&adminname=malithmp&password=meh&action=tempinit";
//
//				String static_url = "http://www.example.com";
//				Log.i("test","hello");
//				HttpClient httpclient = new DefaultHttpClient();
//				HttpGet httpget = new HttpGet(url);
//				// Execute HTTP Post Request
//				//HttpResponse response = httpclient.execute(httppost);
//				ResponseHandler<String> responseHandler = new BasicResponseHandler();
//				received = httpclient.execute(httpget, responseHandler);
//				//HttpResponse response = httpclient.execute(httppost);
//
//			} catch (ClientProtocolException e) {
//				//TODO
//				error = e.getMessage();
//				cancel(true);
//				e.printStackTrace();
//			} catch (IOException e) {
//				//TODO
//				error = e.getMessage();
//				cancel(true);
//				e.printStackTrace();
//			}
//
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			pb.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(Double result) {
			if(developerMode)Toast.makeText(context, "->" + response[0]+"::"+response[1], Toast.LENGTH_LONG).show();
			
			pb.setVisibility(View.GONE);
			
			if(response[0].equals("true")){
				Toast.makeText(context,"Login Successful", Toast.LENGTH_LONG).show();
				//If status = true login successful then instead of message store Token in sharedPreferences.
				//Then direct to next page
			}
			else if(response[0].equals("false")){
				
				Toast.makeText(context, "Error: "+response[1],Toast.LENGTH_LONG).show();
			}
			else{
				
				if(developerMode) Toast.makeText(context, "Error: no status returned",Toast.LENGTH_LONG).show();
			}
			//Toast.makeText(getApplicationContext(), "command sent", Toast.LENGTH_SHORT).show();

			/*if (error != null){
				Toast.makeText(getApplicationContext(), "Error: "+error, Toast.LENGTH_LONG).show();
			}
			else{
				
				Toast.makeText(getApplicationContext(), "Message received: " + received, Toast.LENGTH_LONG).show();
			}	*/	
		}


	}

}
