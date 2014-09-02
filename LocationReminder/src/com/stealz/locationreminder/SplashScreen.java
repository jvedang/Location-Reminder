package com.stealz.locationreminder;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class SplashScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
		Handler handler = new Handler(); 
		 handler.postDelayed(
				 new Runnable() 
		 { 
		         public void run()
		         { 
		        	 
		              Intent i = new Intent(SplashScreen.this,HomeActivity.class);
		              finish();
		              startActivity(i);
		         } 
		 }, 2000); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}

}
