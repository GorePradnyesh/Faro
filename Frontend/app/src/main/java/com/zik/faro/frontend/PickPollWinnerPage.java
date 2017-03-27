package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;

public class PickPollWinnerPage extends Activity {

    private static String eventID = null;
    private static String pollID = null;
    private static String calledFrom = null;
    private static Poll clonePoll;

    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static FaroServiceHandler serviceHandler;
    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();


    private List<PollOption> pollOptionsList;

    private static final Integer INVALID_SELECTED_INDEX = -1;
    private Integer pollOptionWinnerPosition = INVALID_SELECTED_INDEX;
    private Integer previousPollOptionWinnerPosition = INVALID_SELECTED_INDEX;

    private static int popupWidth;
    private static int popupHeight;
    private RelativeLayout popUpRelativeLayout;
    private static final Integer POLL_OPTION_ROW_HEIGHT = 100;

    Intent OpenPollLandingPage = null;
    Intent ClosedPollLandingPage = null;

    private static String TAG = "OpenPollLandingPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_poll_winner);

        serviceHandler = eventListHandler.serviceHandler;

        final TextView pollDesc = (TextView)findViewById(R.id.pollDescription);
        final Button selectWinner = (Button) findViewById(R.id.selectWinner);

        OpenPollLandingPage = new Intent(PickPollWinnerPage.this, OpenPollLandingPage.class);
        ClosedPollLandingPage = new Intent(PickPollWinnerPage.this, ClosedPollLandingPage.class);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        popupWidth = (int) (dm.widthPixels * 0.8);
        popupHeight = (int) (dm.heightPixels * 0.8);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        final Context mContext = this;

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            pollID = extras.getString("pollID");
            calledFrom = extras.getString("calledFrom");
            clonePoll = pollListHandler.getPollCloneFromMap(pollID);

            pollOptionsList = clonePoll.getPollOptions();
            pollDesc.setText(clonePoll.getDescription());

            LinearLayout voterButtonLinearLayout = (LinearLayout) findViewById(R.id.voterButtonLinearLayout);
            RadioGroup pollOptionsRadioGroup = (RadioGroup) findViewById(R.id.pollOptionsRadioGroup);

            popUpRelativeLayout = (RelativeLayout) findViewById(R.id.pickPollWinner);


            for (Integer i = 0; i < pollOptionsList.size(); i++) {
                //Create RadioButton
                final RadioButton button = new RadioButton(this);
                PollOption pollOption = pollOptionsList.get(i);
                button.setText(pollOption.getOption());
                button.setId(i);
                if((pollOption.getId().equals(clonePoll.getWinnerId()))){
                    button.setChecked(true);
                    pollOptionWinnerPosition = i;
                    previousPollOptionWinnerPosition = i;
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RadioButton clickedRadioButton = (RadioButton)v;
                        Integer id = clickedRadioButton.getId();
                        if(clickedRadioButton.isChecked()){
                            pollOptionWinnerPosition = id;
                        }
                    }
                });

                //Create voter count button
                Button voterCountButton = new Button(this);
                int votersCount = pollOption.getVoters().size();
                voterCountButton.setText("(" + Integer.toString(votersCount) + ")");
                voterCountButton.setBackgroundColor(Color.TRANSPARENT);
                voterCountButton.setId(i);
                voterCountButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        voterListPopUP(v);
                    }
                });

                //Insert RadioButton into Radio Group
                RelativeLayout.LayoutParams radioButtonParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, POLL_OPTION_ROW_HEIGHT);
                pollOptionsRadioGroup.addView(button, radioButtonParams);

                //Insert voter count button into voterButtonLinearLayout
                RelativeLayout.LayoutParams voterCountButtonParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, POLL_OPTION_ROW_HEIGHT);
                voterButtonLinearLayout.addView(voterCountButton, voterCountButtonParams);
            }

            selectWinner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (previousPollOptionWinnerPosition.equals(INVALID_SELECTED_INDEX) &&
                            pollOptionWinnerPosition.equals(INVALID_SELECTED_INDEX)){
                        Toast.makeText(PickPollWinnerPage.this, "Pick a winner", LENGTH_LONG).show();
                    }else if (pollOptionWinnerPosition.equals(previousPollOptionWinnerPosition)){
                        Toast.makeText(PickPollWinnerPage.this, "Winner not changed", LENGTH_LONG).show();
                    }else{
                        PollOption winnerPollOption = pollOptionsList.get(pollOptionWinnerPosition);

                        Poll pollVersionObj = new Poll();
                        pollVersionObj.setEventId(clonePoll.getEventId());
                        pollVersionObj.setId(clonePoll.getId());
                        pollVersionObj.setVersion(clonePoll.getVersion());
                        pollVersionObj.setWinnerId(winnerPollOption.getId());
                        pollVersionObj.setStatus(ObjectStatus.CLOSED);

                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("poll", pollVersionObj);

                        serviceHandler.getPollHandler().updatePoll(new BaseFaroRequestCallback<Poll>() {
                            @Override
                            public void onFailure(Request request, IOException ex) {
                                Log.e(TAG, "failed to send Poll update request");
                            }

                            @Override
                            public void onResponse(final Poll receivedPoll, HttpError error) {
                                if (error == null ) {
                                    Runnable myRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.i(TAG, "Poll Update Response received Successfully");
                                            pollListHandler.removePollFromListAndMap(clonePoll);
                                            pollListHandler.addPollToListAndMap(receivedPoll);
                                            ClosedPollLandingPage.putExtra("eventID", eventID);
                                            ClosedPollLandingPage.putExtra("pollID", pollID);
                                            startActivity(ClosedPollLandingPage);
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
        }
    }

    public void voterListPopUP(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.poll_voters_list_popup, null);
        final PopupWindow popupWindow = new PopupWindow(container, popupWidth, popupHeight, true);

        ArrayList<String> voters = new ArrayList<>();
        Button selectedButton = (Button) v;
        Integer id = selectedButton.getId();
        PollOption pollOption = pollOptionsList.get(id);

        voters.add("Voters are:");
        ListView voterList = (ListView) container.findViewById(R.id.votersList);
        for (String temp : pollOption.getVoters()) {
            String friendName = userFriendListHandler.getFriendFullNameFromID(temp);

            if (friendName != null) {
                voters.add(friendName);
            }else{
                voters.add(temp);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(PickPollWinnerPage.this, android.R.layout.simple_spinner_item, voters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        voterList.setAdapter(adapter);

        popupWindow.showAtLocation(popUpRelativeLayout, Gravity.CENTER, 0, 0);

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (calledFrom.equals("OpenPollLandingPage")) {
            OpenPollLandingPage.putExtra("eventID", eventID);
            OpenPollLandingPage.putExtra("pollID", pollID);
            startActivity(OpenPollLandingPage);
        }else if (calledFrom.equals("ClosedPollLandingPage")){  //Can happen when need to change winner of a closed Poll
            ClosedPollLandingPage.putExtra("eventID", eventID);
            ClosedPollLandingPage.putExtra("pollID", pollID);
            startActivity(ClosedPollLandingPage);
        }
        finish();
    }
}
