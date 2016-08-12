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
import java.io.FilenameFilter;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
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
import jtar.TarOutputStream;

import static net.subsect.subserv.Const.*;

import android.content.Context;

import android.os.Environment;
import android.text.format.Time;
import android.util.Base64;
import android.widget.Toast;


public class Util {

    private static String uploadDirectory = "";


    public static void setUploadDirectory(String dir){
        uploadDirectory = dir;
    }


    public static String getUploadDirectory() {
        return uploadDirectory;
    }


    static void DeleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }


    public static String installApp(Context context, String appdir, String filenm, String icon,
                                    int subsectid, String title, String permissions){
        String rtn = JSONReturn(false);

        try {

            System.out.println("In installapp appdir : "+appdir + "  filenm : " + filenm +
                    "  permissions : " + permissions);

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

            if (title.length() == 0 ) title = appName;

            installto = new File(context.getFilesDir(), appdir+"/"+ appName);

            if (installto.exists()) DeleteRecursive(installto);
            untarTGzFile(context, installfrom, appdir);
            installfl.delete();

            if (icon.length() == 0){
                icon = loadIcon(installto);
            }

            // There could be a problem here because dup app name
            if (SQLManager.createIsOpenDb(DB_SYS+appName, FIXED_DB_VERSION)){
                // This is a new install db created and log registry
                System.out.println("New install : "+appName);
                SQLHelper.initializeRegistry((SQLManager.getSQLHelper(DB_SUBSERV)).getDatabase(),
                        appName, true, icon, subsectid, title, permissions);
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


    static String loadIcon(File sitedir){
        String icon64 = "";
        File icondir;

        icondir = new File(sitedir, "icon");

        if (icondir.exists() && icondir.isDirectory()) {
            File[] icary = icondir.listFiles();
            byte data[] = new byte[BASE_BLOCKSIZE];

            try {
                int count;
                int offst = 0;

                if (icary.length > 0) {
                    FileInputStream fis = new FileInputStream(icary[0]);
                    BufferedInputStream bfin = new BufferedInputStream(fis);

                    while (offst < BASE_BLOCKSIZE - 2001 &&
                            (count = bfin.read(data, offst, 2000)) != -1) {
                        offst += count;
                    }
                //    System.out.println("Total offst " + offst);
                    bfin.close();
                    icon64 = Base64.encodeToString(data, 0, offst, Base64.NO_WRAP);
                }
            }
            catch(Exception e) {
                System.out.println("File I/O error " + e);
            }
        }

        return(icon64);
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


    static public JSONArray JSONxtraReturn(JSONArray jray, String field, String val){

        try {
            JSONObject jobj = new JSONObject("{\"" + field + "\":\"" + val +"\"}");

            jray.put(jobj);
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
        return(jray);
    }

    static public String stringJA(JSONArray ja){
        return(ja.toString().replace("\\", ""));
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
            System.out.println("File I/O error " + e);
        }

        return (msg);
    }


    protected static String deletefile(String filename) {

        return (Util.JSONReturn(new File(filename).delete()));

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


    public static String[] getSchema(Context context, String dbname, String flnme) {

        String schema = "";
        String[] sql_table =  new String[4];

        sql_table[1] = "";
        sql_table[2] = "FFF";   //permissions
        sql_table[3] = "";      //Data Loadfile for schema

        try {
            String appname = getAppfromDb(dbname);
            File readfl;
            String schemapath = context.getFilesDir().getPath();
            String readbuf;
            schemapath = schemapath + "/" + getDirfromDb(dbname) +"/"+appname+"/schemas";
         //    System.out.println("Scemas DIR is : " + schemapath);

            readfl = new File(schemapath, flnme);
            BufferedReader buf = new BufferedReader(new FileReader(readfl));
            while ((readbuf = buf.readLine()) != null){
                schema = schema + " " + readbuf;
            }
            buf.close();

            readfl = new File(schemapath, flnme + LOADFILE_EXT);
            if (readfl.exists()) {
                buf = new BufferedReader(new FileReader(readfl));
                while ((readbuf = buf.readLine()) != null) {
                    sql_table[3] = sql_table[3] + " " + readbuf;
                }
                buf.close();
            }

            Pattern ptn = Pattern.compile("^\\s*" + SKIP_SCHEMA, Pattern.CASE_INSENSITIVE);
            Matcher mtcher = ptn.matcher(schema);

            if (mtcher.find()) {   // if at beginning of table file
                System.out.println("Table skipped");
                schema = "";
            } else {
                String perms;
                ptn = Pattern.compile("^\\s*#" + FLD_PERMISSIONS + "\\s*\\w*", Pattern.CASE_INSENSITIVE);
                mtcher = ptn.matcher(schema);
                if (mtcher.find()) {

                    perms = mtcher.group();

                    schema = schema.replaceAll(perms, "").trim();

                    //System.out.println("Schema after replace 2 : " + schema);
                    sql_table[2] = perms.trim().split("\\s+", 3)[1];
                    sql_table[1] = schema.split("\\s+", 4)[2];

                  //  System.out.println("Secure on tablename : "+ sql_table[1] + "  Perms : " +
                  //  sql_table[2]);
                }

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

        sql_table[0] = schema;
     //   System.out.println("Schema SQL: " + sql_table[0] + "  table : " + sql_table[1]);
        return sql_table;
    }


    public static String[] getSchemaFileNames(Context context, String dbname){

        String[] flnmes = new String[0];

        try {
            String appname = getAppfromDb(dbname);
            String schemapath = context.getFilesDir().getPath();
            schemapath = schemapath + "/" + getDirfromDb(dbname) +"/"+appname+"/schemas";

            File schemdir = new File(schemapath);

            if (!schemdir.exists() || !schemdir.isDirectory()) return flnmes;

            flnmes = schemdir.list(
                    new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return ! name.endsWith(LOADFILE_EXT);
                        }}
            );
          //  System.out.println("Got schema files list  " + flnmes.toString() );
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


    public static void DbBackup(Context context, ToolsActivity xact){

      //  File data = Environment.getDataDirectory();
        int count;
        byte data[] = new byte[BASE_BLOCKSIZE];

        xact.postMessage("Begin Backup", Toast.LENGTH_SHORT);

        MainActivity.stopDb();

        File restdir =  new File(context.getFilesDir().getPath(), RESTORE);   //CLEAN RESTORE
        if (restdir.exists()) {
            DeleteRecursive(restdir);
        }
        restdir.mkdir();

        File bkupdir =  new File(context.getFilesDir().getPath(), BACKUP);
        if (bkupdir.exists()) {
            DeleteRecursive(bkupdir);
        }
        bkupdir.mkdir();

        File DBfile = null;
        File DBbkupfile = null;

        FileChannel source=null;
        FileChannel destination=null;

        File currentDB = new File(Environment.getDataDirectory(), APP_DBPATH);

        String[] xfiles = currentDB.list();

        for (int i=0; i< xfiles.length; i++ ) {
            if (!xfiles[i].endsWith("-journal")) {
          //      System.out.println("Got db files list  " + xfiles[i]);

                DBfile = new File(currentDB, xfiles[i]);
                DBbkupfile = new File(bkupdir, xfiles[i]);


                try {
                    source = new FileInputStream(DBfile).getChannel();
                    destination = new FileOutputStream(DBbkupfile).getChannel();
                    destination.transferFrom(source, 0, source.size());
                    source.close();
                    destination.close();
                    //           System.out.println("db file copied " + xfiles[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String tarflnm = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        tarflnm = SUBSERV + tarflnm + BKUP_TAR;
        try {

            File tout = new File(bkupdir, tarflnm);
            TarOutputStream tarout = new TarOutputStream(new FileOutputStream(tout));
            FileInputStream datain;
            File datafl;

            for (int i=0; i< xfiles.length; i++ ) {
                if (!xfiles[i].endsWith("-journal")) {
          //          System.out.println("db file tarred " + xfiles[i]);
                    xact.postMessage("Creating tar file : " + xfiles[i],
                            Toast.LENGTH_SHORT);
                    datafl = new File(bkupdir, xfiles[i]);
                    datain = new FileInputStream(datafl);

                    tarout.putNextEntry(new TarEntry(datafl, xfiles[i]));

                    while ((count = datain.read(data)) != -1) {
                        tarout.write(data, 0, count);
                    }
                    datain.close();
                }
            }
            tarout.close();


        } catch(IOException e) {
            e.printStackTrace();
            xact.postMessage("Back up failed. Reload Subsect App", Toast.LENGTH_LONG);
        }
     //   (MainActivity)context.startdb();
        xact.postMessage("Backup Complete. Reload Subsect App", Toast.LENGTH_LONG);
    }


    public static void DbRestore(Context context, ToolsActivity xact) {

        //  File data = Environment.getDataDirectory();
        int count;
        byte data[] = new byte[BASE_BLOCKSIZE];

        xact.postMessage("Begin Restore.",Toast.LENGTH_SHORT);

        MainActivity.stopDb();

        File restdir = new File(context.getFilesDir().getPath(), RESTORE);   //CLEAN RESTORE
        if (!restdir.exists()) {
            xact.postMessage("Restore directory missing.Upload again.",Toast.LENGTH_LONG);
                    restdir.mkdir();
            return;
        }

        File DBfile = null;
        File restorefile = null;

        FileChannel source = null;
        FileChannel destination = null;

        File currentDB = new File(Environment.getDataDirectory(), APP_DBPATH);

        String[] xfiles = restdir.list();

        TarInputStream tis = null;
        File tarfl = null;

        try {
            for (int i = 0; i < xfiles.length; i++) {
                if (xfiles[i].endsWith(BKUP_TAR)) {
                    tarfl = new File(restdir, xfiles[i]);
                    tis = new TarInputStream(new BufferedInputStream(
                            new FileInputStream(tarfl)));
                    tis.setDefaultSkip(true);
                    untar(context, tis, restdir.getAbsolutePath());

                    tis.close();
                    tarfl.delete();

                    tis = null;
                    tarfl = null;
                }
            }

            xfiles = restdir.list();

            for (int i = 0; i < xfiles.length; i++) {

                if (xfiles[i].startsWith(DB_SYS) ||
                        xfiles[i].startsWith(DB_USR) ) {

                    DBfile = new File(currentDB, xfiles[i]);
                    restorefile = new File(restdir, xfiles[i]);

                    source = new FileInputStream(restorefile).getChannel();
                    destination = new FileOutputStream(DBfile).getChannel();
                    destination.transferFrom(source, 0, source.size());
                    source.close();
                    destination.close();
                  //  System.out.println("db file copied " + xfiles[i]);
                    xact.postMessage("Restore file : "+ xfiles[i],Toast.LENGTH_SHORT);
                    restorefile.delete();
                }
            }
        } catch (IOException e) {
            xact.postMessage("Restore Failed. Reload Subsect App", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
        xact.postMessage("Restore Complete. Reload Subsect App",Toast.LENGTH_LONG);
    }


    public static void exportPkg(Context context, String pkg, ToolsActivity xact) {

        String[] pkgnm = pkg.split("/");
        String tarflnm = pkgnm[pkgnm.length - 1];
        String basefl = pkgnm[0];

        System.out.println("Base dir : " + basefl + "  Tar file : " + tarflnm);
        xact.postMessage("Export : " + tarflnm, Toast.LENGTH_SHORT);

        File bkupdir = new File(context.getFilesDir().getPath(), BACKUP);
        if (bkupdir.exists()) {
            DeleteRecursive(bkupdir);
        }
        bkupdir.mkdir();

        try {
            File tout = new File(bkupdir, tarflnm+".tar");
            TarOutputStream tarout = new TarOutputStream(new FileOutputStream(tout));

            if (!TarRecursive(new File(context.getFilesDir().getPath() + "/" + basefl),
                    tarflnm, tarout)) {
                xact.postMessage("Export Failed", Toast.LENGTH_LONG);
            } else {
                xact.postMessage("Exported to directory : " + BACKUP, Toast.LENGTH_LONG);
            }
            tarout.close();

        } catch(IOException e) {
            e.printStackTrace();
            xact.postMessage("Export Failed", Toast.LENGTH_LONG);
        }
    }


    static boolean TarRecursive(File basefl, String fileOrDir, TarOutputStream tarout) {

        int count;
        byte data[] = new byte[BASE_BLOCKSIZE];

      //  System.out.println("fileOrDir is : " + fileOrDir);

        try {
            File tmpfile = new File(basefl, fileOrDir);
            tarout.putNextEntry(new TarEntry(tmpfile, fileOrDir));

            if (tmpfile.isDirectory()) {
                for (String child : tmpfile.list())
                    if (!TarRecursive(basefl, fileOrDir + "/" + child, tarout)) {
                        return false;
                    }
            } else {

                FileInputStream datain = new FileInputStream(tmpfile);

                while ((count = datain.read(data)) != -1) {
                    tarout.write(data, 0, count);
                }
                datain.close();
            }

        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static String getSha1Hex(String clearString)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(clearString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes)
            {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
            return null;
        }
    }


    public static boolean testPerm(int operation, String perm, String passwd, Context context){

        perm = perm.trim();

        int num = Integer.parseInt(perm.split("", 5)[PERM_USER+1], 16);

        if ((num & operation) > 0) {
            return true;
        }

   //     System.out.println("testPerm split : " + perm.split("", 5)[PERM_SUPER+1]);
        num = Integer.parseInt(perm.split("", 5)[PERM_SUPER+1], 16);

        return (passwd.equals(Prefs.getPassword(context)) &&
                (num & operation) > 0);

    }


    public static String generateToken(Context context){

        String toke = Prefs.getToken(context);

        toke = toke + Prefs.getPassword(context) + getTimeNow();
        toke = getSha1Hex(toke);
        Prefs.setToken(context, toke);
        System.out.println("Token generated : " + toke);
        return(toke);
    }
}