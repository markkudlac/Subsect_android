package net.subsect.subserv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by markkudlac on 2015-03-13.
 */
public class ConnectActivity extends Activity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_connect);

        popConnectDialogue();
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
            WebView webarg = (WebView)findViewById(R.id.connectview);
            WebSettings webSettings = webarg.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webarg.loadUrl("http://" + connectto + ".subsect.net/app/TestApp");
        } else {
            Toast.makeText(getBaseContext(), "No connection entered",
                    Toast.LENGTH_LONG).show();
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
}
