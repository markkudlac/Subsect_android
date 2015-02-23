package net.subsect.subserv;

import android.app.Activity;
import android.os.Bundle;


public class SettingsActivity extends Activity {

	 
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	    //    System.out.println("In Settings Activity");

	        // Display the fragment as the main content.
	        getFragmentManager().beginTransaction()
	                .replace(android.R.id.content, new Prefs())
	                .commit();
	    }
}
