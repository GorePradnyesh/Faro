package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.widget.Toast.LENGTH_LONG;

public class EditPoll extends Activity {

    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static FaroServiceHandler serviceHandler;

    private static String eventID = null;
    private String pollID;
    private static Poll clonePoll;
    private Intent PollLandingPage;

    private static String TAG = "EditPoll";

    private RelativeLayout popUpRelativeLayout;
    private final List <PollOption> newPollOptionList = new LinkedList<>();
    final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_poll);

        serviceHandler = eventListHandler.serviceHandler;

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        final TextView pollDescription = (TextView) findViewById(R.id.pollDescription);
        final CheckBox isMultiChoice = (CheckBox)findViewById(R.id.multiChoiceFlag);
        final EditText optionText = (EditText)findViewById(R.id.pollOptionEditText);

        final ImageButton addNewOptionButton = (ImageButton)findViewById(R.id.add_new_option);
        addNewOptionButton.setImageResource(R.drawable.plus);
        addNewOptionButton.setEnabled(false);

        ListView editPollOptionsListView = (ListView)findViewById(R.id.editPollOptionsList);

        final Button editPollOK = (Button) findViewById(R.id.editPollOK);
        editPollOK.setEnabled(true);
        final Button deletePoll = (Button)findViewById(R.id.deletePoll);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.editPollPage);

        PollLandingPage = new Intent(EditPoll.this, OpenPollLandingPage.class);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            pollID = extras.getString("pollID");
            clonePoll = pollListHandler.getPollCloneFromMap(pollID);
        }


        final PollOptionsAdapter editPollOptionsAdapter = new PollOptionsAdapter(this, R.layout.poll_option_can_edit_row_style);
        editPollOptionsListView.setAdapter(editPollOptionsAdapter);

        for (Integer i = 0; i < clonePoll.getPollOptions().size(); i++){
            PollOption pollOption = clonePoll.getPollOptions().get(i);
            editPollOptionsAdapter.insert(pollOption, 0);
        }

        editPollOptionsAdapter.notifyDataSetChanged();
        editPollOptionsListView.setAdapter(editPollOptionsAdapter);

        pollDescription.setText(clonePoll.getDescription());
        isMultiChoice.setChecked(clonePoll.getMultiChoice());

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
                newPollOptionList.add(pollOption);
                editPollOptionsAdapter.notifyDataSetChanged();
                optionText.setText("");
            }
        });

        editPollOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!newPollOptionList.isEmpty()) {
                    clonePoll.setPollOptions(newPollOptionList);

                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("poll", clonePoll);

                    serviceHandler.getPollHandler().updatePoll(new BaseFaroRequestCallback<Poll>() {
                        @Override
                        public void onFailure(Request request, IOException ex) {
                            Log.e(TAG, "failed to send Poll update request");
                        }

                        @Override
                        public void onResponse(final Poll poll, HttpError error) {
                            if (error == null ) {
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Poll Update Response received Successfully");
                                        pollListHandler.addPollToListAndMap(poll);
                                        PollLandingPage.putExtra("eventID", eventID);
                                        PollLandingPage.putExtra("pollID", pollID);
                                        startActivity(PollLandingPage);
                                        finish();
                                    }
                                };
                                Handler mainHandler = new Handler(mContext.getMainLooper());
                                mainHandler.post(myRunnable);
                            }else {
                                Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                            }
                        }
                    }, eventID, pollID, map);
                }
            }
        });

        deletePoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmPollDeletePopUP(v);
            }
        });
    }

    private void confirmPollDeletePopUP(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.delete_popup, null);
        final PopupWindow popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        TextView message = (TextView)container.findViewById(R.id.questionTextView);
        message.setText("Are you sure you want to delete the Poll?");
        Button delete = (Button)container.findViewById(R.id.popUpDeleteButton);
        Button cancel = (Button)container.findViewById(R.id.popUpCancelButton);

        popupWindow.showAtLocation(popUpRelativeLayout, Gravity.CENTER, 0, 0);

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return false;
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceHandler.getPollHandler().deletePoll(new BaseFaroRequestCallback<String>() {
                    @Override
                    public void onFailure(Request request, IOException ex) {
                        Log.e(TAG, "failed to delete clonePoll");
                    }

                    @Override
                    public void onResponse(String s, HttpError error) {
                        if (error == null ) {
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    pollListHandler.removePollFromListAndMap(clonePoll);
                                    popupWindow.dismiss();
                                    Toast.makeText(EditPoll.this, clonePoll.getDescription() + "is Deleted", LENGTH_LONG).show();
                                    finish();
                                }
                            };
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(myRunnable);
                        }else {
                            Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        }
                    }
                }, eventID, pollID);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

    }


    @Override
    public void onBackPressed() {
        PollLandingPage.putExtra("eventID", eventID);
        PollLandingPage.putExtra("pollID", pollID);
        startActivity(PollLandingPage);
        finish();
        super.onBackPressed();
    }
}

