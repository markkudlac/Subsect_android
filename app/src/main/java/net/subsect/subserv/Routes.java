package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-02-16.
 */
import static net.subsect.subserv.Const.*;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;



public class Routes {

    public static String callMethods(String uri, String qryString, Context context, File rootdir){

        JSONObject qryJSON;

        String msg = Util.stringJA(Util.JSONdbReturn(false, -1, ""));


      //  System.out.println("Route : "+uri + " qryString : "+qryString);

        uri = trimUri(uri,API_PATH);

        if (uri.indexOf(API_SAVEFILE) == 0){

            msg = Util.JSONReturn(false);

            qryJSON = Util.qryStringToJSON(qryString);
            try {
                String fullpath = rootdir.getPath()+qryJSON.getString("filename");
                msg = Util.savefile(fullpath,
                        Util.decodeJSuriComp(qryJSON.getString("filecontent")));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (uri.indexOf(API_DELETEFILE) == 0){

            msg = Util.JSONReturn(false);

            qryJSON = Util.qryStringToJSON(qryString);
            try {
                String fullpath = rootdir.getPath()+qryJSON.getString("filename");
                System.out.println("Route : "+uri + " fullpath  2 : "+fullpath);
                msg = Util.deletefile(fullpath);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (uri.indexOf(API_INSERTDB) == 0) {

            qryJSON = Util.qryStringToJSON(qryString);

            secureIn: try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString(ARGS_SQLPK));

                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_vals = jsob.getJSONObject(ARGS_VALUES);

                String table = jsob.getString(ARGS_TABLE);
                SQLHelper targetdb = SQLManager.getSQLHelper(jsob.getString(ARGS_DB));
               // System.out.println("insertDB password : " + jsob.getString("password"));

                int secure = targetdb.checkSecure(table);

                if (secure < 0){
                    break secureIn;
                } else if (secure == 0 ||
                        jsob.getString(ARGS_PASSWORD).equals(Prefs.getPassword(context))){
                    msg = Util.stringJA(targetdb.
                            insertDB(table, jsob_vals, jsob.getString(ARGS_FUNCID)));
                } else {
                    msg = Util.stringJA(Util.JSONdbReturn(false, FAIL_PASSWORD,
                            jsob.getString(ARGS_FUNCID)));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_QUERYDB) == 0){

            qryJSON = Util.qryStringToJSON(qryString);

            try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString(ARGS_SQLPK));
                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_args, jsob_limits;

                jsob_args = jsob.getJSONObject(ARGS_ARGS);
                jsob_limits = jsob.getJSONObject("limits");

                msg = Util.stringJA(SQLManager.getSQLHelper(jsob.getString(ARGS_DB)).
                        queryDB(jsob.getString(ARGS_QSTR), jsob_args, jsob_limits,
                                jsob.getString(ARGS_FUNCID)));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_UPDATEDB) == 0){

            qryJSON = Util.qryStringToJSON(qryString);

            secureUp: try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString(ARGS_SQLPK));
                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_args, jsob_values;

                jsob_values = jsob.getJSONObject(ARGS_VALUES);
                jsob_args = jsob.getJSONObject(ARGS_ARGS);
                String table = jsob.getString(ARGS_TABLE);
                SQLHelper targetdb = SQLManager.getSQLHelper(jsob.getString(ARGS_DB));

                int secure = targetdb.checkSecure(table);

                if (secure < 0){
                    break secureUp;
                } else if (secure == 0 ||
                        jsob.getString(ARGS_PASSWORD).equals(Prefs.getPassword(context))){

                    msg = Util.stringJA(targetdb.updateDB(table,
                                jsob_values, jsob.getString(ARGS_QSTR), jsob_args,
                                jsob.getString(ARGS_FUNCID)));
                } else {
                    msg = Util.stringJA(Util.JSONdbReturn(false, FAIL_PASSWORD,
                            jsob.getString(ARGS_FUNCID)));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_REMOVEDB) == 0){

            qryJSON = Util.qryStringToJSON(qryString);

            secureRm: try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString(ARGS_SQLPK));

                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_args;
                String funcid = jsob.getString(ARGS_FUNCID);
                String qstr = jsob.getString(ARGS_QSTR);

                jsob_args = jsob.getJSONObject(ARGS_ARGS);
                String table = jsob.getString(ARGS_TABLE);
                SQLHelper targetdb = SQLManager.getSQLHelper(jsob.getString(ARGS_DB));

                int secure = targetdb.checkSecure(table);

                if (secure < 0){
                    break secureRm;
                } else if (secure == 0 ||
                        jsob.getString(ARGS_PASSWORD).equals(Prefs.getPassword(context))){
                msg = Util.stringJA(targetdb.removeDB(table,
                        qstr, jsob_args, funcid));
                } else {
                    msg = Util.stringJA(Util.JSONdbReturn(false, FAIL_PASSWORD, funcid));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_GETMENU) == 0) {

            uri = trimUri(uri,API_GETMENU);
            msg = Util.stringJA(SQLManager.getSQLHelper(DB_SUBSERV).getMenu(uri));
        } else if (uri.indexOf(API_TESTPASSWORD) == 0) {

            uri = trimUri(uri, API_TESTPASSWORD);
            String passwdin = uri.split("/")[0];
            String token = uri.split("/")[1];

            int eql = 0;  //0 means password not equal 1 is equal

            if ((token.equals("T") && passwdin.equals(Prefs.getToken(context))) ||
                    passwdin.equals(Prefs.getPassword(context))) eql = 1;

            msg = Util.stringJA(Util.JSONdbReturn(true, eql, uri.split("/")[2]));

        } else if (uri.indexOf(API_GETTOKEN) == 0) {

            uri = trimUri(uri, API_GETTOKEN);

            JSONArray jray = Util.JSONdbReturn(true, 1, uri.split("/")[0]);
            msg = Util.stringJA(Util.JSONxtraReturn(jray, "token", Prefs.getToken(context)));

        } else if (uri.indexOf(API_GETUPLOADDIR) == 0){

            msg = Util.JSONReturn(false);
            msg = Prefs.getuploaddir(context);

        } else if (uri.indexOf(API_GETIPADD) == 0) {

            uri = trimUri(uri, API_GETIPADD);

            JSONArray jray = Util.JSONdbReturn(true, 1, uri.split("/")[0]);
            msg = Util.stringJA(Util.JSONxtraReturn(jray, "ipadd", MainActivity.getHost()));

        }

        return(msg);
    }


    private static String trimUri(String uri, String lead){

        String strout = "";

        final Matcher matcher = Pattern.compile(lead).matcher(uri);
        if(matcher.find()){
            strout = uri.substring(matcher.end()).trim();
        }

        return(strout);
    }

}

