package com.example.mobile.sdaclient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinner = (Spinner) findViewById(R.id.spinner);
    EditText text1 = (EditText) findViewById(R.id.editText) ;
    EditText text2 = (EditText) findViewById(R.id.editText2) ;
    EditText text3 = (EditText) findViewById(R.id.editText3) ;
    EditText text4 = (EditText) findViewById(R.id.editText4) ;
    Button go=(Button)findViewById(R.id.button);
    ServerConnection server;
    int endpoint=0;
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.endpoints_array, android.R.layout.simple_spinner_item);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server= new ServerConnection();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // in here use the end point to call the correct function in the server class that will talk to the server
                switch (endpoint) {
                    case 0:
                        showIDOnly();
                        break;
                    case 1:  showIDOnly();
                        break;
                    case 2: showAll();
                        break;
                    case 3:  showTagsOnly();
                        break;
                    case 4:  showNone();
                        break;
                    case 5: showTagsOnly();
                        break;
                    case 6:  showNone();
                        break;
                    case 7:  showNone();
                        break;
                    case 8:  showNone();
                        break;
                    case 9:  showNone();
                        break;
                    case 10: showTagsOnly();
                        break;
                    default: showIDOnly();
                        break;
                }
            }
        });
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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
/*<item>View Document</item>
        <item>Delete Document</item>
        <item>New Document</item>
        <item>Delete By Tags</item>
        <item>List All Documents</item>
        <item>Search</item>
        <item>Reset</item>
        <item>List</item>
        <item>PageRank</item>
        <item>Boost</item>
        <item>Update</item>*/

        endpoint=pos;
        switch (pos) {
            case 0:
                    showIDOnly();
                break;
            case 1:  showIDOnly();
                break;
            case 2: showAll();
                break;
            case 3:  showTagsOnly();
                break;
            case 4:  showNone();
                break;
            case 5: showTagsOnly();
                break;
            case 6:  showNone();
                break;
            case 7:  showNone();
                break;
            case 8:  showNone();
                break;
            case 9:  showNone();
                break;
            case 10: showTagsOnly();
                break;
            default: showIDOnly();
                break;
        }
    }
    public void showIDOnly(){
        text1.setVisibility(View.VISIBLE);
        text2.setVisibility(View.GONE);
        text3.setVisibility(View.GONE);
        text4.setVisibility(View.GONE);
    }
    public void showTagsOnly(){
        text1.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);
        text3.setVisibility(View.GONE);
        text4.setVisibility(View.VISIBLE);
    }
    public void showAll(){
        text1.setVisibility(View.VISIBLE);
        text2.setVisibility(View.VISIBLE);
        text3.setVisibility(View.VISIBLE);
        text4.setVisibility(View.VISIBLE);
    }
    public void showNone(){
        text1.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);
        text3.setVisibility(View.GONE);
        text4.setVisibility(View.GONE);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

}
