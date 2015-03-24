package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-02-16.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.prefs.Preferences;

import static net.subsect.subserv.Const.*;


public class Prefs extends PreferenceFragment implements OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        setSummary(getPreferenceScreen().getSharedPreferences(),
                this.getString(R.string.hostname));
        setSummary(getPreferenceScreen().getSharedPreferences(),
                this.getString(R.string.localnameserv));
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

        setSummary(sharedPreferences, key);
    }


    // This is stupid and should be looked at later
    private void setSummary(SharedPreferences sharedPreferences, String key) {

     //   System.out.println("In Pref 2 changd : " +key);
            if (key.equals(this.getString(R.string.hostname)) ||
                    key.equals(this.getString(R.string.localnameserv))) {
                Preference pref = findPreference(key);
                pref.setSummary(sharedPreferences.getString(key, ""));
            }
    }


    public static String getUploadDir(Context context) {

        return( PreferenceManager
                .getDefaultSharedPreferences(context).getString(
                        context.getString(R.string.uploadto), INSTALL_DIR)
        );
    }


    protected static String getuploaddir(Context context){

//		System.out.println("In getuploaddir");
        return("{\"dir\":\"" + getUploadDir(context) + "\"}");
    }


    public static String getHostname(Context context) {

        return( PreferenceManager
                .getDefaultSharedPreferences(context).getString(
                        context.getString(R.string.hostname), context.getString(R.string.defaulthost))
        );
    }


    public static String getNameServer(Context context) {

        if (PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(
                        context.getString(R.string.heroku), false)
                ) {
            return(context.getString(R.string.defRemoteServer));
        } else {
            return (PreferenceManager
                    .getDefaultSharedPreferences(context).getString(
                            context.getString(R.string.localnameserv),
                            context.getString(R.string.defLocServer))
            );
        }
    }
}
