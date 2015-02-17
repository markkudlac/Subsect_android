package net.subsect.subserv;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import elonen.SimpleWebServer;


public class MainActivity extends Activity {

    private static SimpleWebServer HttpdServ = null;
    private static SQLHelper SubservDb = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startdb();
        turnServerOn(this);

        WebView serverjs = (WebView)findViewById(R.id.serverJS);
        WebSettings webSettings = serverjs.getSettings();
        webSettings.setJavaScriptEnabled(true);
        serverjs.loadUrl("file:///android_asset/index.html");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        System.out.println("In DESTROY");
        stopHttpdServer();
        stopDb();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            System.out.println("Got configuration change : Landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            System.out.println("Got configuration change : Portrait");
        }
    }

    public void startHttpdServer(int httpdPort, String ipadd) {

        try {
            stopHttpdServer();
            HttpdServ = new SimpleWebServer(ipadd, httpdPort, getFilesDir(), this);
            HttpdServ.start();
            System.out.println("HttpdServ started Add : " + ipadd + "  Port : "
                    + httpdPort);
        } catch (Exception ex) {
            System.out.println("HttpdServer error  : " + ex);
        }
    }


    public  void turnServerOn(final Context ctx) {

        stopHttpdServer();
        new Thread(new Runnable() {
            public void run() {
                try {
                    String ipad = Util.getHTTPAddress(ctx);
                    startHttpdServer(8080, ipad);

                } catch (Exception ex) {
                    System.out.println("Select thread exception : " + ex);
                }
            }
        }).start();
    }


    public void stopHttpdServer() {

        try {
            if (HttpdServ != null) {
                HttpdServ.stop();
                HttpdServ = null;
            }

        } catch (Exception ex) {
            System.out.println("HttpdServer stop : " + ex);
        }
    }


    private void startdb(){
        stopDb();
        SubservDb = new SQLHelper(this);
    }


    private void stopDb(){

        if (SubservDb != null){
            SubservDb.closeDb();
            SubservDb = null;
        }
    }
}
