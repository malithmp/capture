package activities;

import com.capture.candroid.R;

import network.NetworkTools;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class SplashScreen extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		// This is the splash screen. We can put our backdoor buttons here
		// Also this canbe used as an actual splash screen if needed.. While checking internet connection, GPS lock status, yada yada yada
		// or we can use this as the login screen in the final version
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}
	
	public void buttonAction(View view){
		NetworkTools nt = new NetworkTools();
		Thread t = new Thread(new TR(nt,this));
		t.start();
	}

}

class TR implements Runnable{
	NetworkTools nt;
	Context context;
	TR(NetworkTools nt,Context context){
		this.nt = nt;
		this.context=context;
	}
	public void run(){
		String[] response = new String[2];
		boolean status = nt.signIn(response, "malithr","pass1");
		if(status){
			// No network error occured. safe to check if the signin passed
			if(response[0].equals("true")){
				// Auth passed. We have the token in response[1]
			}
			else{
				// AUth failed
			}
		}
		else{
			// Network failed
		}
	}
}
