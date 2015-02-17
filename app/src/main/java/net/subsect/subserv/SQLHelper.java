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
import android.os.SystemClock;


public class SQLHelper extends SQLiteOpenHelper {

    private static SQLiteDatabase database = null;
    private static Context srvcontext;

    public SQLHelper(Context context) {

        super(context, DB_NAME, null, CURRENT_DB_VERSION);

        srvcontext = context;

        try {
            database = getWritableDatabase();
        } catch (SQLiteException e) {
            System.out.println("SQLiteException");
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("In onCreate db");
        Util.versionChangeHTML(srvcontext);
        dropAndCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        System.out.println("In onUpgrade db old : " + oldVersion + " new : " + newVersion);

        if (newVersion >= CURRENT_DB_VERSION) {
            Util.versionChangeHTML(srvcontext);
        }
    }

    protected void dropAndCreate(SQLiteDatabase db) {

        createTables(db);
    }

    protected void createTables(SQLiteDatabase db) {

        System.out.println("In createTables");

        try {
            db.execSQL(
                    "create table " + TABLE_DEVICE + " ( " +
                            FLD_ID + " integer primary key autoincrement, " +
                            FLD_TAG + " text, " +
                            FLD_STATUS + " char(1) default \'A\', " +
                            FLD_CREATED_AT + " integer default 0, " +
                            FLD_UPDATED_AT + " integer default 0 " +
                            ")"
            );


            initializeDevice(db);

            System.out.println("Out createTables");
        } catch (SQLException e) {
            System.out.println("SQLException create");
        }
    }


    protected void closeDb() {
        database.close();
        database = null;
    }


    private static void initializeDevice(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        System.out.println("nIn initialize Device");
        values.put(FLD_TAG, "TEST_DEVICE_ID_0");
        values.put(FLD_CREATED_AT, Util.getTimeNow());

        if (-1 == db.insert(TABLE_DEVICE, null, values)) {
            System.out.println("device insert error");
        }
    }
}

