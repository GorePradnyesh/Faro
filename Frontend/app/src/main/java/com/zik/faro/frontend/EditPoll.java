package com.zik.faro.frontend;

import android.app.Activity;
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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class EditPoll extends Activity {

    static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static String eventID = null;
    private String pollID;
    private static Poll poll;
    private static Event event;
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
        addNewOptionButton.setEnabled(false);

        ListView editPollOptionsListView = (ListView)findViewById(R.id.editPollOptionsList);
        final List <PollOption> newPollOptionList = new LinkedList<>();

        final Button editPollOK = (Button) findViewById(R.id.editPollOK);
        editPollOK.setEnabled(true);
        final Button editPollCancel = (Button) findViewById(R.id.editPollCancel);

        PollLandingPage = new Intent(EditPoll.this, OpenPollLandingPage.class);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            pollID = extras.getString("pollID");
            poll = pollListHandler.getPollFromMap(pollID);
            event = eventListHandler.getEventFromMap(eventID);
        }


        final PollOptionsAdapter editPollOptionsAdapter = new PollOptionsAdapter(this, R.layout.poll_create_new_page_row_style);
        editPollOptionsListView.setAdapter(editPollOptionsAdapter);
        for (Integer i = 0; i < poll.getPollOptions().size(); i++){
            PollOption pollOption = poll.getPollOptions().get(i);
            editPollOptionsAdapter.insert(pollOption, 0);
        }
        editPollOptionsAdapter.notifyDataSetChanged();
        editPollOptionsListView.setAdapter(editPollOptionsAdapter);
        /*final PollOptionsAdapter newPollOptionsAdapter = new PollOptionsAdapter(this, R.layout.poll_create_new_page_row_style);
        newPollOptionsList.setAdapter(newPollOptionsAdapter);

        final PollOptionsAdapter oldPollOptionsAdapter = new PollOptionsAdapter(this, R.layout.poll_create_new_page_row_style);
        for (Integer i = 0; i < poll.getPollOptions().size(); i++){
            PollOption pollOption = poll.getPollOptions().get(i);
            oldPollOptionsAdapter.insert(pollOption, 0);
            oldPollOptionsAdapter.notifyDataSetChanged();
            finalPollOptionList.add(pollOption);
        }
        oldPollOptionsList.setAdapter(oldPollOptionsAdapter);*/

        pollDescription.setText(poll.getDescription());
        isMultiChoice.setChecked(poll.isMultiChoice());

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

                editPollOptionsAdapter.insert(pollOption, 0);
                editPollOptionsAdapter.notifyDataSetChanged();
                newPollOptionList.add(pollOption);
                optionText.setText("");
            }
        });

        editPollOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!newPollOptionList.isEmpty()) {
                    //TODO Make API call to update the pollOptions
                    List <PollOption> tempList = editPollOptionsAdapter.getList();
                    for (Integer i = 0; i < tempList.size(); i++){
                        PollOption pollOption = tempList.get(i);
                        pollOption.setId(UUID.randomUUID().toString());
                    }
                    poll.setPollOptions(editPollOptionsAdapter.getList());
                }

                PollLandingPage.putExtra("eventID", event.getEventId());
                PollLandingPage.putExtra("pollID", poll.getId());
                startActivity(PollLandingPage);
                finish();
            }
        });

        editPollCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PollLandingPage.putExtra("eventID", event.getEventId());
                PollLandingPage.putExtra("pollID", poll.getId());
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
        PollLandingPage.putExtra("eventID", event.getEventId());
        PollLandingPage.putExtra("pollID", poll.getId());
        startActivity(PollLandingPage);
        finish();
        super.onBackPressed();
    }
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
}

