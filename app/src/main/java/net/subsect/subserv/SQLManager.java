package net.subsect.subserv;

import android.content.Context;

import java.util.Enumeration;
import java.util.Hashtable;

import static net.subsect.subserv.Const.*;

/**
 * Created by markkudlac on 2015-03-14.
 */
public class SQLManager {

    static private Hashtable<String, SQLHelper> dbs;
    static private Context context;

    public SQLManager(Context context) {

        dbs = new Hashtable<String, SQLHelper>();
        this.context = context;
    }


    static public void openAll() {
        openDb(DB_SUBSERV, CURRENT_DB_VERSION);
        Enumeration<String> dbs = getSQLHelper(DB_SUBSERV).getAllDbs().elements();

        String tmp;
        while(dbs.hasMoreElements()){
            tmp = dbs.nextElement();
            System.out.println("In openAll : "+tmp);
            openDb(tmp, FIXED_DB_VERSION);
        }


    }


    static public void openDb(String dbname, int dbversion) {
        if (!dbs.containsKey(dbname)) {
            dbs.put(dbname, new SQLHelper(context, dbname, dbversion));
        }
    }


    static public void closeAll() {
        Enumeration<SQLHelper> db = dbs.elements();

        while(db.hasMoreElements()){
     //       System.out.println("In closeAll");
            db.nextElement().closeDb();
        }
    }


    static public SQLHelper getSQLHelper(String dbname) {

        return(dbs.get(dbname));
    }
}