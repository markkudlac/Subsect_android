package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-02-16.
 */


import android.provider.BaseColumns;

public interface Const extends BaseColumns {

    public static final int BASE_BLOCKSIZE = 65536;

    public static final String SOURCE_ADDRESS = "www.subsect.net";
   // public static final String ALT_ADDRESS = "192.168.1.xxx";
   //public static final int ALT_PORT = 3000;

    public static final int INTERNAL_PORT = 8080;
    public static final String DEMO_NAME = "demo";
    public static final String HOSTAVAILABLE = "hostavailable";
    public static final String HOSTSUBMIT = "hostsubmit";
    public static final String SERVEREMAIL = "serveremail";
    public static final String BAZAAR_NAME = "bazaar";
    public static final String INSTALLED_PATH = "/sys/Menu/installed.html";
    public static final int POLLSERVER_TIME = 90000;
    public static final String PREF_PASSWORD = "Password";
    public static final String PREF_HOSTNAME = "hostname";
    public static final String PREF_PASSLENGTH = "Passlength";
    public static final String PREF_TOKEN = "Token";
    public static final int HASH_LENGTH = 40;

    public static final String HTTP_PROT = "http";
    public static final String SYS_DIR = "sys";
    public static final String USR_DIR = "usr";
    public static final String INSTALL_DIR = "install";
    public static final String INSTALL_FILE = "rootpack.targz";
    public static final String FORMUPLOAD = "formupload";
    public static final String FORMFILENAME = "subupldfile";
    public static final String APP_DBPATH = "/data/net.subsect.subserv/databases/";
    public static final String BKUP_TAR = "bkup.tar";
    public static final String BACKUP = "backup";
    public static final String RESTORE = "restore";
    public static final String LOADFILE_EXT = ".ld";
    public static final String MENU_HELP = "/sys/Menu/help.html";
    public static final int INSTALL_PROMPT_ON = 1;
    public static final int INSTALL_PROMPT_OFF = 0;


    public static final String ARGS_FUNCID = "funcid";
    public static final String ARGS_TABLE = "table";
    public static final String ARGS_SQLPK = "sqlpk";
    public static final String ARGS_PASSWORD = "password";
    public static final String ARGS_DB = "db";
    public static final String ARGS_VALUES = "values";
    public static final String ARGS_QSTR = "qstr";
    public static final String ARGS_ARGS = "args";

    public static final String API_PATH = "/api/";
    public static final String API_SAVEFILE = "savefile";
    public static final String API_DELETEFILE = "deletefile";
    public static final String API_INSERTDB = "insertDB";
    public static final String API_QUERYDB = "queryDB";
    public static final String API_UPDATEDB = "updateDB";
    public static final String API_REMOVEDB = "removeDB";
    public static final String API_GETUPLOADDIR = "getuploaddir/";
    public static final String API_SETUPLOADDIR = "setuploaddir/";
    public static final String API_TESTPASSWORD = "testPassword/";
    public static final String API_GETTOKEN = "getToken/";
    public static final String API_GETIPADD = "getIPadd/";

    public static final int FAIL_PASSWORD = -2;
    public static final String API_GETMENU = "getMenu/";

    public static final String SUBSERV = "subserv";
    public static final String DB_SYS = "S_";
    public static final String DB_USR = "U_";
    public static final String DB_SUBSERV = DB_SYS + SUBSERV;
    public static final String SUB_HREF_REMOTE = "Sub_Href_Remote";
    public static final String SKIP_SCHEMA = "#skip";

    public static final int PERM_SUPER = 0;
    public static final int PERM_GROUP = 1;
    public static final int PERM_USER = 2;
    public static final int PERM_CR = 8;
    public static final int PERM_RE = 4;
    public static final int PERM_UP = 2;
    public static final int PERM_DE = 1;

    public static final String FLD_ID = "id";
    public static final String FLD_STATUS = "status";
    public static final String ACTIVE_STATUS = "A";
    public static final String DELETE_STATUS = "D";
    public static final String FLD_CREATED_AT = "created_at";
    public static final String FLD_UPDATED_AT = "updated_at";
    public static final int CURRENT_DB_VERSION = 1;
    public static final int FIXED_DB_VERSION = 1;

    public static final String TBL_REGISTRY = "registry";
    public static final String FLD_APP = "app";
    public static final String FLD_TITLE = "title";
    public static final String FLD_TYPE = "type";
    public static final String FLD_SUBSECTID = "subsectid";
    public static final String FLD_ICON = "icon";
    public static final String FLD_PERMISSIONS = "permissions";
    public static final String FLD_HREF = "href";

    public static final String TBL_SECURE = "secure";
    public static final String FLD_DBNAME = "dbname";
    public static final String FLD_TABLENAME = "tablename";
}
