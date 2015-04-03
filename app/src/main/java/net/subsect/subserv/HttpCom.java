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

public class HttpCom extends AsyncTask<String, Integer, String>{

    ConnectActivity conact;
    int filesize = 0;

    public HttpCom(ConnectActivity conact, int filesize) {
        this.conact = conact;
        this.filesize = filesize;
    }


    @Override
    protected String doInBackground(String... xparam){

        HttpURLConnection con = null;
        int buffsze = BASE_BLOCKSIZE*2;
        StringBuilder sb = new StringBuilder(buffsze);
     //   String line, result = "";

        try {
            int count = 0;
            int progcnt = 0;
            char data[] = new char[BASE_BLOCKSIZE];


            String xurl = API_PATH + xparam[0];
            System.out.println("HttpCom xurl : "+xurl);

         //   URL url = new URL(HTTP_PROT, SOURCE_ADDRESS, xurl);
              		URL url = new URL(HTTP_PROT, "192.168.1.103", 3000, xurl);
            con = (HttpURLConnection) url.openConnection();

            InputStream xin = (InputStream) con.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(xin), BASE_BLOCKSIZE);
/*
            while((line=reader.readLine())!=null){
                progcnt += line.length();
                publishProgress(progcnt);
                result+=line;
            }
*/
            int inc = 0;

            while((count=reader.read(data))!=-1){
                progcnt += count;
                if (progcnt / buffsze > inc ) {
                    publishProgress(progcnt);
                    inc = progcnt / buffsze;
                }
                sb= sb.append(data, 0, count);
            }

            reader.close();
            publishProgress(-1001);

        } catch (Exception ex) {
            System.out.println("Exception caught 1 : " + ex);
            publishProgress(-1);
        }

        finally {
            if (con != null) con.disconnect();
        }
        return(sb.toString());
    }


    protected void onProgressUpdate(Integer... progress) {

        if (progress[0] < 0) {
            conact.updateProg(progress[0]);
        } else {
            conact.updateProg(progress[0] / (filesize / 100 + 1));
        }
    }

    @Override
    protected void onPostExecute(String result) {

        System.out.println("onPostExecute HttpCom");

        if (result.length() > 0) {
            try {
//    	    System.out.println("onPostExecute result : " + result);

                String installdir = USR_DIR;

                JSONObject jObj =  new JSONObject(result);
   				System.out.println("Appname : "+ jObj.getString("appname")+"  dbtype : "+
                        jObj.getString("dbtype"));

                if (Util.copyBase64(conact, jObj.getString("zipfile"),
                        "/"+INSTALL_DIR+"/"+INSTALL_FILE)){

                    if (jObj.getString("dbtype").matches(DB_SYS)) installdir = SYS_DIR;
                    System.out.println("Install to : "+ installdir);
                    //Need to remove carriage returns put in by rails which muffs escape remove
                    // when doing json tostring. This is dumb and should be looked at later
                    String nocar_rtn_icon = jObj.getString("icon").replace("\n", "");
                    Util.installApp(conact,installdir ,INSTALL_FILE, nocar_rtn_icon,
                            jObj.getInt("id"));
                }
                conact.updateProg(-1002);
            }
            catch(JSONException ex) {
                conact.updateProg(-1);
                ex.printStackTrace();
            }
        }
        return;
    }

}

