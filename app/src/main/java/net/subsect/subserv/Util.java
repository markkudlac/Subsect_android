package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-02-16.
 */
import java.io.BufferedReader;
import java.io.File;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import jtar.TarEntry;
import jtar.TarInputStream;

import static net.subsect.subserv.Const.*;

import android.content.Context;

import android.text.format.Time;
import android.util.Base64;


public class Util {

    static void DeleteRecursive(File fileOrDirectory) {

        System.out.println("IN deleteRecursive : " + fileOrDirectory.getAbsolutePath());
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }


    public static String installApp(Context context, String appdir, String filenm, String icon,
                                    int subsectid){
        String rtn = JSONReturn(false);

        try {

          //  System.out.println("In installapp appdir : "+appdir + "  filenm : " + filenm);
            String appName;
            File installto;
            File installfl;
            FileInputStream installfrom;


            installfl = new File(context.getFilesDir().getAbsolutePath()+
                    "/"+INSTALL_DIR+"/"+filenm);

            installfrom = new FileInputStream(installfl);
            appName = checkInstall(context, installfrom);
            installfrom = new FileInputStream(installfl); //Need to reopen as tar closes

            if (appName.length() == 0) throw new Exception("No name found in targz"); //No name found in taegz

            installto = new File(context.getFilesDir(), appdir+"/"+ appName);
            // System.out.println("HTML DIR is : " + userdir.getAbsolutePath());

            if (installto.exists()) DeleteRecursive(installto);
            untarTGzFile(context, installfrom, appdir);
            installfl.delete();

            // There could be a problem here because dup app name
            if (SQLManager.createIsOpenDb(DB_SYS+appName, FIXED_DB_VERSION)){
                // This is a new install db created and log registry
                System.out.println("New install : "+appName);
                SQLHelper.initializeRegistry((SQLManager.getSQLHelper(DB_SUBSERV)).getDatabase(),
                        appName, true, icon, subsectid);
            } else {
                System.out.println("Update install : "+appName);
                // This is an update with db already open tables should be mods only
                SQLHelper.processTables(context,
                        (SQLManager.getSQLHelper(DB_SYS+appName)).getDatabase(),
                        DB_SYS+appName, true);
            }
            rtn = JSONReturn(true);
        } catch (Exception e) {
            System.out.println("File I/O error " + e);
        }
        return(rtn);
    }


    public static void installAssets(Context context) {

        try {

            File htmlpar, userdir;

            userdir = new File(context.getFilesDir(), USR_DIR);
            if (!userdir.exists()) {
                userdir.mkdirs();    //Create the user directory if it doesn't exit
            } else {
                //*******  This delete is here for testing now
                // 		DeleteRecursive(userdir);
                //*************
            }

            htmlpar = new File(context.getFilesDir(), SYS_DIR);
           // System.out.println("HTML DIR is : " + userdir.getAbsolutePath());

            if (htmlpar.exists()) DeleteRecursive(htmlpar);

            untarTGzFile(context, (context.getAssets().openFd(INSTALL_FILE)).createInputStream(), "");

        } catch (Exception e) {
            System.out.println("File I/O error " + e);
        }
    }


    public static void untarTGzFile(Context mnact, FileInputStream zis, String targdir) throws IOException {

        String destFolder = mnact.getFilesDir().getAbsolutePath();
        if (targdir.length() > 0){
            destFolder = destFolder + "/" + targdir;
        }
       // FileInputStream zis = (mnact.getAssets().openFd(INSTALL_FILE)).createInputStream();

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


    static protected String checkInstall(Context context, FileInputStream zis) throws IOException {

        TarEntry entry;

        String appdir =  "";
        TarInputStream tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(zis)));
        tis.setDefaultSkip(true);

        byte data[] = new byte[BASE_BLOCKSIZE];

        System.out.println("Extracting appdir from targz ");

        while ((entry = tis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                appdir = entry.getName();
                appdir = (appdir.split("/",3))[0];
                System.out.println("Extracting got name : "+appdir);
                break;
            }

            while (tis.read(data) != -1) {
             // * just loop to directory
            }
        }
        tis.close();
        return(appdir);
    }


    static public File targetCopyFile(String copyfile) {
        File dirfile, targfile;
        String fileonly, dir;

        fileonly = copyfile;
        int xind = fileonly.lastIndexOf("/");
        if (xind >= 0) {
            fileonly = fileonly.substring(xind + 1);
        }

        dir = copyfile.substring(0, xind);

     // 		System.out.println("targetCopy dir : "+dir+"  file : "+fileonly);
        dirfile = new File(dir);
        if (!dirfile.exists()) {
            dirfile.mkdirs();
      //        		System.out.println("targetCopy dir is created");
        }

        targfile = new File(dirfile, fileonly);

        if (targfile.exists()) {
           	//	System.out.println("file exists : "+ targfile.getPath());
            targfile.delete();
        }

        return (new File(dirfile, fileonly));
    }


    static public Boolean copyBase64(Context context, String flin, String flout) {

        File downfl = null;

        byte[] fileAsBytes = Base64.decode(flin, Base64.DEFAULT);
        downfl = Util.targetCopyFile(context.getFilesDir() + flout);

        try {
            FileOutputStream downflout = new FileOutputStream(downfl);
            downflout.write(fileAsBytes, 0, fileAsBytes.length);
            downflout.close();
        } catch (Exception ex) {
            System.out.println("Exception caught 1 : " + ex);
            return (false);
        }
        return (true);
    }


    static public long getTimeNow() {

        Time tm = new Time();
        tm.setToNow();
        return (tm.toMillis(true));
    }


    static public String JSONReturn(Boolean val) {

        return ("{\"rtn\":" + val +"}");
    }


    static public JSONArray JSONdbReturn(Boolean val, long id, String funcid) {

        JSONArray jray = new JSONArray();

        try {
            JSONObject jobj = new JSONObject("{\"rtn\":" + val + ", " + "\"db\":" + id +
                ", " + "\"funcid\":\"" + funcid +"\"}");

            jray.put(0, jobj);
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
        return(jray);
    }


    static public String getWifiApIpAddress() {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {

                NetworkInterface intf = en.nextElement();

                if (intf.getName().contains("wlan") || intf.getName().contains("eth0")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements(); ) {
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


    static public String getHTTPAddress(Context context) {

        String ipad;

        ipad = Util.getWifiApIpAddress();
        if (ipad == null) ipad = "localhost";
        return (ipad);
    }


    static public JSONObject qryStringToJSON(String qryString) {

        String xstr;
        JSONObject jsob = null;

     //   xstr = "{\"" + Uri.decode(qryString) + "\"}";

        xstr = "{\"" + qryString + "\"}";
        xstr = xstr.replace("&", "\", \"");
        xstr = xstr.replace("=", "\":\"");
        System.out.println("JSON string : " + xstr);

        try {
            jsob = new JSONObject(xstr);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return (jsob);
    }


    static public String decodeJSuriComp(String str){

        String decodestr = null;

        try {
            decodestr = URLDecoder.decode(str.replace("+", "%2B"), "UTF-8").replace("%2B", "+");
        }  catch (Exception ex) {
        ex.printStackTrace();
    }

    return (decodestr);
    }


    protected static String savefile(String filename, String filecontent) {

        String msg = Util.JSONReturn(false);

        try {
            byte [] xbuf = filecontent.getBytes("UTF-8");
            File fl_dest;

            fl_dest = targetCopyFile(filename);

            OutputStream out = new FileOutputStream(fl_dest);
            out.write(xbuf, 0, xbuf.length);
            out.close();
            msg = Util.JSONReturn(true);
        } catch (IOException e) {
            System.out.println( "File I/O error " + e);
        }

        return (msg);
    }


    public static String[] JSONOtoStringArray(JSONObject jsob){

        String[] ary = new String[jsob.length()];
        int i = 0;

        try {
            Iterator<String> itr = jsob.keys();
            while(itr.hasNext()) {
                ary[i] = jsob.getString(itr.next());
                i++;
            }
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }

        return ary;
    }


    public static Boolean singleWord(String str){

        return(! str.contains(" "));
    }


    public static String getSchema(Context context, String dbname, String flnme) {

        String schema ="";

        try {
            String appname = getAppfromDb(dbname);
            File schemfl;
            String schemapath = context.getFilesDir().getPath();
            String readbuf;
            schemapath = schemapath + "/" + getDirfromDb(dbname) +"/"+appname+"/schemas";
         //    System.out.println("Scemas DIR is : " + schemapath);
            schemfl = new File(schemapath, flnme);

            BufferedReader buf = new BufferedReader(new FileReader(schemfl));
            while ((readbuf = buf.readLine()) != null){
                schema = schema + " " + readbuf;
            }

            buf.close();

            Pattern ptn = Pattern.compile("^\\s*" + SKIP_SCHEMA, Pattern.CASE_INSENSITIVE);
            Matcher mtcher = ptn.matcher(schema);

            if (mtcher.find()) {   // if at beginning of table file
                System.out.println("Table skipped");
                schema = "";
            } else {
                ptn = Pattern.compile("^\\s*create", Pattern.CASE_INSENSITIVE);
                mtcher = ptn.matcher(schema);
                // Only add id etc if on create table
                if (mtcher.find()) {
                    String ext = " , " + FLD_ID + " integer primary key autoincrement, " +
                            FLD_CREATED_AT + " integer default 0, " +
                            FLD_UPDATED_AT + " integer default 0 " +
                            ")";
                    schema = schema.replaceAll("\\)\\s*$", ext);
                }
            }
        } catch (Exception e) {
            System.out.println("File I/O error " + e);
        }

        System.out.println("Schema SQL: " + schema);
        return schema;
    }


    public static String[] getSchemaFileNames(Context context, String dbname){

        String[] flnmes = new String[0];

        try {
            String appname = getAppfromDb(dbname);
            String schemapath = context.getFilesDir().getPath();
            schemapath = schemapath + "/" + getDirfromDb(dbname) +"/"+appname+"/schemas";

            File schemdir = new File(schemapath);

            if (!schemdir.exists() || !schemdir.isDirectory()) return flnmes;

            flnmes = schemdir.list();
            System.out.println("Got schema files list  " + flnmes.toString() );
        } catch (Exception e) {
            System.out.println("File I/O error " + e);
        }

        return(flnmes);
    }


    public static String getDirfromDb(String dbnm){

        if (dbnm.startsWith(DB_SYS)){
            return SYS_DIR;
        } else {
            return USR_DIR;
        }
    }

    public static String getAppfromDb(String dbnm){
        return(dbnm.substring(2));
    }
}