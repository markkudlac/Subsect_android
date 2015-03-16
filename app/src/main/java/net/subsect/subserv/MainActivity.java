package net.subsect.subserv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import elonen.SimpleWebServer;
import static net.subsect.subserv.Const.*;

public class MainActivity extends Activity {

    private static SimpleWebServer HttpdServ = null;
    private static SQLManager SubservDb = null;
    private static String hostadd = null;
    private static int hostport = 8080;
    private static TextView androidout;
    //private static WebView serverjs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidout = (TextView)findViewById(R.id.androidout);

        startdb();
        turnServerOn(this);

            WebView serverjs = (WebView) findViewById(R.id.serverJS);
            WebSettings webSettings = serverjs.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);

            serverjs.addJavascriptInterface(this.new JsInterface(), "android");

            serverjs.loadUrl("file:///android_asset/index.html?subhost=" + Prefs.getHostname(this) +
                    "&subnamesrv=" + Prefs.getNameServer(this));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        System.out.println("In onDestroy");
        stopHttpdServer();
        stopDb();
    }


    @Override
    public void onResume() {
        super.onResume();

        System.out.println("In onResume");
      //  onCreate(null);
    }


    @Override
    public void onRestart() {
        super.onRestart();

        System.out.println("In onRestart");
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
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

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.connect) {

            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.server) {
            Toast.makeText(getBaseContext(), "Server",
                    Toast.LENGTH_LONG).show();

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
        hostadd = Util.getHTTPAddress(ctx);

        String fullhost = hostadd + ":" + hostport;
        androidout.setText("HOST : " + fullhost);

        new Thread(new Runnable() {
            public void run() {
                try {
                    startHttpdServer(hostport, hostadd);

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
        SubservDb = new SQLManager(this);
        SQLManager.openAll();
    }


    private void stopDb(){

        if (SubservDb != null){
            SQLManager.closeAll();
            SubservDb = null;
        }
    }


    //THis could be eliminated but leaving here as example
    // Pass in as a param to webview

    private final class JsInterface {

        @JavascriptInterface
        public String localhostaddress(){

            String fullhost = hostadd + ":" + hostport;
         //   androidout.setText("HOST 2 : " + fullhost);
            return(fullhost);
        }

    }

}
