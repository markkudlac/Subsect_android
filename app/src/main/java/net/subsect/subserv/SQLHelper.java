package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-02-16.
 */

import static net.subsect.subserv.Const.*;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.database.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;


public class SQLHelper extends SQLiteOpenHelper {

    private SQLiteDatabase database = null;
    private String dbname;
    private Context context;

    public SQLHelper(Context context, String dbname, int dbversion) {

        super(context, dbname, null, dbversion);
        this.dbname = dbname;
        this.context = context;

        try {
            database = getWritableDatabase();
        } catch (SQLiteException e) {
            System.out.println("SQLiteException");
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("In onCreate db : "+dbname);
        if(isadmindb(dbname)) {
            Util.installAssets(context);
        }
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        System.out.println("In onUpgrade db old : " + oldVersion + " new : " + newVersion);

      //  if (newVersion > oldVersion && isadmindb(dbname)) {
      //      Util.installAssets(context);
      //  }
    }


    protected void createTables(SQLiteDatabase db) {

        System.out.println("In createTables : " + dbname);

        try {
            if (isadmindb(dbname)) {

                db.execSQL(
                        "create table " + TBL_REGISTRY + " ( " +
                                FLD_ID + " integer primary key autoincrement, " +
                                FLD_APP + " text, " +
                                FLD_TYPE + " char(2) default \'"+ DB_USR + "\', " +
                                FLD_ICON + " text, " +
                                FLD_SUBSECTID + " integer, " +
                                FLD_HREF + " char(50), " +
                                FLD_STATUS + " char(1) default \'"+ ACTIVE_STATUS + "\', " +
                                FLD_CREATED_AT + " integer default 0, " +
                                FLD_UPDATED_AT + " integer default 0 " +
                                ")"
                );

             //   initializeRegistry(db, PREINSTALL_1, true);
            } else {
                processTables(context, db, dbname, true);
            }
    //        System.out.println("Out createTables");
        } catch (SQLException e) {
              System.out.println("SQLException create");
        }
    }


    public static void processTables(Context context, SQLiteDatabase db, String dbnm, boolean sys) {
        String sqlst;
        String[] schemafls = Util.getSchemaFileNames(context, dbnm);

       // System.out.println("In processTables : " + Util.getAppfromDb(dbnm));
        for (int i = 0; i < schemafls.length; i++) {
            sqlst = Util.getSchema(context, dbnm, schemafls[i]);

            if (sqlst.length() > 0) db.execSQL(sqlst);
        }
    }


    public SQLiteDatabase getDatabase(){
        return(database);
    }


    private boolean isadmindb(String dbnm){
        return(dbnm == DB_SUBSERV);
    }


    public void closeDb() {
        database.close();
        database = null;
    }


    public static boolean initializeRegistry(SQLiteDatabase db, String app, boolean sys,
                       String icon, int subsectid) {
        ContentValues values = new ContentValues();

        System.out.println(TBL_REGISTRY + " app : "+ app);
                values.put(FLD_APP, app);
                values.put(FLD_ICON, icon);
                values.put(FLD_SUBSECTID, subsectid);
                if (app.equals(PREINSTALL_1)) {
                    values.put(FLD_HREF, SUB_HREF_LOCAL +app+"/"+app.toLowerCase()+".html");
                } else {
                    values.put(FLD_HREF, SUB_HREF_REMOTE + app);
                }

        if (sys) {
            values.put(FLD_TYPE, DB_SYS);
        } else {
            values.put(FLD_TYPE, DB_USR);
        }
        values.put(FLD_CREATED_AT, Util.getTimeNow());

        if (-1 == db.insert(TBL_REGISTRY, null, values)) {
            System.out.println(TBL_REGISTRY + " insert error");
            return(false);
        }

        return(true);
    }


    public boolean removeSite(int siteid){
        JSONArray jray;

        try {
            JSONObject qargs = new JSONObject("{ \"id\": \""+ siteid +"\" }");
            jray = queryDB(TBL_REGISTRY, qargs,
                    new JSONObject(), "1");

            if (jray.length() > 1){

                JSONObject vals = new JSONObject("{ \""+FLD_STATUS+"\": \""+ DELETE_STATUS +"\" }");
                JSONArray tmpary = updateDB(TBL_REGISTRY, vals, "", qargs, "-1");

              //  System.out.println("Updatr status : "+tmpary.getJSONObject(0).getInt("db"));

                if (tmpary.getJSONObject(0).getInt("db") > 0){
                    String dbnm =  jray.getJSONObject(1).getString(FLD_TYPE) +
                               jray.getJSONObject(1).getString(FLD_APP);

                    context.deleteDatabase(dbnm);
                   // System.out.println("DBPath  2 : " + context.getDatabasePath("S_TestApp").getPath());

                    Util.DeleteRecursive(new File(context.getFilesDir().getAbsolutePath()+
                            "/"+ Util.getDirfromDb(dbnm)+"/" +
                            jray.getJSONObject(1).getString(FLD_APP)));

                    removeDB(TBL_REGISTRY, "", qargs,"-1");
                    return(true);
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return(false);
    }


    public JSONArray getMenu(String funcid){

        JSONArray jray = Util.JSONdbReturn(false, -1, "-1");

        try {

            JSONObject qargs = new JSONObject("{ \""+FLD_STATUS+"\": \""+ ACTIVE_STATUS +"\" }");
          //  JSONObject qargs = new JSONObject();
            jray = queryDB(TBL_REGISTRY, qargs, new JSONObject(), funcid);


            if (jray.length() > 1) {
                String tmphref = jray.getJSONObject(1).getString(FLD_HREF);
                //   System.out.print("items string 1 : " + items.substring(6500));
                String rmt = "http://" + Prefs.getHostname(context) + ".subsect.net/app/";
                tmphref = tmphref.replace(SUB_HREF_REMOTE, rmt);

                if (tmphref.indexOf(SUB_HREF_LOCAL) > 0) {
                    rmt = "http://" + MainActivity.getHost() + "/" + SYS_DIR + "/";
                    tmphref = tmphref.replace(SUB_HREF_LOCAL, rmt);
                }
                jray.getJSONObject(1).put(FLD_HREF,tmphref);
            }
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
        return(jray);
    }


    protected JSONArray insertDB(String table, JSONObject jsob, String funcid) {

        ContentValues values = new ContentValues();
        JSONArray jray = Util.JSONdbReturn(false, -1, funcid);

        long idval = -1;

        try {
            Iterator<String> itr = jsob.keys();
            String tmpkey;

            while(itr.hasNext()) {
                tmpkey = itr.next();
                values.put(tmpkey, jsob.getString(tmpkey));
            }
            values.put(FLD_CREATED_AT, Util.getTimeNow());

            idval = database.insert(table, null, values);
            if (-1 != idval){
                jray =  Util.JSONdbReturn(true, idval, funcid);
            } else {
                System.out.println("Insert error");
            }
        }
          catch(JSONException ex) {
              ex.printStackTrace();
        }
        return jray;
    }


    protected JSONArray queryDB(String qstr, JSONObject jsob_args, JSONObject jsob_limits,
                                    String funcid) {

        Cursor tmpCursor;
        String[] args = Util.JSONOtoStringArray(jsob_args);
        JSONArray jray = Util.JSONdbReturn(false, -1, funcid);
        JSONObject jsob;

        try {

            if (Util.singleWord(qstr)) {
                qstr = "SELECT * FROM " + qstr + " ";

                Iterator<String> itr = jsob_args.keys();
                Boolean andflg = false;
                while(itr.hasNext()) {

                    if (andflg) {
                        qstr = qstr + " AND ";
                    } else {
                        andflg = true;
                        qstr = qstr + " where ";
                    }
                    qstr = qstr + itr.next() + " = ? ";
                }
            }

            if (jsob_limits.has("limit")) {
                qstr = qstr + " LIMIT " + jsob_limits.getString("limit");
                if (jsob_limits.has("offset")) {
                    qstr = qstr + " OFFSET " + jsob_limits.getString("offset");
                }
            }

          //  System.out.println("query str : "+ qstr + " Limits size: " + jsob_limits.length());
            tmpCursor = database.rawQuery(qstr, args);

            String[] colnames = tmpCursor.getColumnNames();
            JSONArray jArray = Util.JSONdbReturn(true, 0, funcid);

            int reccnt = 0;
            if (tmpCursor.moveToFirst()){

                do {
                    jsob = new JSONObject();

                    for (int i=0; i<colnames.length; i++){
                        if (tmpCursor.getType(i) == Cursor.FIELD_TYPE_INTEGER) {
                            jsob.put(colnames[i], tmpCursor.getLong(tmpCursor.getColumnIndex(colnames[i])));
                        } else if (tmpCursor.getType(i) == Cursor.FIELD_TYPE_FLOAT) {
                            jsob.put(colnames[i], tmpCursor.getFloat(tmpCursor.getColumnIndex(colnames[i])));
                        } else {
                            jsob.put(colnames[i], tmpCursor.getString(tmpCursor.getColumnIndex(colnames[i])));
                        }
                    }
                    jArray.put(jsob);
                    ++reccnt;
                } while(tmpCursor.moveToNext());
            }
            jArray.getJSONObject(0).put("db", reccnt);
            jray = jArray;
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
        return jray;
    }


    protected JSONArray updateDB(String table, JSONObject jsob_values, String qstr, JSONObject jsob_args,
                                     String funcid) {

        ContentValues values = new ContentValues();
        String[] args = Util.JSONOtoStringArray(jsob_args);
        JSONArray jray = Util.JSONdbReturn(false, -1, funcid);

        long idval = -1;

        try {

            Iterator<String> itr = jsob_values.keys();
            String tmpkey;

            while(itr.hasNext()) {
                tmpkey = itr.next();
                values.put(tmpkey, jsob_values.getString(tmpkey));
            }
            values.put(FLD_UPDATED_AT, Util.getTimeNow());

            if (qstr == null || qstr == "null" || qstr.isEmpty()) {
                qstr = "";
                itr = jsob_args.keys();
                Boolean andflg = false;
                while(itr.hasNext()) {

                    if (andflg) {
                        qstr = qstr + " AND ";
                    } else {
                        andflg = true;
                    }
                    qstr = qstr + itr.next() + " = ? ";
                }
            }
            //System.out.println("Update qstr 3 : "+ "x"+qstr+"x");
            idval = database.update(table, values, qstr, args);
            if (-1 != idval){
                jray =  Util.JSONdbReturn(true, idval, funcid);
            } else {
                System.out.println("Update error");
            }
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
        return jray;
    }


    protected JSONArray removeDB(String table, String qstr, JSONObject jsob_args,
                                     String funcid) {

        String[] args = Util.JSONOtoStringArray(jsob_args);
        JSONArray jray = Util.JSONdbReturn(false, -1, funcid);

        long idval = -1;

            if (qstr == null || qstr == "null" || qstr.isEmpty()) {
                Iterator<String> itr = jsob_args.keys();

                qstr = "";
                Boolean andflg = false;
                while(itr.hasNext()) {

                    if (andflg) {
                        qstr = qstr + " AND ";
                    } else {
                        andflg = true;
                    }
                    qstr = qstr + itr.next() + " = ?";
                }
            }
            //System.out.println("Update qstr 3 : "+ "x"+qstr+"x");
            idval = database.delete(table, qstr, args);
            if (-1 != idval){
                jray =  Util.JSONdbReturn(true, idval, funcid);
            } else {
                System.out.println("Delete error");
            }

        return jray;
    }


    // Call only with DB_SUBSERV

    protected Hashtable<Integer, String> getAllDbs() {

        Hashtable<Integer, String> dbs = new Hashtable<Integer, String>();
        String[] args = new String[0];

        try {

            JSONObject qargs = new JSONObject("{ \""+FLD_STATUS+"\": \""+ ACTIVE_STATUS +"\" }");
         //   JSONObject qargs = new JSONObject();
        //   System.out.println("json args : "+ qargs.getInt("id"));

            JSONArray jray = queryDB(TBL_REGISTRY, qargs,
                new JSONObject(), "1");
         //   System.out.println("het all db count : "+jray.length());

            for (int i=1; i < jray.length(); i++){

            dbs.put(i, jray.getJSONObject(i).getString(FLD_TYPE)+
                    jray.getJSONObject(i).getString(FLD_APP));
            }
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }

        return dbs;
    }
}

