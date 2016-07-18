package net.subsect.subserv;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import static net.subsect.subserv.Const.API_PATH;
import static net.subsect.subserv.Const.BASE_BLOCKSIZE;
import static net.subsect.subserv.Const.HTTP_PROT;
import static net.subsect.subserv.Const.SERVEREMAIL;
import static net.subsect.subserv.Const.SOURCE_ADDRESS;

/**
 * Created by markkudlac on 16-07-14.
 */
public class HttpSetup extends AsyncTask<String, Void, String> {

    String func;

    public HttpSetup(String func){
        this.func = func;
    }

    @Override
    protected String doInBackground(String... xparam){

        HttpURLConnection con = null;
        String line, result = "";

        try {
            URL url;

            String xurl = API_PATH + xparam[0];
            System.out.println("HttpSetup xurl : "+xurl);

            if (Prefs.connectSubsect(MainActivity.globalactivity)) {
                url = new URL(HTTP_PROT, SOURCE_ADDRESS, xurl);
            } else {
                String[] hostaddr = Prefs.getNameServer(MainActivity.globalactivity).split(":");
                url = new URL(HTTP_PROT, hostaddr[0], Integer.parseInt(hostaddr[1]), xurl);
            }

            con = (HttpURLConnection) url.openConnection();

            InputStream xin = (InputStream) con.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(xin), BASE_BLOCKSIZE);

            while((line=reader.readLine())!=null){
                result+=line;
            }

            reader.close();


        } catch (Exception ex) {
            System.out.println("HttpStat Exception : " + ex);

        }

        finally {
            if (con != null) con.disconnect();
        }
        return(result);
    }


    @Override
    protected void onPostExecute(String result) {

        System.out.println("onPostExecute HttpSetup : " + result);

        if (result.length() > 0) {

            try {
                JSONObject jObj = new JSONObject(result);

                System.out.println("HttpSetup : "+func+" : " + jObj.getBoolean("rtn"));

                try {
                    Method meth;

                    if (func.equals(SERVEREMAIL)) {
                        meth = ServerActivity.class.getDeclaredMethod(func, String.class);
                        meth.invoke(null, jObj.getString("contact"));
                    } else {
                        meth = ServerActivity.class.getDeclaredMethod(func, boolean.class);
                        meth.invoke(null, jObj.getBoolean("rtn"));
                    }
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }
        }
        return;
    }
}
