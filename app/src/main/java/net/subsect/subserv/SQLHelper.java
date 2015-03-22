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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

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
                                FLD_STATUS + " char(1) default \'A\', " +
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

        System.out.println("In processTables : " + Util.getAppfromDb(dbnm));
        for (int i = 0; i < schemafls.length; i++) {
            sqlst = Util.getSchema(context, dbnm, schemafls[i]);

            if (sqlst.length() > 0) db.execSQL(sqlst);
        }
     //   initializeRegistry(db, app, sys);
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


    public static void initializeRegistry(SQLiteDatabase db, String app, boolean sys) {
        ContentValues values = new ContentValues();

        System.out.println(TBL_REGISTRY + " app : "+ app);
                values.put(FLD_APP, app);

        if (sys) {
            values.put(FLD_TYPE, DB_SYS);
        } else {
            values.put(FLD_TYPE, DB_USR);
        }
        values.put(FLD_CREATED_AT, Util.getTimeNow());

        if (-1 == db.insert(TBL_REGISTRY, null, values)) {
            System.out.println(TBL_REGISTRY + " insert error");
        }
    }


    protected String insertDB(String table, JSONObject jsob, String funcid) {

        ContentValues values = new ContentValues();
        String msg = Util.JSONdbReturn(false, -1, funcid);

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
                msg =  Util.JSONdbReturn(true, idval, funcid);
            } else {
                System.out.println("Insert error");
            }
        }
          catch(JSONException ex) {
              ex.printStackTrace();
        }

        return msg;
    }


    protected String queryDB(String qstr, JSONObject jsob_args, JSONObject jsob_limits,
                                    String funcid) {

        Cursor tmpCursor;
        String[] args = Util.JSONOtoStringArray(jsob_args);
        String msg = Util.JSONdbReturn(false, -1, funcid);
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
                    }
                }
            }

            if (jsob_limits.has("limit")) {
                qstr = qstr + " LIMIT " + jsob_limits.getString("limit");
                if (jsob_limits.has("offset")) {
                    qstr = qstr + " OFFSET " + jsob_limits.getString("offset");
                }
            }

            System.out.println("query str : "+ qstr + " Limits size: " + jsob_limits.length());


            tmpCursor = database.rawQuery(qstr, args);

            JSONArray jArray =  new JSONArray();
            String[] colnames = tmpCursor.getColumnNames();

            jsob = new JSONObject();
            jsob.put("rtn", true);
            jsob.put("db", 0);
            jsob.put("funcid", funcid);
            jArray.put(jsob);

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
            msg = jArray.toString().replace("\\", "");
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
        return msg;
    }


    protected String updateDB(String table, JSONObject jsob_values, String qstr, JSONObject jsob_args,
                                     String funcid) {

        ContentValues values = new ContentValues();
        String[] args = Util.JSONOtoStringArray(jsob_args);
        String msg = Util.JSONdbReturn(false, -1, funcid);

        long idval = -1;
        //System.out.println("insert sqlpk : "+ sqlpk);

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
                    qstr = qstr + itr.next() + " = ?";
                }
            }
            //System.out.println("Update qstr 3 : "+ "x"+qstr+"x");
            idval = database.update(table, values, qstr, args);
            if (-1 != idval){
                msg =  Util.JSONdbReturn(true, idval, funcid);
            } else {
                System.out.println("Update error");
            }
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
        return msg;
    }


    protected String removeDB(String table, String qstr, JSONObject jsob_args,
                                     String funcid) {

        String[] args = Util.JSONOtoStringArray(jsob_args);
        String msg = Util.JSONdbReturn(false, -1, funcid);

        long idval = -1;
        //System.out.println("insert sqlpk : "+ sqlpk);

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
                msg =  Util.JSONdbReturn(true, idval, funcid);
            } else {
                System.out.println("Delete error");
            }

        return msg;
    }


    // Call only with DB_SUBSERV

    protected Hashtable<Integer, String> getAllDbs() {

        Hashtable<Integer, String> dbs = new Hashtable<Integer, String>();
        Cursor tmpCursor;
        String[] args = new String[0];

        tmpCursor = database.rawQuery("SELECT * FROM " + TBL_REGISTRY, args);

        int reccnt = 0;
        String dbval;

        if (tmpCursor.moveToFirst()){
            dbval = tmpCursor.getString(tmpCursor.getColumnIndex(FLD_TYPE));
            dbval = dbval + tmpCursor.getString(tmpCursor.getColumnIndex(FLD_APP));

            dbs.put(reccnt, dbval);
        }

        return dbs;
    }
}

