package activities;

import java.io.IOException;

import network.NetworkTools;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


import com.capture.candroid.R;
import com.capture.candroid.R.layout;
import com.capture.candroid.R.menu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SignUpPage extends Activity implements OnClickListener {
	
	private EditText username,email,firstName,lastName;
	private EditText password;
	private ProgressBar pb;
	private Button btn;
	private String token;
	
	boolean developerMode = true;

	Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up_page);
		
		username = (EditText) findViewById(R.id.editText_username);
		email = (EditText) findViewById(R.id.editText_email);
		firstName = (EditText) findViewById(R.id.editText_firstname);
		lastName = (EditText) findViewById(R.id.editText_lastname);
		password = (EditText) findViewById(R.id.editText_password);
		pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(View.GONE);
		btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(this);
		//networktools.setURL("http://10.0.2.2", 8080,"/Capture_Server/Resolver");		// Dont need to set this if the default values are set up. I just put this here to let you know it exists
		//networktools.setURL("http://192.168.0.106", 8080,"/Capture_Server/Resolver");		// Dont need to set this if the default values are set up. I just put this here to let you know it exists
		NetworkTools.getInstance().setURL("http://198.84.191.122", 8088,"/Capture_Server/Resolver");
		context = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up_page, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(username.getText().toString().length()<1){
			
			Toast.makeText(this, "Please Enter a Username", Toast.LENGTH_LONG).show();
		}
		else if(email.getText().toString().length()<1){
			
			Toast.makeText(this, "Please Enter an Email Address", Toast.LENGTH_LONG).show();
			
		}
		else if(password.getText().toString().length()<1){
			
			Toast.makeText(this, "Please Enter a Password", Toast.LENGTH_LONG).show();
		}
		else if(!checkUsername(username.getText().toString())){
			
			Toast.makeText(this, "Username must be of at least 5 characters long and without any special characters", Toast.LENGTH_LONG).show();
			
		}
		else if(!checkEmail(email.getText().toString())){
			
			Toast.makeText(this, "Please Enter a Valid Email Address", Toast.LENGTH_LONG).show();
			
		}
/*else if(!checkUsername(username.getText().toString())){
			
			Toast.makeText(this, "Please Enter a Valid Username without any special characters", Toast.LENGTH_LONG).show();
			
		}*/
		
		else{
			pb.setVisibility(View.VISIBLE);
			
			/*
			 * param 0 - username
			 * param 1 - email
			 * param 2 - firstName
			 * param 3 - lastName
			 * param 4 - password
			*/
			new MyAsyncTask().execute(username.getText().toString(),email.getText().toString(),firstName.getText().toString(),lastName.getText().toString(),password.getText().toString());	
			
			
		}
		
		
		
	}
	
	public boolean checkUsername(String username){
		if(username.matches("[a-zA-Z0-9.]{5,30}")){
			return true;
		}
		else
			return false;
	}
	
	
	public boolean checkEmail(String email){
		if(email.matches("[a-zA-Z0-9.]{1,30}@[a-zA-Z0-9]{1,10}.[a-zA-Z]{2,3}")){
			return true;
		}
		else
			return false;
	}
	
	private class MyAsyncTask extends AsyncTask<String, Integer, Double>{

		private String received;
		private String error = null;
		String[] response;
		
		@Override
		protected Double doInBackground(String... params) {
			/*
			 * param 0 - username
			 * param 1 - email
			 * param 2 - firstName
			 * param 3 - lastName
			 * param 4 - password
			*/
			response = new String[2];
			
				
				NetworkTools.getInstance().signUp(response, params[0],params[1],params[2],params[3],params[4]);
				Log.i("Tag1","after sign up = "+response[0]+" token = "+response[1]);
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			pb.setProgress(progress[0]);
		}

		protected void onPostExecute(Double result) {
			if(developerMode)Toast.makeText(context, "->" + response[0]+"::"+response[1], Toast.LENGTH_LONG).show();
			Log.i("postExecute"," Post Execute, response[0]="+response[0]+" response[1]="+response[1]);
			pb.setVisibility(View.GONE);
			
			if(response[0].equals("true")){
				Log.i("postExecute"," response is true");
				Toast.makeText(context,"Account Created Successfully", Toast.LENGTH_LONG).show();
				
				Intent intent=new Intent(SignUpPage.this,LoginPage.class);
				startActivity(intent);
				//If status = true login successful then instead of message store Token in sharedPreferences.
				//Then direct to next page
			}
			else if(response[0].equals("false")){
				Log.i("postExecute"," response is false");
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