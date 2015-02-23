package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-02-16.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import static net.subsect.subserv.Const.*;


public class Prefs extends PreferenceFragment implements OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


    }



    public static String getUploadDir(Context context) {

        String xstr = PreferenceManager
                .getDefaultSharedPreferences(context).getString(
                        context.getString(R.string.uploadto), SYSHTML_DIR);
        return(xstr);
    }




    protected static String getuploaddir(Context context){

//		System.out.println("In getuploaddir");

        return("{\"dir\":\"" + getUploadDir(context) + "\"}");

    }

}
