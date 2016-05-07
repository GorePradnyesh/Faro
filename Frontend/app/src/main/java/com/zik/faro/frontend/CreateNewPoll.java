package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.zik.faro.data.Event;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;


public class CreateNewPoll extends Activity {

    static PollListHandler pollListHandler = PollListHandler.getInstance();
    private  static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static String eventID = null;
    private static Event event;
    Intent PollListPage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_poll);

        final EditText pollDescription = (EditText)findViewById(R.id.pollDescription);
        final CheckBox isMultiChoice = (CheckBox)findViewById(R.id.multiChoiceFlag);
        final EditText optionText = (EditText)findViewById(R.id.pollOptionEditText);

        final ImageButton addNewOptionButton = (ImageButton)findViewById(R.id.add_new_option);
        addNewOptionButton.setImageResource(R.drawable.plus);
        addNewOptionButton.setEnabled(false);

        ListView pollOptionsList = (ListView)findViewById(R.id.pollOptionsList);

        final Button createNewPollOK = (Button) findViewById(R.id.createNewPollOK);

        PollListPage = new Intent(CreateNewPoll.this, PollListPage.class);
        final Intent OpenPollLandingPage = new Intent(CreateNewPoll.this, OpenPollLandingPage.class);

        final PollOptionsAdapter pollOptionsAdapter = new PollOptionsAdapter(this, R.layout.poll_create_new_page_row_style);
        pollOptionsList.setAdapter(pollOptionsAdapter);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            event = eventListHandler.getEventFromMap(eventID);
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
                //Enable createNewPollOK button only if atleast 2 poll are added.
                if (pollOptionsAdapter.getCount() == 2) createNewPollOK.setEnabled(true);
            }
        });

        createNewPollOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pollOptionsAdapter.getCount() < 2){
                    //TODO Display Popup asking for atleast 2 options
                    createNewPollOK.setEnabled(false);
                    return;
                }
                Poll poll;
                poll = new Poll(eventID, "pollCreator", isMultiChoice.isChecked(),
                        pollOptionsAdapter.list, "pollCreator", pollDescription.getText().toString(),
                        ObjectStatus.OPEN);

                pollListHandler.addNewPoll(poll);
                OpenPollLandingPage.putExtra("eventID", event.getEventId());
                OpenPollLandingPage.putExtra("pollID", poll.getId());
                startActivity(OpenPollLandingPage);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        PollListPage.putExtra("eventID", event.getEventId());
        startActivity(PollListPage);
        finish();
        super.onBackPressed();
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
