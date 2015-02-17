package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-02-16.
 */
import static net.subsect.subserv.Const.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;



public class Routes {

    public static String callMethods(String uri, String qryString, Context context){

        String msg = Util.JSONReturn(false);

        System.out.println("Route : "+uri + " qryString : "+qryString);


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

