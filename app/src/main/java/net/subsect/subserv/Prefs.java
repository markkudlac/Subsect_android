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
import android.widget.Toast;

import java.util.prefs.Preferences;

import static net.subsect.subserv.Const.*;


public class Prefs extends PreferenceFragment implements OnSharedPreferenceChangeListener{


    private static String newhost = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

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
        if (key.equals(this.getString(R.string.localnameserv))){
                Preference pref = findPreference(key);
                pref.setSummary(sharedPreferences.getString(key,""));
        }


    }


    public static String getHostname(Context context) {

        return( PreferenceManager
                .getDefaultSharedPreferences(context).getString(
                        PREF_HOSTNAME, context.getString(R.string.defaulthost))
        );
    }


    public static void setHostName(Context context, String hostnm) {

        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putString(PREF_HOSTNAME, hostnm).commit();

    }


    public static String getPassword(Context context) {

        return( PreferenceManager
                .getDefaultSharedPreferences(context).getString(
                        PREF_PASSWORD, "")
        );
    }


    public static void setPassword(Context context, String passwd) {

        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putString(PREF_PASSWORD, passwd).commit();

    }


    public static int getPassLength(Context context) {

        return( PreferenceManager
                .getDefaultSharedPreferences(context).getInt(
                        PREF_PASSLENGTH, 0)
        );
    }

    public static void setPassLength(Context context, int passlength) {

        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putInt(PREF_PASSLENGTH, passlength).commit();

    }



    public static String getToken(Context context) {

        return( PreferenceManager
                .getDefaultSharedPreferences(context).getString(
                        PREF_TOKEN, "6")
        );
    }


    public static void setToken(Context context, String token) {

        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putString(PREF_TOKEN, token).commit();

    }



    public static boolean connectSubsect(Context context){

        return(PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(
                        context.getString(R.string.connectserv), false)
                );
    }


    public static boolean pollServer(Context context){

        return(getHostname(context).equals(DEMO_NAME));
    }


    public static String getNameServer(Context context) {

        if (connectSubsect(context)) {
            return(context.getString(R.string.defRemoteServer));
        } else {
            return (PreferenceManager
                    .getDefaultSharedPreferences(context).getString(
                            context.getString(R.string.localnameserv),
                            "")
            );
        }
    }

}
