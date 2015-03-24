package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-03-23.
 */

import static net.subsect.subserv.Const.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

public class HttpCom extends AsyncTask<String, Void, String>{

    Context context;
    String funcHandle;

    public HttpCom(Context context) {
        this.context = context;
        this.funcHandle = funcHandle;
    }


    @Override
    protected String doInBackground(String... xparam){

        HttpURLConnection con = null;
        String line, result = "";

        try {

            String xurl = API_PATH + xparam[0];
            System.out.println("HttpCom xurl : "+xurl);

      //      URL url = new URL(HTTP_PROT, SOURCE_ADDRESS, xurl);
              		URL url = new URL(HTTP_PROT, "192.168.1.103", 3000, xurl);
            con = (HttpURLConnection) url.openConnection();

            InputStream xin = (InputStream) con.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(xin));

            while((line=reader.readLine())!=null){
                result+=line;
            }

        } catch (Exception ex) {
            System.out.println("Exception caught 1 : " + ex);
        }

        finally {
            if (con != null) con.disconnect();
        }
        return(result);
    }


    @Override
    protected void onPostExecute(String result) {

        System.out.println("onPostExecute HttpCom");

        if (result.length() > 0) {
            try {
  //              if (result.charAt(0) == '{'){
//    					 System.out.println("In matches");
 //                   result = "[" + result +"]";
 //               }
//    	    System.out.println("onPostExecute result : " + result);

                JSONObject jObj =  new JSONObject(result);
   				System.out.println("Appname : "+ jObj.getString("appname"));

  /*              try {
                    Method meth = SQLHelper.class.getDeclaredMethod(funcHandle, JSONArray.class, Context.class);
                    meth.invoke(null, jArray, context);
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                } */

            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }
        }
        return;
    }

}

