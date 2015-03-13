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

        System.out.println("Route : "+uri + " qryString : "+qryString);

        uri = trimUri(uri,API_PATH);

        if (uri.indexOf(API_SAVEFILE) == 0){

            qryJSON = Util.qryStringToJSON(qryString);
            try {
                String fullpath = rootdir.getPath()+qryJSON.getString("filename");
             //   System.out.println("JSON filename : "+ fullpath);
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

                String table = jsob.getString("table");
                String funcid = jsob.getString("funcid");
                System.out.println("Value str : " + table);

                jsob = jsob.getJSONObject("values");
                msg = SQLHelper.insertDB(table, jsob, funcid);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_QUERYDB) == 0){

            qryJSON = Util.qryStringToJSON(qryString);

            try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString("sqlpk"));
                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_args, jsob_limits;
                String funcid = jsob.getString("funcid");

                String qstr = jsob.getString("qstr");

                jsob_args = jsob.getJSONObject("args");
                jsob_limits = jsob.getJSONObject("limits");

                msg = SQLHelper.queryDB(qstr, jsob_args, jsob_limits, funcid);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_UPDATEDB) == 0){

            qryJSON = Util.qryStringToJSON(qryString);

            try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString("sqlpk"));
                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_args, jsob_values;
                String table = jsob.getString("table");
                String funcid = jsob.getString("funcid");
                String qstr = jsob.getString("qstr");

                jsob_values = jsob.getJSONObject("values");
                jsob_args = jsob.getJSONObject("args");
                msg = SQLHelper.updateDB(table, jsob_values, qstr, jsob_args, funcid);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_REMOVEDB) == 0){

            qryJSON = Util.qryStringToJSON(qryString);

            try {
                String sqlpk = Util.decodeJSuriComp(qryJSON.getString("sqlpk"));

                JSONObject jsob = new JSONObject(sqlpk);
                JSONObject jsob_args;
                String table = jsob.getString("table");
                String funcid = jsob.getString("funcid");
                String qstr = jsob.getString("qstr");

                jsob_args = jsob.getJSONObject("args");
                msg = SQLHelper.removeDB(table, qstr, jsob_args, funcid);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (uri.indexOf(API_GETUPLOADDIR) == 0){
            msg = Prefs.getuploaddir(context);
            System.out.println("Get Upload dir : " + msg);
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
        final Matcher matcher = Pattern.compile("(-?\\w+)").matcher(uri);

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

