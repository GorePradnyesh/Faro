package com.zik.faro.frontend;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;


public class CreateNewPoll extends Activity {

    private ArrayList<String> pollOptionsArray = new ArrayList<>();
    int pollOtionsNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_poll);

        EditText pollDescription = (EditText)findViewById(R.id.pollDescription);
        final CheckBox isMultiChoice = (CheckBox)findViewById(R.id.multiChoiceFlag);
        final EditText pollOption = (EditText)findViewById(R.id.pollOptionEditText);
        final ImageButton addNewOptionButton = (ImageButton)findViewById(R.id.add_new_option);

        ListView pollOptionsList = (ListView)findViewById(R.id.pollOptionsList);

        final Button createNewPollOK = (Button) findViewById(R.id.createNewPollOK);
        final Button createNewPollCancel = (Button) findViewById(R.id.createNewPollCancel);

        addNewOptionButton.setImageResource(R.drawable.plus);

        final ArrayAdapter<String> pollOptionsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pollOptionsArray);
        pollOptionsList.setAdapter(pollOptionsAdapter);

        //Enable the addNewOptionButton only after Users enters an Option
        pollOption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addNewOptionButton.setEnabled(!(pollOption.getText().toString().trim().isEmpty()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        addNewOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pollOptionsArray.add(pollOption.getText().toString());
                pollOptionsAdapter.notifyDataSetChanged();
                pollOption.setText("");
                pollOtionsNum++;
                //Enable createNewPollOK button only if atleast 2 poll are added.
                if (pollOtionsNum == 2) createNewPollOK.setEnabled(true);
            }
        });

        createNewPollOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Poll poll = Poll("eventID", "eventCreator", isMultiChoice.isChecked(), );
            }
        });

        createNewPollCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
