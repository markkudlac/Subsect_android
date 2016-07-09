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
        setSummary(getPreferenceScreen().getSharedPreferences(),
                this.getString(R.string.token));
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
                key.equals(this.getString(R.string.token))
            //     key.equals(this.getString(R.string.password))
                ) {
            Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals(this.getString(R.string.password))) {
         String passwd;

            passwd = sharedPreferences.getString(key,"");

            if (passwd.length() < 40 ) {
//                System.out.println("Passwd less than 40 host : " + sharedPreferences.getString("Hostname",""));
                passwd = Util.getSha1Hex(sharedPreferences.getString("Hostname","") + passwd);
                sharedPreferences.edit().putString(key,passwd).commit();
            }
        } else if (key.equals(this.getString(R.string.localnameserv))){
                Preference pref = findPreference(key);
                pref.setSummary(sharedPreferences.getString(key,
                        DEMO_ADDRESS + ":" + DEMO_PORT));
            }
    }


    public static String getHostname(Context context) {

        return( PreferenceManager
                .getDefaultSharedPreferences(context).getString(
                        context.getString(R.string.hostname), context.getString(R.string.defaulthost))
        );
    }


    public static String getPassword(Context context) {

        return( PreferenceManager
                .getDefaultSharedPreferences(context).getString(
                        context.getString(R.string.password), "")
        );
    }


    public static String getToken(Context context) {

        return( PreferenceManager
                .getDefaultSharedPreferences(context).getString(
                        context.getString(R.string.token), "6")
        );
    }


    public static boolean useHeroku(Context context){

        return(PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(
                        context.getString(R.string.heroku), false)
                );
    }


    public static boolean pollServer(Context context){

        return(PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(
                        context.getString(R.string.pollserver), false)
        );
    }



    public static String getNameServer(Context context) {

        if (useHeroku(context)) {
            return(context.getString(R.string.defRemoteServer));
        } else {
            return (PreferenceManager
                    .getDefaultSharedPreferences(context).getString(
                            context.getString(R.string.localnameserv),
                            DEMO_ADDRESS + ":" + DEMO_PORT)
            );
        }
    }
}
