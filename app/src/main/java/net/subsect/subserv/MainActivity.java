package net.subsect.subserv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.TextView;
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
    private static WebView serverjs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        androidout = (TextView)findViewById(R.id.androidout);

        startdb();
        turnServerOn(this);

            serverjs = (WebView)findViewById(R.id.serverJS);
            serverjs.setWebChromeClient(new WebChromeClient());

            WebSettings webSettings = serverjs.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
            webSettings.setDomStorageEnabled(true); //added to allow local HTML5 storage
            webSettings.setDatabaseEnabled(true); //added to allow local HTML5 storage
          //  serverjs.addJavascriptInterface(this.new JsInterface(this), "android");

      //  serverjs.loadUrl("file:///android_asset/test.html");

      //      serverjs.loadUrl("file:///android_asset/index.html?subhost=" + Prefs.getHostname(this) +
      //              "&subnamesrv=" + Prefs.getNameServer(this) + "&fullhost="+getHost());

        loadServer(this);
        if (Prefs.pollServer(this)) statusWatch(this);

    }


    public static void loadServer(MainActivity xthis){

        serverjs.loadUrl("file:///android_asset/index.html?subhost=" + Prefs.getHostname(xthis) +
                "&subnamesrv=" + Prefs.getNameServer(xthis) + "&fullhost="+getHost());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        System.out.println("In onDestroy");
      //  stopHttpdServer();
      //  stopDb();
    }


    @Override
    public void onResume() {
        super.onResume();

        System.out.println("In onResume");
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
            intent.putExtra(getString(R.string.webviewctr), R.string.connect);
            startActivity(intent);
            return true;
        } else if (id == R.id.subzaar) {
            Intent intent = new Intent(this, ConnectActivity.class);
            intent.putExtra(getString(R.string.webviewctr), R.string.subzaar);
            startActivity(intent);
            return true;
        } else if (id == R.id.installed) {
            Intent intent = new Intent(this, ConnectActivity.class);
            intent.putExtra(getString(R.string.webviewctr), R.string.installed);
            startActivity(intent);
            return true;
        } else if (id == R.id.tools) {
            Intent intent = new Intent(this, ToolsActivity.class);
            startActivity(intent);
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


    public static String getHost(){
        return(hostadd + ":" + hostport);
    }


    public  void turnServerOn(final Context ctx) {

        stopHttpdServer();
        hostadd = Util.getHTTPAddress(ctx);

        androidout.setText("HOST : " + getHost());

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


    private void statusWatch(final MainActivity mainact){
        System.out.println("In statusWatch 1");

        new Thread(new Runnable() {

         //   System.out.println("In statusWatch 2");

            @Override
            public void run() {
                int cnt = 0;
                System.out.println("In statusWatch 3");
                try {
                    while (true) {
                        System.out.println("Thread loop : " + cnt);

                        Thread.sleep(30000);
                        new HttpStat(mainact).execute("control/"+Prefs.getHostname(mainact));
                        ++cnt;
                    }
                } catch (Exception ex) {
                    System.out.println("Thread exception : " + ex);
                }
            }
        }).start();
    }


    private final class JsInterface {

        private Context contx;

        public JsInterface(Context contx) {
            this.contx = contx;

        }
/*
        @JavascriptInterface
        public void transferPeer(final String peerstr) {

            System.out.println("Transfer peer : " + peerstr);

            runOnUiThread(new Runnable() {
                public void run() {
                    try {

                //        poller.loadUrl("javascript:setPeer(\""+ peerstr +"\")");

                    } catch (Exception ex) {
                        System.out.println("Select thread exception : " + ex);
                    }
                }
            });

        }
*/
        /*
        @JavascriptInterface
        public void callToggle(final int toggle) {

            System.out.println("callToggle peer : " + toggle);

            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        WebView tmpvw;

                        if (toggle % 2 == 1) {
                            System.out.println("callToggle poller");
                            tmpvw = poller;
                        } else {
                            System.out.println("callToggle serverjs");
                            tmpvw =serverjs;
                        }

                        tmpvw.loadUrl("javascript:toggleOn("+ toggle + ")");
                        tmpvw.loadUrl("file:///android_asset/index.html?subhost=" + Prefs.getHostname(contx) +
                                "&subnamesrv=" + Prefs.getNameServer(contx) + "&fullhost="+getHost()+
                                "&toggle="+toggle);
                    } catch (Exception ex) {
                        System.out.println("Select thread exception : " + ex);
                    }
                }
            });

        }
        */
    }
}
