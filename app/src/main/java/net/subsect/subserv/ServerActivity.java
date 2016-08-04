package net.subsect.subserv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import static net.subsect.subserv.Const.*;

/**
 * Created by markkudlac on 16-07-15.
 */
public class ServerActivity extends Activity {

    private static ServerActivity serveract;
    private Button butSubmit;

    private static EditText hostText;
    private static EditText passwordText;
    private static EditText emailText;
    private static String passHash = "";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serveract = this;

        setContentView(R.layout.activity_server);
        addListenerOnButton();

        configEditFields();

        setTextFields();

    }


    private void addListenerOnButton() {

        butSubmit = (Button) findViewById(R.id.butSubmit);

        butSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (validate()) {

                    String opasswd = Prefs.getPassword(serveract);

                    if (passwordText.getText().toString().contains("-")) {
                        passHash = opasswd;
                    } else {
                        passHash = Util.getSha1Hex(hostText.getText().toString() +
                                passwordText.getText().toString());
                    }

                    // Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    String deviceId = Settings.Secure.getString(serveract.getContentResolver(),
                            Settings.Secure.ANDROID_ID);

                    if (opasswd.length() < HASH_LENGTH &&
                            Prefs.getHostname(serveract).length() == 0){

                        opasswd = passHash;
                        System.out.println("Set opasswd : " + opasswd);
                    }

                    //   System.out.println("Submit Button : " + passwd);
                    new HttpSetup(HOSTSUBMIT).
                            execute(HOSTSUBMIT + "/" + hostText.getText().toString() +
                                    "/" + passHash + "?contact=" + emailText.getText().toString() +
                                    "&deviceid=" + deviceId + "&opasswd=" + opasswd);
                }
            }

        });
    }


    private void configEditFields() {

        hostText = (EditText) findViewById(R.id.input_host);
        passwordText = (EditText) findViewById(R.id.input_password);
        emailText = (EditText) findViewById(R.id.input_email);

        hostText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus && !(hostText.getText().toString().
                        equals(Prefs.getHostname(serveract)))) {

                    new HttpSetup(HOSTAVAILABLE).
                            execute(HOSTAVAILABLE + "/" + ((EditText) v).getText().toString());
                }
            }
        });

        passwordText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (((EditText) v).getText().toString().contains("-")) {
                    ((EditText) v).setText("", TextView.BufferType.EDITABLE);
                    if (!hasFocus) poperrorDialogue("Contains invalid character:  '-'");
                }
            }
        });
    }


    public void setTextFields() {

        String hostbuf = Prefs.getHostname(serveract);
        String passbuf;

        if (hostbuf.length() == 0) return;

        ((TextView) findViewById(R.id.help_host)).setVisibility(View.GONE);

        if (Prefs.getPassLength(serveract) > 0 &&
                Prefs.getPassword(serveract).length() == HASH_LENGTH) {
            passbuf = new String(new char[Prefs.getPassLength(serveract)]).replace("\0", "-");
        } else{
            passbuf = "";
        }
        hostText.setText(hostbuf, TextView.BufferType.EDITABLE);
        hostText.setEnabled(false);
        passwordText.setText(passbuf, TextView.BufferType.EDITABLE);

        new HttpSetup(SERVEREMAIL).
                execute(HOSTAVAILABLE + "/" + hostbuf);

    }

    public static void hostavailable(boolean isavail) {

        System.out.println("Available : " + isavail);

        if (isavail) {
            hostText.setError(null);
        } else {
            hostText.setError("Host name is not available");
        }
    }


    public static void hostsubmit(boolean rtn) {

        //   System.out.println("Hostsubmit : " + rtn);

        if (rtn) {

            Toast.makeText(serveract, "Saved", Toast.LENGTH_LONG).show();
            if (Prefs.getHostname(serveract).length() == 0) {
                Prefs.setHostName(serveract, hostText.getText().toString());
            }

            Prefs.setPassword(serveract, passHash);
            Prefs.setPassLength(serveract, passwordText.getText().toString().length());
        } else {
            poperrorDialogue("Submit failed : retry");
        }
    }


    public static void serveremail(String email) {

        //  System.out.println("serveremail : " + email);
        emailText.setText(email, TextView.BufferType.EDITABLE);
    }


    public boolean validate() {
        boolean valid = true;

        String host = hostText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (host.isEmpty() || (host.length() < 4 && !host.equals("dev"))) {
            hostText.setError("at least 4 lowercase alphanumeric");
            valid = false;
        } else {
            hostText.setError(null);
        }

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Invalid email format");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            passwordText.setError("between 4 and 20 alphanumeric and underscore");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }


    private static void poperrorDialogue(String message) {

        AlertDialog.Builder alert = new AlertDialog.Builder(serveract);

      //  final EditText edittext = new EditText(serveract);
        alert.setMessage(message);
        alert.setTitle("ERROR");

     //   alert.setView(edittext);
/*
        alert.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value

            }
        });
*/
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                Toast.makeText(serveract, "Cancelled",
                        Toast.LENGTH_LONG).show();
            }
        });

        alert.show();
    }

}
