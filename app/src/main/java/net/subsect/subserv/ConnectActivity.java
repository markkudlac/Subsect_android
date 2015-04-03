package net.subsect.subserv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static net.subsect.subserv.Const.*;

/**
 * Created by markkudlac on 2015-03-13.
 */
public class ConnectActivity extends Activity {

    private static ConnectActivity conact;
    private static WebView webarg;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conact = this;
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_connect);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int value = extras.getInt(conact.getString(R.string.webviewctr));
            System.out.println("In Bundle received : "+value);

            if (value == R.string.connect) {
                popConnectDialogue();
            } else {
                startSubzaar(value);
            }
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

        System.out.println("In onResume Connect");
        //  onCreate(null);
    }


    @Override
    public void onRestart() {
        super.onRestart();

        System.out.println("In onRestart Connect");

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id==android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    public void startConnect(String connectto) {

        System.out.println("Load "+connectto+".subsect.net");

        if (connectto.length() > 0) {
            webarg = (WebView)findViewById(R.id.connectview);
            webarg.setWebChromeClient(new WebChromeClient());

            WebSettings webSettings = webarg.getSettings();
            webSettings.setJavaScriptEnabled(true);

            webarg.loadUrl("http://" + connectto + ".subsect.net/app/Menu");
        } else {
            Toast.makeText(getBaseContext(), "No connection entered",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void startSubzaar(int value) {

        System.out.println("Start Subzaar");

            webarg = (WebView)findViewById(R.id.connectview);
            webarg.setWebChromeClient(new WebChromeClient());

            WebSettings webSettings = webarg.getSettings();
            webSettings.setJavaScriptEnabled(true);

            webarg.addJavascriptInterface(this.new JsInterface(), "android");

            if (value == R.string.subzaar) {
               // webarg.loadUrl("http://192.168.1.103:3000/subzaar"); //Only program nor download
                webarg.loadUrl("http://www.subsect.net/subzaar");
            } else {
                webarg.loadUrl("http://"+MainActivity.getHost()+"/sys/Menu/menu.html");
            }
    }

    public void popConnectDialogue(){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext= new EditText(this);
        alert.setMessage("Enter Host");
        alert.setTitle("Connect To");

        alert.setView(edittext);

        alert.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                startConnect((((TextView)edittext).getText()).toString());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                Toast.makeText(getBaseContext(), "Cancelled",
                        Toast.LENGTH_LONG).show();
            }
        });

        alert.show();
    }


    public static void updateProg(int percent){
        webarg.loadUrl("javascript:updateProg("+percent+")");
    }


    private final class JsInterface {

        @JavascriptInterface
        public void install(int xid, int filesize){

            new HttpCom(conact, filesize).execute("serve/"+xid);
        }


        @JavascriptInterface
        public boolean removeSite(int siteid) {

            return(SQLManager.getSQLHelper(DB_SUBSERV).removeSite(siteid));
        }
    }

}
