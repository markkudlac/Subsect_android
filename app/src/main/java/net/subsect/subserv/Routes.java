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
  //         uri = trimUri(uri,API_SAVEFILE);

            qryJSON = Util.qryStringToJSON(qryString);
            try {
                String fullpath = rootdir.getPath()+qryJSON.getString("filename");
                System.out.println("JSON filename : "+ fullpath);
                msg = Util.savefile(fullpath,
                        URLDecoder.decode(qryJSON.getString("filecontent").replace("+", "%2B"), "UTF-8").replace("%2B", "+"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (uri.indexOf(API_PROCSQL) == 0){
            uri = trimUri(uri,API_PROCSQL);
        //    msg = SQLHelper.exclude(getArg(uri, 0), Long.valueOf(getArg(uri, 0)).longValue());

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

