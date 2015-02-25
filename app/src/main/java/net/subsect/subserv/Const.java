package net.subsect.subserv;

/**
 * Created by markkudlac on 2015-02-16.
 */
import android.provider.BaseColumns;

public interface Const extends BaseColumns {

    public static final int BASE_BLOCKSIZE = 65536;

    public static final String SOURCE_ADDRESS = "www.subsect.net";


    public static final String HTTP_PROT = "http";
    public static final String SYSHTML_DIR = "SysHtml";
    public static final String USERHTML_DIR = "UserHtml";
//    public static final String ADS_DIR = "ads";
//    public static final String LANDING_DIR = "landing";
    public static final String FORMUPLOAD = "formupload";

    public static final String API_PATH = "/api/";
    public static final String API_SAVEFILE = "savefile";
    public static final String API_PROCSQL = "procsql/";
    public static final String API_GETUPLOADDIR = "getuploaddir/";

    public static final String DB_NAME = "subserv";
    public static final String FLD_ID = "id";
    public static final String FLD_STATUS = "status";
    public static final String FLD_CREATED_AT = "created_at";
    public static final String FLD_UPDATED_AT = "updated_at";
    public static final int CURRENT_DB_VERSION = 4;

    public static final String TABLE_DEVICE = "device";
    public static final String FLD_TAG= "tag";
}
