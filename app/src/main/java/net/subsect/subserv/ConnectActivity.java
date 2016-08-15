package net.subsect.subserv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.view.MenuItem;
import android.view.Menu;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

import static net.subsect.subserv.Const.*;

/**
 * Created by markkudlac on 2015-03-13.
 */
public class ConnectActivity extends Activity {

    private static ConnectActivity conact = null;
    private static WebView webarg;
    private static boolean newInstall = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conact = this;
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_connect);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int[] value = extras.getIntArray(conact.getString(R.string.webviewctr));
      //      System.out.println("In Bundle received : "+value);

            startBazaar(value);
        } else {
            System.out.println("In Bundle failed");
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        System.out.println("In onDestroy Connect");

    }


    @Override
    public void onResume() {
        super.onResume();

       // System.out.println("In onResume Connect");
        //  onCreate(null);
    }


    @Override
    public void onRestart() {
        super.onRestart();

       // System.out.println("In onRestart Connect");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (newInstall) {
                    onBackPressed();
                    return true;
                } else {
                    finish();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }


    private void mainIntent(){
        super.onBackPressed();

        Intent intent = new Intent(MainActivity.globalactivity, MainActivity.class);
        MainActivity.globalactivity.startActivity(intent);

        finish();
    }


    @Override
    public void onBackPressed() {

        if (newInstall) {
            popConnectDialogue(1);
        } else {
            super.onBackPressed();
            finish();
        }

    }




    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    /*
    public void startConnect(String connectto) {

        System.out.println("Load "+connectto+".subsect.net");

        if (connectto.length() > 0) {
            webarg = (WebView)findViewById(R.id.connectview);
            webarg.setWebChromeClient(new WebChromeClient());

            webarg.setWebViewClient(new WebViewClient() {

                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });

            WebSettings webSettings = webarg.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true); //added to allow local HTML5 storage
            webSettings.setDatabaseEnabled(true); //added to allow local HTML5 storage
            loadConnect(connectto);

        } else {
            Toast.makeText(getBaseContext(), "No connection entered",
                    Toast.LENGTH_LONG).show();
        }
    }


    public static void loadConnect(String connectto){

        webarg.loadUrl("http://" + connectto + ".subsect.net/pkg/Menu");
    }

*/
    public static ConnectActivity getConAct(){

        return conact;
    }


    public void startBazaar(int[] value) {

      //  System.out.println("Start Bazaar");

            webarg = (WebView)findViewById(R.id.connectview);
            webarg.setWebChromeClient(new WebChromeClient());

            WebSettings webSettings = webarg.getSettings();
            webSettings.setJavaScriptEnabled(true);

            webarg.addJavascriptInterface(this.new JsInterface(), "android");

            if (value[0] == R.string.bazaar) {

                if (Prefs.connectSubsect(this)) {
                    webarg.loadUrl("http://" + SOURCE_ADDRESS + "/" + BAZAAR_NAME);

                    if (value[1] == INSTALL_PROMPT_ON){
                        newInstall = true;
                        popConnectDialogue(0);
                    }
                } else {
                  //  System.out.println("Use Bazaar local");
                    webarg.loadUrl("http://"+ Prefs.getNameServer(this) +"/" + BAZAAR_NAME);
                    if (value[1] == INSTALL_PROMPT_ON) {
                        newInstall = true;
                        popConnectDialogue(0);
                    }
                }
            } else if (value[0] == R.string.help){
                webarg.loadUrl("http://"+MainActivity.getHost() + MENU_HELP);
            } else {
                webarg.loadUrl("http://"+MainActivity.getHost() + INSTALLED_PATH);
            }

    }



    private void popConnectDialogue(final int mode){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

  //      final EditText edittext= new EditText(this);

        if (mode == 0) {
            alert.setTitle(R.string.installtitle);
            alert.setMessage(R.string.installprompt);
        } else {
            newInstall = false;
            alert.setTitle("Access Sites");
            alert.setMessage(Html.fromHtml(conact.getString(R.string.urlprompt_1) +
                    "<b>" + Prefs.getHostname(MainActivity.globalactivity) +
                    ".subsect.net</b> " + conact.getString(R.string.urlprompt_2)));
        }
   //     alert.setView(edittext);

  /*      alert.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                startConnect((((TextView)edittext).getText()).toString());
            }
        });
*/
        alert.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                if (mode == 1){
                   mainIntent();
                }
            }
        });

        alert.show();
    }


    public static void updateProg(int percent){
        webarg.loadUrl("javascript:updateProg("+percent+")");
    }


    private final class JsInterface {

        @JavascriptInterface
        public void install(int xid, int filesize) {

            new HttpCom(conact, filesize).execute("serve/" + xid);
        }


        @JavascriptInterface
        public boolean removeSite(int siteid) {

            return (SQLManager.getSQLHelper(DB_SUBSERV).removeSite(siteid));
        }


        @JavascriptInterface
        public String subsectHost() {

            return (MainActivity.getHost());
        }

    }

}
