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
    private static final Integer POLL_OPTION_ROW_HEIGHT = 150;

    private Intent PollLandingPageIntent = null;

    private static String TAG = "PollPickWinnerPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_poll_winner);

        serviceHandler = eventListHandler.serviceHandler;

        final TextView pollDesc = (TextView)findViewById(R.id.pollDescription);
        final Button selectWinner = (Button) findViewById(R.id.selectWinner);

        PollLandingPageIntent = new Intent(PickPollWinnerPage.this, PollLandingPage.class);

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

            final LinearLayout pollOptionsListLinearLayout  = (LinearLayout) findViewById(R.id.pollOptionsListLinearLayout);
            final LinearLayout voterButtonLinearLayout = (LinearLayout) findViewById(R.id.votersListLinearLayout);

            popUpRelativeLayout = (RelativeLayout) findViewById(R.id.pickPollWinner);


            for (Integer i = 0; i < pollOptionsList.size(); i++) {
                PollOption pollOption = pollOptionsList.get(i);

                //Create textview for the poll Option
                TextView pollOptionDescription = new TextView(this);
                pollOptionDescription.setText(pollOption.getOption());
                pollOptionDescription.setId(i);
                pollOptionDescription.setGravity(Gravity.CENTER_VERTICAL);
                pollOptionDescription.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView selectedButton = (TextView) v;
                        Integer id = selectedButton.getId();
                        pollOptionsListLinearLayout.getChildAt(id).setBackgroundColor(Color.BLUE);
                        voterButtonLinearLayout.getChildAt(id).setBackgroundColor(Color.BLUE);
                        if (!pollOptionWinnerPosition.equals(INVALID_SELECTED_INDEX) && !pollOptionWinnerPosition.equals(id)) {
                            pollOptionsListLinearLayout.getChildAt(pollOptionWinnerPosition).setBackgroundColor(Color.TRANSPARENT);
                            voterButtonLinearLayout.getChildAt(pollOptionWinnerPosition).setBackgroundColor(Color.TRANSPARENT);
                        }
                        pollOptionWinnerPosition = id;
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

                if((pollOption.getId().equals(clonePoll.getWinnerId()))) {
                    pollOptionDescription.setBackgroundColor(Color.BLUE);
                    voterCountButton.setBackgroundColor(Color.BLUE);
                    pollOptionWinnerPosition = i;
                    previousPollOptionWinnerPosition = i;
                }

                //Insert poll Option and the voter count button into the LinearLayouts
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, POLL_OPTION_ROW_HEIGHT);
                pollOptionsListLinearLayout.addView(pollOptionDescription, layoutParams);
                voterButtonLinearLayout.addView(voterCountButton, layoutParams);
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
                                            PollLandingPageIntent.putExtra("eventID", eventID);
                                            PollLandingPageIntent.putExtra("pollID", pollID);
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
                        }, eventID, pollID, map);
                    }
                }
            });
        }
    }

    private void voterListPopUP(View v) {
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
        PollLandingPageIntent.putExtra("eventID", eventID);
        PollLandingPageIntent.putExtra("pollID", pollID);
        startActivity(PollLandingPageIntent);
    finish();
    }
}
