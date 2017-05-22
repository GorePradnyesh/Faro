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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.okhttp.Request;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.notification.NotificationPayloadHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClosedPollLandingPage extends Activity implements NotificationPayloadHandler {

    private static String eventID = null;
    private static String pollID = null;
    private static Poll clonePoll;
    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private  static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;
    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();

    List<PollOption> pollOptionsList;

    private static int popupWidth;
    private static int popupHeight;
    private RelativeLayout popUpRelativeLayout;
    private static final Integer POLL_OPTION_ROW_HEIGHT = 100;

    Intent PickPollWinnerIntent = null;
    Intent OpenPollLandingPageIntent = null;
    Intent PollListPageIntent = null;

    private LinearLayout linlaHeaderProgress = null;
    private RelativeLayout ClosedPollLandingPageRelativeLayout = null;

    private final Context mContext = this;

    private Map<String, Object> map = null;

    private static String TAG = "ClsdPollLndngPageIntent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closed_poll_landing_page);

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        ClosedPollLandingPageRelativeLayout = (RelativeLayout) findViewById(R.id.openPollLandingPageRelativeLayout);
        ClosedPollLandingPageRelativeLayout.setVisibility(View.GONE);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        checkAndHandleNotification(extras);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ClosedPollLandingPage.this, android.R.layout.simple_spinner_item, voters);
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

    private void setupPageDetails(){
        final TextView pollDesc = (TextView)findViewById(R.id.pollDescription);
        final TextView winnerPollOptionTV = (TextView) findViewById(R.id.winnerPollOptionTextView);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.closedPollLandingPage);

        final Button changeWinner = (Button)findViewById(R.id.changeWinner);
        final Button reOpenPoll = (Button)findViewById(R.id.reOpenPoll);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        popupWidth = (int) (dm.widthPixels * 0.8);
        popupHeight = (int) (dm.heightPixels * 0.8);

        OpenPollLandingPageIntent = new Intent(ClosedPollLandingPage.this, OpenPollLandingPage.class);
        PickPollWinnerIntent = new Intent(ClosedPollLandingPage.this, PickPollWinnerPage.class);
        PollListPageIntent = new Intent(ClosedPollLandingPage.this, PollListPage.class);



        clonePoll = pollListHandler.getPollCloneFromMap(pollID);
        pollOptionsList = clonePoll.getPollOptions();
        pollDesc.setText(clonePoll.getDescription());

        LinearLayout pollOptionsTextList = (LinearLayout) findViewById(R.id.pollOptionsTextList);
        LinearLayout voterButtonLinearLayout = (LinearLayout) findViewById(R.id.voterButtonLinearLayout);
        for (Integer i = 0; i < pollOptionsList.size(); i++) {
            //Create TextView to display Poll Option
            final TextView textView = new TextView(this);
            PollOption pollOption = pollOptionsList.get(i);

            textView.setText(pollOption.getOption());
            textView.setGravity(Gravity.CENTER_VERTICAL);

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

            //Setting the TextView to display the winning Option
            if ((pollOption.getId().equals(clonePoll.getWinnerId()))){
                winnerPollOptionTV.setText("Winner is: " + pollOption.getOption());
                textView.setBackgroundColor(Color.BLUE);
                voterCountButton.setBackgroundColor(Color.BLUE);
            }

            //Insert TextView into pollOptionsTextList
            RelativeLayout.LayoutParams textViewparams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, POLL_OPTION_ROW_HEIGHT);
            pollOptionsTextList.addView(textView, textViewparams);

            //Insert voter count button into voterButtonLinearLayout
            RelativeLayout.LayoutParams voterCountButtonParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, POLL_OPTION_ROW_HEIGHT);
            voterButtonLinearLayout.addView(voterCountButton, voterCountButtonParams);


        }

        changeWinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickPollWinnerIntent.putExtra("eventID", eventID);
                PickPollWinnerIntent.putExtra("pollID", pollID);
                PickPollWinnerIntent.putExtra("calledFrom", "ClosedPollLandingPageIntent");
                startActivity(PickPollWinnerIntent);
                finish();
            }
        });

        reOpenPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO update server of reopening the Poll

                Poll pollVersionObj = new Poll();
                pollVersionObj.setEventId(clonePoll.getEventId());
                pollVersionObj.setId(clonePoll.getId());
                pollVersionObj.setVersion(clonePoll.getVersion());
                pollVersionObj.setWinnerId(null);
                pollVersionObj.setStatus(ObjectStatus.OPEN);

                map = new HashMap<String, Object>();
                map.put("poll", pollVersionObj);

                updatePollToServerAndReopenIt();
            }
        });
    }

    private void updatePollToServerAndReopenIt(){
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
                            OpenPollLandingPageIntent.putExtra("eventID", eventID);
                            OpenPollLandingPageIntent.putExtra("pollID", pollID);
                            startActivity(OpenPollLandingPageIntent);
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

    private void getUpdatedPollFromServer(){
        serviceHandler.getPollHandler().getPoll(new BaseFaroRequestCallback<Poll>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get the updated Poll Object");
            }

            @Override
            public void onResponse(final Poll receivedPoll, HttpError error) {
                if (error == null ) {
                    //Since update to server successful
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            pollListHandler.addPollToListAndMap(receivedPoll);
                            if(receivedPoll.getStatus().equals(ObjectStatus.CLOSED)) {
                                setupPageDetails();
                            }else{
                                /*
                                 * We can hit is case in case the user receives the notification when
                                  * the poll was still open but clicks on it after the poll was closed.
                                 */
                                OpenPollLandingPageIntent = new Intent(ClosedPollLandingPage.this, OpenPollLandingPage.class);
                                OpenPollLandingPageIntent.putExtra("eventID", eventID);
                                OpenPollLandingPageIntent.putExtra("pollID", pollID);
                                startActivity(OpenPollLandingPageIntent);
                                finish();
                            }
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }
                else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }

            }
        }, eventID, pollID);
    }

    @Override
    public void checkAndHandleNotification(Bundle extras) {
        if (extras == null) return; //TODO: How to handle such conditions

        String bundleType = extras.getString("bundleType");
        eventID = extras.getString("eventID");
        pollID = extras.getString("pollID");

        Log.d(TAG, "******eventID is " + eventID);
        Log.d(TAG, "******pollID is " + pollID);

        if (bundleType == null) {
            setupPageDetails();
            return;
        }

        //Else the bundleType is "notification"

        //API call to get updated poll
        getUpdatedPollFromServer();
    }
}
