package com.zik.faro.frontend;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.zik.faro.data.Event;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;

public class EditPoll extends ActionBarActivity {

    static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static String eventID = null;
    private String pollID;
    private static Poll P;
    private static Event E;
    Intent PollLandingPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_poll);

        final TextView pollDescription = (TextView) findViewById(R.id.pollDescription);
        final CheckBox isMultiChoice = (CheckBox)findViewById(R.id.multiChoiceFlag);
        final EditText optionText = (EditText)findViewById(R.id.pollOptionEditText);

        final ImageButton addNewOptionButton = (ImageButton)findViewById(R.id.add_new_option);
        addNewOptionButton.setImageResource(R.drawable.plus);

        ListView pollOptionsList = (ListView)findViewById(R.id.pollOptionsList);

        final Button editPollOK = (Button) findViewById(R.id.editPollOK);
        final Button editPollCancel = (Button) findViewById(R.id.editPollCancel);

        PollLandingPage = new Intent(EditPoll.this, OpenPollLandingPage.class);

        final PollOptionsAdapter pollOptionsAdapter = new PollOptionsAdapter(this, R.layout.poll_create_new_page_row_style);
        pollOptionsList.setAdapter(pollOptionsAdapter);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            pollID = extras.getString("pollID");
            P = pollListHandler.getPollFromMap(pollID);
            E = eventListHandler.getEventFromMap(eventID);
        }

        //Enable the addNewOptionButton only after Users enters an Option
        optionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addNewOptionButton.setEnabled(!(optionText.getText().toString().trim().isEmpty()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        addNewOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String optionDescription = optionText.getText().toString();
                PollOption pollOption = new PollOption(optionDescription);

                pollOptionsAdapter.insert(pollOption, 0);
                pollOptionsAdapter.notifyDataSetChanged();
                optionText.setText("");
            }
        });

        editPollOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                P.setPollOptions(pollOptionsAdapter.getList());
                PollLandingPage.putExtra("eventID", E.getEventId());
                PollLandingPage.putExtra("pollID", P.getId());
                startActivity(PollLandingPage);
                finish();
            }
        });

        editPollCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PollLandingPage.putExtra("eventID", E.getEventId());
                PollLandingPage.putExtra("pollID", P.getId());
                startActivity(PollLandingPage);
                finish();
            }
        });
    }

    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //This would probably lead to increase in stack size for Intents since the previous intents are
    //not popped out of the stack. Check how to see the stack size of the intents.
    @Override
    public void onBackPressed() {
        PollLandingPage.putExtra("eventID", E.getEventId());
        PollLandingPage.putExtra("pollID", P.getId());
        startActivity(PollLandingPage);
        finish();
        super.onBackPressed();
    }
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
}

