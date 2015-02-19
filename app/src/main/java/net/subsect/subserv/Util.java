package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-02-16.
 */
import java.io.File;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;

import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import jtar.TarEntry;
import jtar.TarInputStream;

import static net.subsect.subserv.Const.*;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
//import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Base64;
import android.widget.Toast;

public class Util {

    static   void DeleteRecursive(File fileOrDirectory) {

        System.out.println("IN deleteRecursive : " + fileOrDirectory.getAbsolutePath());
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }



    public static void versionChangeHTML(Context mnact ) {

        try {

            File htmlpar,userdir;

            userdir = new File(mnact.getFilesDir(),USERHTML_DIR);
            htmlpar = new File(mnact.getFilesDir(),HTML_DIR);

            //*******  This delete is here for testing now
            // 		if (userdir.exists())	DeleteRecursive(userdir);
            //*************

            if (!userdir.exists()){
                userdir.mkdirs();	//Create the user directory if it doesn't exit
            }

            System.out.println("HTML DIR is : "+userdir.getAbsolutePath());

            System.out.println("The version number changed");
            if (htmlpar.exists())	DeleteRecursive(htmlpar);
            untarTGzFile(mnact);

        } catch (Exception e) {
            System.out.println( "File I/O error " + e);
        }

    }


    public static void untarTGzFile(Context mnact) throws IOException {

        String destFolder = mnact.getFilesDir().getAbsolutePath();
        FileInputStream zis = (mnact.getAssets().openFd("rootpack.targz")).createInputStream();

        TarInputStream tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(zis)));
        tis.setDefaultSkip(true);
        untar(mnact, tis, destFolder);

        tis.close();
    }



    private static void untar(Context mnact, TarInputStream tis, String destFolder) throws IOException {
        BufferedOutputStream dest = null;

        TarEntry entry;
        while ((entry = tis.getNextEntry()) != null) {
// 			System.out.println("Extracting: " + entry.getName());
            int count;
            byte data[] = new byte[BASE_BLOCKSIZE];

            if (entry.isDirectory()) {
                new File(destFolder + "/" + entry.getName()).mkdirs();
                continue;
            } else {
                int di = entry.getName().lastIndexOf('/');
                if (di != -1) {
                    new File(destFolder + "/" + entry.getName().substring(0, di)).mkdirs();
                }
            }

            FileOutputStream fos = new FileOutputStream(destFolder + "/" + entry.getName());
            dest = new BufferedOutputStream(fos);

            while ((count = tis.read(data)) != -1) {
                dest.write(data, 0, count);
            }

            dest.flush();
            dest.close();
        }
    }


    static public File targetCopyFile(String copyfile) {
        File dirfile, targfile;
        String fileonly, dir;

        fileonly = copyfile;
        int xind = fileonly.lastIndexOf("/");
        if (xind >= 0){
            fileonly = fileonly.substring(xind+1);
        }

        dir = copyfile.substring(0, xind);

        //		System.out.println("targetCopy dir : "+dir+"  file : "+fileonly);
        dirfile = new File(dir);
        if (!dirfile.exists()) {
            dirfile.mkdirs();
            //   		System.out.println("targetCopy dir is created");
        }

        targfile = new File(dirfile,fileonly);

        if (targfile.exists()){
            //    		System.out.println("file exists : "+ targfile.getPath());
            targfile.delete();
        }

        return (new File(dirfile,fileonly));
    }


     /*

     static public void downloadFile(Context context, long id, String uri){
     	HttpURLConnection con = null;
        File downfl = null;
        byte [] xbuf = new byte[BASE_BLOCKSIZE];

    	try {
    		System.out.println("HttpAdImage : "+uri);
    		downfl = Util.targetCopyFile(context.getFilesDir()+uri);

    		URL url = new URL(HTTP_PROT, SOURCE_ADDRESS, Uri.encode(uri));
    		con = (HttpURLConnection) url.openConnection();

    		InputStream httpin = (InputStream) con.getInputStream();
    	    FileOutputStream downflout = new FileOutputStream(downfl);

    	    // Transfer bytes from in to out
    	    System.out.println("Start transfer");
    	    Integer fbytes = 0;
    	    int len;
    	    while ((len = httpin.read(xbuf)) > 0) {
    	        downflout.write(xbuf, 0, len);
    	        fbytes += len;
    	    }
    	    httpin.close();
    	    downflout.close();

    	    SQLHelper.setAdvertStatus(id, "A");		//Make advert active
    	    System.out.println("Done transfer");
    	}
    	catch (Exception ex) {
    		System.out.println("Exception caught 1 : " + ex);
    	}

    	finally {
    		if (con != null) {
    			con.disconnect();
    		} else {
    			System.out.println("con null 2");
    		}
    	}
     }
     */


    static public String copyImage(Context context, String img, String imgdir, String flname, Long adlid){

        File downfl = null;
        String uri;

        uri =  "/"+imgdir +"/"+flname.substring(0, 3)+
                adlid+flname.substring(flname.length()-4);

        byte[] imageAsBytes = Base64.decode(img, Base64.DEFAULT);

        downfl = Util.targetCopyFile(context.getFilesDir() + uri);

        try {
            FileOutputStream downflout = new FileOutputStream(downfl);
            downflout.write(imageAsBytes, 0, imageAsBytes.length);
            downflout.close();
        }
        catch (Exception ex) {
            System.out.println("Exception caught 1 : " + ex);
            return(null);
        }

        return(uri);
    }


    static public String copyLocalHref(Context context, String img, String landingdir, String flname, Long adlid){

        File downfl = null, tmpdir;
        String unldfl, locpath;

        locpath =  "/"+landingdir+"/"+flname.substring(0, 3)+adlid;
        unldfl = context.getFilesDir() + locpath +"/"+flname;

        tmpdir = new File(context.getFilesDir() + locpath);
        if (tmpdir.exists()) DeleteRecursive(tmpdir);

        byte[] imageAsBytes = Base64.decode(img, Base64.DEFAULT);

        System.out.println("copyLocalHref : " + unldfl);
        downfl = Util.targetCopyFile(unldfl);

        try {
            FileOutputStream downflout = new FileOutputStream(downfl);
            downflout.write(imageAsBytes, 0, imageAsBytes.length);
            downflout.close();

            FileInputStream zis = new FileInputStream(downfl);

            TarInputStream tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(zis)));
            tis.setDefaultSkip(true);
            untar(context, tis, context.getFilesDir() + locpath);

            tis.close();
            downfl.delete();
        }
        catch (Exception ex) {
            System.out.println("Exception caught 1 : " + ex);
            return(null);
        }

        return(locpath + "/index.html");
    }


    static public long getTimeNow(){

        Time tm = new Time();
        tm.setToNow();
        return(tm.toMillis(true));
    }


    static public String JSONReturn(Boolean val){

        return("{\"rtn\":" + val + "}");
    }


    static	public String getWifiApIpAddress() {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {

                NetworkInterface intf = en.nextElement();

                if (intf.getName().contains("wlan") || intf.getName().contains("eth0")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4)) {
 	                    	System.out.println("AP address : " + inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.println("AP exception : " + ex);
        }
        return null;
    }


    static public String getHTTPAddress(Context context){

        String ipad = "localhost";

     //   if (Prefs.getIPaddress(context)){
     //       ipad = Util.getWifiApIpAddress();

     //   }

        return(ipad);
    }


    static public JSONObject qryStringToJSON(String qryString){

        String xstr;
        JSONObject jsob = null;

        xstr = "{\""+Uri.decode(qryString)+"\"}";

        xstr = xstr.replace("&", "\", \"");
        xstr = xstr.replace("=", "\":\"");
        System.out.println("JSON string : "+xstr);

        try {
            jsob = new JSONObject(xstr);
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }

        return(jsob);
    }


    public static boolean isWifiDataConected(Context context) {

//    	 WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        //   	 if (wifiMgr != null && wifiMgr.isWifiEnabled()){

        ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() ||
                conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()){
            System.out.println("WiFi or data is connected.");
            return(true);
        }
        //   	 }
        //   	 System.out.println("WiFi NOT connected.");
        return(false);
    }


    public static void sendNotofication(Context context, JSONObject item){

        /*
        try {

            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getString(FLD_URLHREF)));
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    myIntent,
                    Intent.FLAG_ACTIVITY_NEW_TASK);

            byte[] imageAsBytes = Base64.decode(item.getString("icon"), Base64.DEFAULT);

            //   	     Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.adladla72x72);
            Bitmap largeIcon = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length );

            Notification myNotification = new NotificationCompat.Builder(context)
                    .setContentTitle("Your Followup Request")
                    .setContentText(item.getString("descript"))
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.adladl_a)
                    .setLargeIcon(largeIcon)
                    .build();

            NotificationManager notificationManager =
                    (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(item.getInt(FLD_ADVERT_ID), myNotification);

            SQLHelper.uploadDone(new JSONArray().put(item), context);
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
        */
    }

}
