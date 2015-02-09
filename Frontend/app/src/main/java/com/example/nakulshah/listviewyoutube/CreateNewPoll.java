package com.example.nakulshah.listviewyoutube;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;


public class CreateNewPoll extends Activity {

    private ArrayList<String> pollOptionsArray = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_poll);

        EditText pollQuestion = (EditText)findViewById(R.id.pollQuestion);
        CheckBox ismultiChoice = (CheckBox)findViewById(R.id.multiChoicePoll);
        final EditText pollOption = (EditText)findViewById(R.id.pollOptionEditText);
        ImageButton addNewOption = (ImageButton)findViewById(R.id.add_new_option);
        ListView pollOptionsList = (ListView)findViewById(R.id.pollOptionsList);

        addNewOption.setImageResource(R.drawable.plus);

        final ArrayAdapter<String> pollOptionsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pollOptionsArray);
        pollOptionsList.setAdapter(pollOptionsAdapter);

        addNewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pollOptionsArray.add(pollOption.getText().toString());
                //pollOptionsAdapter.add(pollOption.getText().toString());
                pollOptionsAdapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_new_poll, menu);
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
}
