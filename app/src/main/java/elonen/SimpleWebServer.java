package elonen;


import android.content.Context;

import net.subsect.subserv.Prefs;
import net.subsect.subserv.SQLManager;
import net.subsect.subserv.Util;
import net.subsect.subserv.Routes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;


import static net.subsect.subserv.Const.API_PATH;
import static net.subsect.subserv.Const.BASE_BLOCKSIZE;
import static net.subsect.subserv.Const.DB_SUBSERV;
import static net.subsect.subserv.Const.FORMUPLOAD;
import static net.subsect.subserv.Const.FORMFILENAME;


public class SimpleWebServer extends NanoHTTPD {
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {{
        put("css", "text/css");
        put("htm", "text/html");
        put("html", "text/html");
        put("ngml", "text/ng-template");    //add to support AngularJS
        put("xml", "text/xml");
        put("txt", "text/plain");
        put("asc", "text/plain");
        put("js",  "text/javascript");
        put("json", "application/json");  // added mark to support JQuery getJSON
        put("gif", "image/gif");
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("png", "image/png");
        put("mp3", "audio/mpeg");
        put("m4a", "audio/mpeg");		// Mark added this for apple
        put("m3u", "audio/mpeg-url");
        put("mp4", "video/mp4");
        put("ogv", "video/ogg");
        put("flv", "video/x-flv");
        put("mov", "video/quicktime");
        put("swf", "application/x-shockwave-flash");
        put("pdf", "application/pdf");
        put("doc", "application/msword");
        put("ogg", "application/x-ogg");
        put("zip", "application/octet-stream");
        put("exe", "application/octet-stream");
        put("class", "application/octet-stream");
    }};

 
    private File rootDir;
    private Context context;
    
    public SimpleWebServer(String host, int port, File wwwroot, Context context) {
        super(host, port);
        this.rootDir = wwwroot;
        this.context = context;
    }

    public File getRootDir() {
        return rootDir;
    }

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20' instead of '+'.
     */
    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/"))
                newUri += "/";
            else if (tok.equals(" "))
                newUri += "%20";
            else {
                try {
                    newUri += URLEncoder.encode(tok, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        }
        return newUri;
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers and HTTP parameters.
     */
    public Response serveFile(String uri, Map<String, String> header, String qryString, File homeDir) {
        Response res = null;

 //       System.out.println("uri in serveFile 2 : "+uri + "  qry : " + qryString);
        
        if (uri.indexOf(API_PATH) == 0 || uri.endsWith(FORMUPLOAD)){
        	String msg;
        	
 //       	System.out.println("uri has api");
        	msg = Routes.callMethods(uri, qryString, context, getRootDir());
        	
        	res = new Response(msg);
        } else if (!homeDir.isDirectory())
            // Make sure we won't die of an exception later
            res = new Response(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");

        if (res == null) {
            // Remove URL arguments
            uri = uri.trim().replace(File.separatorChar, '/');
            if (uri.indexOf('?') >= 0)
                uri = uri.substring(0, uri.indexOf('?'));

            // Prohibit getting out of current directory
            if (uri.startsWith("src/main") || uri.endsWith("src/main") || uri.contains("../"))
                res = new Response(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Won't serve ../ for security reasons.");
        }

        /******* Added to check passwords *****/
        if (uri.endsWith(".html")){

            String pkgName = uri.split("/")[2];

 //           System.out.println("uri has .html  pkgName : " + pkgName);
            if (uri.endsWith(pkgName.toLowerCase() + ".html")) {

                String perm = SQLManager.getSQLHelper(DB_SUBSERV).getPermission(pkgName);
                String[] passwd = qryString.split("=");
         //       System.out.println("Permission : " + perm );

                String[] permary = perm.split("");
                if (!(permary[3].equals("F")
                        || (passwd.length == 2 && passwd[0].equals("SUBSECT_passwd") &&
                        passwd[1].length() == 40 && passwd[1].equals(Prefs.getPassword(context))))){
                 //   System.out.println("Login is required 3");

                    res = new Response(Response.Status.OK, "text/html",
                        "<html><head></head><body><div>LOGIN REQUIRED</div><script>" +
                        "window.location.hash=\"login\"; " +   //Must set anchor 1st
                        "window.location.pathname=\"/pkg/Menu\"; " +
                        "</script></body></html>");
                }
            }
        }

        File f = new File(homeDir, uri);
        if (res == null && !f.exists())
            res = new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");

        // List the directory, if necessary
        if (res == null && f.isDirectory()) {
            // Browsers get confused without '/' after the
            // directory, send a redirect.
            if (!uri.endsWith("/")) {
                uri += "/";
                res = new Response(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML, "<html><body>Redirected: <a href=\"" + uri + "\">" + uri
                        + "</a></body></html>");
                res.addHeader("Location", uri);
            }

            if (res == null) {
                // First try index.html and index.htm
                if (new File(f, "index.html").exists())
                    f = new File(homeDir, uri + "/index.html");
                else if (new File(f, "index.htm").exists())
                    f = new File(homeDir, uri + "/index.htm");
                    // No index file, list the directory if it is readable
                else if (f.canRead()) {
                    String[] files = f.list();
                    String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";

                    if (uri.length() > 1) {
                        String u = uri.substring(0, uri.length() - 1);
                        int slash = u.lastIndexOf('/');
                        if (slash >= 0 && slash < u.length())
                            msg += "<b><a href=\"" + uri.substring(0, slash + 1) + "\">..</a></b><br/>";
                    }

                    if (files != null) {
                        for (int i = 0; i < files.length; ++i) {
                            File curFile = new File(f, files[i]);
                            boolean dir = curFile.isDirectory();
                            if (dir) {
                                msg += "<b>";
                                files[i] += "/";
                            }

                            msg += "<a href=\"" + encodeUri(uri + files[i]) + "\">" + files[i] + "</a>";

                            // Show file size
                            if (curFile.isFile()) {
                                long len = curFile.length();
                                msg += " &nbsp;<font size=2>(";
                                if (len < 1024)
                                    msg += len + " bytes";
                                else if (len < 1024 * 1024)
                                    msg += len / 1024 + "." + (len % 1024 / 10 % 100) + " KB";
                                else
                                    msg += len / (1024 * 1024) + "." + len % (1024 * 1024) / 10 % 100 + " MB";

                                msg += ")</font>";
                            }
                            msg += "<br/>";
                            if (dir)
                                msg += "</b>";
                        }
                    }
                    msg += "</body></html>";
                    res = new Response(msg);
                } else {
                    res = new Response(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: No directory listing.");
                }
            }
        }

        try {
            if (res == null) {
                // Get MIME type from file name extension, if possible
                String mime = null;
                int dot = f.getCanonicalPath().lastIndexOf('.');
                if (dot >= 0)
                    mime = MIME_TYPES.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
                if (mime == null)
                    mime = NanoHTTPD.MIME_DEFAULT_BINARY;

                // Calculate etag
                String etag = Integer.toHexString((f.getAbsolutePath() + f.lastModified() + "" + f.length()).hashCode());

                // Support (simple) skipping:
                long startFrom = 0;
                long endAt = -1;
                String range = header.get("range");
                if (range != null) {
                    if (range.startsWith("bytes=")) {
                        range = range.substring("bytes=".length());
                        int minus = range.indexOf('-');
                        try {
                            if (minus > 0) {
                                startFrom = Long.parseLong(range.substring(0, minus));
                                endAt = Long.parseLong(range.substring(minus + 1));
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                // Change return code and add Content-Range header when skipping is requested
                long fileLen = f.length();
                if (range != null && startFrom >= 0) {
                    if (startFrom >= fileLen) {
                        res = new Response(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                        res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                        res.addHeader("ETag", etag);
                    } else {
                        if (endAt < 0)
                            endAt = fileLen - 1;
                        long newLen = endAt - startFrom + 1;
                        if (newLen < 0)
                            newLen = 0;

                        final long dataLen = newLen;
                        FileInputStream fis = new FileInputStream(f) {
                            @Override
                            public int available() throws IOException {
                                return (int) dataLen;
                            }
                        };
                        fis.skip(startFrom);

                        res = new Response(Response.Status.PARTIAL_CONTENT, mime, fis);
                        res.addHeader("Content-Length", "" + dataLen);
                        res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                        res.addHeader("ETag", etag);
    //                    res.addHeader("Access-Control-Allow-Origin", "*");		Allow cross scripting Mark
                    }
                } else {
                    if (etag.equals(header.get("if-none-match")))
                        res = new Response(Response.Status.NOT_MODIFIED, mime, "");
                    else {
                        res = new Response(Response.Status.OK, mime, new FileInputStream(f));
                        res.addHeader("Content-Length", "" + fileLen);
                        res.addHeader("ETag", etag);
                    }
                }
            }
        } catch (IOException ioe) {
            res = new Response(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }

        res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
        res.addHeader("Access-Control-Allow-Origin", "*");		//Allow cross scripting Mark
        return res;
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        String filename = null;
        String qryString = "";
//        boolean formup_flg = false;
    	
    	System.out.println(method + " '" + uri + "' ");
//    	formup_flg = uri.endsWith(FORMUPLOAD);
    	
    	
        Iterator<String> e = header.keySet().iterator();
        while (e.hasNext()) {
            String value = e.next();
        }
        e = parms.keySet().iterator();
        while (e.hasNext()) {
            String value = e.next();
            if (value.equals(FORMFILENAME)) {
            	filename = parms.get(value);
            } else if (value.equals(QUERY_STRING_PARAMETER)){
            	qryString = parms.get(value);
            //	System.out.println("Param string : "+ qryString);
            }
        }
        e = files.keySet().iterator();
        while (e.hasNext()) {
            String value = e.next();
 //           System.out.println("  UPLOADED: '" + value + "' = '" + files.get(value) + "'");
            
      //Added for transfer of upload files. This should be reviewed later
      // There is no error handling here

            if (value.equals(FORMFILENAME) && filename.length() > 0) {
            	copyFile(files.get(value), filename);
            }
        }

        return serveFile(uri, header, qryString, getRootDir());
    }

    
    public void copyFile(String targ, String dest) {
    	
 //   	System.out.println(" Copy temp file HTTPD in: " + targ + "  out : " + dest);
    	
    	
    	try {
    		byte [] xbuf = new byte[BASE_BLOCKSIZE];  
    		File fl_dest;
    		String upld;
    		
    		fl_dest = new File(rootDir, Util.getUploadDirectory());
    		if (!fl_dest.exists()) {
    			fl_dest.mkdirs();
            }
    		
    		upld = Util.getUploadDirectory() + "/" + dest;
//System.out.println("Dest path : "+upld);
    		
    		fl_dest = new File(rootDir, upld);
    		if (fl_dest.exists()){
    			fl_dest.delete();
    		}
    		fl_dest.createNewFile();
    		// Copy contents of temp over to files
    		
        	InputStream in = new FileInputStream(new File(targ));
    	    OutputStream out = new FileOutputStream(fl_dest);
//       		System.out.println("Create output");
    	    
    	    
    	    // Transfer bytes from in to out
    	    int len;
    	    while ((len = in.read(xbuf)) > 0) {
    	        out.write(xbuf, 0, len);
    	    }
    	    in.close();
    	    out.close();
    	    
    	} catch (IOException e) {
    		System.out.println( "File I/O error " + e);
    	}
    }
    

}
