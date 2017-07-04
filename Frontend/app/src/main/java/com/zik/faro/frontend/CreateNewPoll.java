package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;

import java.io.IOException;


public class CreateNewPoll extends Activity {

    private PollListHandler pollListHandler = PollListHandler.getInstance();
    private String eventId = null;

    private FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();

    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private static String TAG = "CreateNewPoll";

    private EditText pollDescription = null;
    private CheckBox isMultiChoice = null;
    private EditText optionText = null;
    private ImageButton addNewOptionButton = null;
    private ListView pollOptionsList = null;
    private Button createNewPollOK = null;
    private PollOptionsAdapter pollOptionsAdapter = null;
    private Intent PollLandingPageIntent = null;

    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_poll);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        if (extras == null) return;//TODO: How to handle this condition

        eventId = extras.getString(FaroIntentConstants.EVENT_ID);

        pollDescription = (EditText)findViewById(R.id.pollDescription);
        isMultiChoice = (CheckBox)findViewById(R.id.multiChoiceFlag);
        optionText = (EditText)findViewById(R.id.pollOptionEditText);

        addNewOptionButton = (ImageButton)findViewById(R.id.add_new_option);
        addNewOptionButton.setImageResource(R.drawable.plus);
        addNewOptionButton.setEnabled(false);

        pollOptionsList = (ListView)findViewById(R.id.pollOptionsList);

        createNewPollOK = (Button) findViewById(R.id.createNewPollOK);

        PollLandingPageIntent = new Intent(CreateNewPoll.this, PollLandingPage.class);

        pollOptionsAdapter = new PollOptionsAdapter(this, R.layout.poll_option_can_edit_row_style);
        pollOptionsList.setAdapter(pollOptionsAdapter);


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
                    createNewPollOK.setEnabled(false);
                    return;
                }
                Poll poll = new Poll(eventId, myUserId, isMultiChoice.isChecked(),
                        pollOptionsAdapter.list, myUserId, pollDescription.getText().toString(),
                        ObjectStatus.OPEN);

                sendNewPollToServer(poll);
            }
        });
    }


    private void sendNewPollToServer (Poll poll) {
        serviceHandler.getPollHandler().createPoll(new BaseFaroRequestCallback<Poll>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to send poll create request");
            }

            @Override
            public void onResponse(final Poll receivedPoll, HttpError error) {
                if (error == null ) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Poll Create Response received Successfully");
                            pollListHandler.addPollToListAndMap(eventId, receivedPoll, mContext);
                            FaroIntentInfoBuilder.pollIntent(PollLandingPageIntent,
                                    eventId, receivedPoll.getId());
                            startActivity(PollLandingPageIntent);
                            finish();
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventId, poll);
    }
}
