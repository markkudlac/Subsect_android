package net.subsect.subserv;

import static net.subsect.subserv.Const.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by markkudlac on 15-05-01.
 */
public class HttpStat extends AsyncTask<String, Void, String> {

    MainActivity mainact;

    public HttpStat(MainActivity mainact) {
        this.mainact = mainact;

    }

    @Override
    protected String doInBackground(String... xparam){

        HttpURLConnection con = null;
        String line, result = "";

        try {
            URL url;

            String xurl = API_PATH + xparam[0];
            System.out.println("HttpCom xurl : "+xurl);

            url = new URL(HTTP_PROT, SOURCE_ADDRESS, xurl);

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

        System.out.println("onPostExecute HttpStat : " + result);

        if (result.length() > 0) {

            try {
                JSONObject jObj = new JSONObject(result);

                if (jObj.getBoolean("rtn")){
                    String action = jObj.getString("action");
                    System.out.println("action HttpStat : " + action);

                    if (action.equals("reset")) {
                        System.out.println("RESET server");
                        mainact.loadServer(mainact);
                    }
                    /*
                    else {
                        if (ConnectActivity.getConAct() != null){
                            ConnectActivity.getConAct().loadConnect(action);
                        }
                    }
                    */
                }
            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }
        }
        return;
    }

}
