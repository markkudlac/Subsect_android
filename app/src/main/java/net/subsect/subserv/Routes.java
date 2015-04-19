package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-02-16.
 */
import static net.subsect.subserv.Const.*;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;



public class Routes {

    public static String callMethods(String uri, String qryString, Context context, File rootdir){

        JSONObject qryJSON;

        String msg = Util.JSONReturn(false);

      //  System.out.println("Route : "+uri + " qryString : "+qryString);

        uri = trimUri(uri,API_PATH);

        if (uri.indexOf(API_SAVEFILE) == 0){

            qryJSON = Util.qryStringToJSON(qryString);
            try {
                String fullpath = rootdir.getPath()+qryJSON.getString("filename");
                msg = Util.savefile(fullpath,
                        Util.decodeJSuriComp(qryJSON.getString("filecontent")));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (uri.indexOf(API_INSERTDB) == 0) {

            qryJSON = Util.qryStringToJSON(qryString);

            try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString("sqlpk"));

                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_vals = jsob.getJSONObject("values");

                msg = SQLManager.getSQLHelper(jsob.getString("db")).
                        insertDB(jsob.getString("table"), jsob_vals,
                                jsob.getString("funcid")).toString().replace("\\", "");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_QUERYDB) == 0){

            qryJSON = Util.qryStringToJSON(qryString);

            try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString("sqlpk"));
                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_args, jsob_limits;

                jsob_args = jsob.getJSONObject("args");
                jsob_limits = jsob.getJSONObject("limits");

                // System.out.println("Value db 2 : " + dbase);
                //jArray.toString().replace("\\", "");
                msg = SQLManager.getSQLHelper(jsob.getString("db")).
                        queryDB(jsob.getString("qstr"), jsob_args, jsob_limits,
                                jsob.getString("funcid")).toString().replace("\\", "");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_UPDATEDB) == 0){

            qryJSON = Util.qryStringToJSON(qryString);

            try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString("sqlpk"));
                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_args, jsob_values;

                jsob_values = jsob.getJSONObject("values");
                jsob_args = jsob.getJSONObject("args");

                msg = SQLManager.getSQLHelper(jsob.getString("db")).
                        updateDB(jsob.getString("table"),
                                jsob_values, jsob.getString("qstr"), jsob_args,
                                jsob.getString("funcid")).toString().replace("\\", "");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_REMOVEDB) == 0){

            qryJSON = Util.qryStringToJSON(qryString);

            try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString("sqlpk"));

                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_args;
                String funcid = jsob.getString("funcid");
                String qstr = jsob.getString("qstr");

                jsob_args = jsob.getJSONObject("args");
                msg = SQLManager.getSQLHelper(jsob.getString("db"))
                        .removeDB(jsob.getString("table"),
                                qstr, jsob_args, funcid).toString().replace("\\", "");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_GETMENU) == 0) {

            uri = trimUri(uri,API_GETMENU);
            msg = SQLManager.getSQLHelper(DB_SUBSERV).getMenu(getArg(uri, 0)).
                    toString().replace("\\", "");

        } else if (uri.indexOf(API_INSTALLAPP) == 0){
            uri = trimUri(uri,API_INSTALLAPP);
            msg = Util.installApp(context, getArg(uri, 0), getArg(uri, 1), "", -1,getArg(uri, 1));
            //rootpack for now

        } else if (uri.indexOf(API_GETUPLOADDIR) == 0){
            msg = Prefs.getuploaddir(context);
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


    private static String getArg(String uri, int indx){

        String xx = null;
        int i = 0;
        final Matcher matcher = Pattern.compile("(\\w+\\.?\\w+)").matcher(uri);

        while (matcher.find()) {
            if (i == indx) {
                xx = matcher.group(0);
                break;
            }
            ++i;
        }

        return(xx);
    }
}

