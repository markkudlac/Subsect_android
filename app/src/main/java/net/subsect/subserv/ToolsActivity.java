package net.subsect.subserv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.subsect.subserv.Const.*;

/**
 * Created by markkudlac on 2015-04-01.
 */
public class ToolsActivity extends Activity {

    private Spinner sitespinner;
    private Button butExport;
    private Button butInstall;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tools);
        addItemsOnSiteSpinner();
        addListenerOnButtons();
    }

    // add items into spinner dynamically
    public void addItemsOnSiteSpinner() {

        int count = 0;
        File[] dirlist;

        sitespinner = (Spinner) findViewById(R.id.sitespinner);
        List<String> list = new ArrayList<String>();

        dirlist = new File(getFilesDir() + "/" + SYS_DIR).listFiles();
        for (int i=0; i < dirlist.length; i++)
        {
            count++;
            list.add(SYS_DIR+"/" + dirlist[i].getName());
            System.out.println("FileName:" + dirlist[i].getName());
        }

        dirlist = new File(getFilesDir() + "/" + USR_DIR).listFiles();
        for (int i=0; i < dirlist.length; i++)
        {
            count++;
            list.add(USR_DIR+"/" + dirlist[i].getName());
            System.out.println("FileName:" + dirlist[i].getName());
        }

        if (count == 0) list.add("No Sites Available");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sitespinner.setAdapter(dataAdapter);
    }

    /*
    public void addListenerOnSpinnerItemSelection() {
        spinner1 = (Spinner) findViewById(R.id.spinner1);
     //   spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }
    */

    // get the selected dropdown list value
    public void addListenerOnButtons() {

        butExport = (Button) findViewById(R.id.butExport);
        butInstall = (Button) findViewById(R.id.butInstall);

        butExport.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(getBaseContext(),
                        "Listener : " +
                                "\nSpinner : " + String.valueOf(sitespinner.getSelectedItem()),
                        Toast.LENGTH_LONG).show();
            }

        });


        butInstall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popInstallDialogue();
                /*
                Toast.makeText(getBaseContext(),
                        "Install pressed",
                        Toast.LENGTH_LONG).show();
                        */
            }

        });
    }


    private void popInstallDialogue(){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage("Tap INSTALL to install site from rootpack.targz");
        alert.setTitle("Install Site");

       // alert.setView(edittext);

        alert.setPositiveButton("INSTALL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                Toast.makeText(getBaseContext(),
                        "Installing Site",
                        Toast.LENGTH_SHORT).show();
                String rtn = Util.installApp(getBaseContext(), SYS_DIR, INSTALL_FILE, "",
                        10000, "");  //title will default to package name

                System.out.println("Rtn install "+rtn);
                if (rtn.indexOf("true") > 0){
                    Toast.makeText(getBaseContext(), "Install Succesful",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "Install Failed",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                Toast.makeText(getBaseContext(), "Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        });

        alert.show();
    }

}
