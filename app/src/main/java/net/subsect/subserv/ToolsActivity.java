package net.subsect.subserv;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by markkudlac on 2015-04-01.
 */
public class ToolsActivity extends Activity {

    private Spinner sitespinner;
    private Button butExport;
    private Button butImport;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tools);
        addItemsOnSiteSpinner();
        addListenerOnButtons();
    }

    // add items into spinner dynamically
    public void addItemsOnSiteSpinner() {

        sitespinner = (Spinner) findViewById(R.id.sitespinner);
        List<String> list = new ArrayList<String>();

        list.add("list 1");
        list.add("list 2");
        list.add("list 3");

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

        butExport.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(getBaseContext(),
                        "Listener : " +
                                "\nSpinner : " + String.valueOf(sitespinner.getSelectedItem()),
                        Toast.LENGTH_LONG).show();
            }

        });
    }
}
