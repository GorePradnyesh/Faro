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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.notification.NotificationPayloadHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static android.widget.Toast.LENGTH_LONG;

public class PollLandingPage extends Activity implements NotificationPayloadHandler {

    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;;

    private static String TAG = "PollLandngPage";

    private LinearLayout linlaHeaderProgress = null;

    private RelativeLayout OpenPollLandingPageRelativeLayout = null;
    private RelativeLayout ClosedPollLandingPageRelativeLayout = null;

    private Poll clonePoll;
    private String eventID = null;
    private String pollID = null;

    private TextView pollDesc = null;
    private ImageButton editButton = null;
    private Button votePoll = null;
    private Button closePoll = null;

    private static int popupWidth;
    private static int popupHeight;
    private RelativeLayout popUpRelativeLayout;
    private static final Integer POLL_OPTION_ROW_HEIGHT = 150;

    Intent PollListPageIntent = null;
    Intent PickPollWinnerIntent = null;
    Intent EditPollPageIntent = null;
    private Intent PollLandingPageReloadIntent = null;

    private List<PollOption> pollOptionsList;

    static FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();

    //Below Integers to manage Single choice polls
    private static final Integer INVALID_SELECTED_INDEX = -1;
    private Integer originalSelectedSingleChoiceOption = INVALID_SELECTED_INDEX;
    private Integer selectedSingleChoiceOption = INVALID_SELECTED_INDEX;

    //Maps to manage Multichoice polls
    private Map<Integer, PollOption> selectedPollMap = new ConcurrentHashMap<>();
    private Map<Integer, PollOption> notSelectedPollMap = new ConcurrentHashMap<>();
    private Map<Integer, PollOption> originalSelectedPollMap = new ConcurrentHashMap<>();

    private boolean isMultiChoice = false;

    private Map<String, Object> map = null;

    private final Context mContext = this;

    private TextView winnerPollOptionTV = null;

    private String isNotification = null;

    private Bundle extras = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_landing_page);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        PollListPageIntent = new Intent(PollLandingPage.this, PollListPage.class);
        PickPollWinnerIntent = new Intent(PollLandingPage.this, PickPollWinnerPage.class);
        EditPollPageIntent = new Intent(PollLandingPage.this, EditPoll.class);
        PollLandingPageReloadIntent = new Intent(PollLandingPage.this, PollLandingPage.class);

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        OpenPollLandingPageRelativeLayout = (RelativeLayout) findViewById(R.id.openPollLandingPageRelativeLayout);
        OpenPollLandingPageRelativeLayout.setVisibility(View.GONE);

        ClosedPollLandingPageRelativeLayout = (RelativeLayout) findViewById(R.id.closedPollLandingPageRelativeLayout);
        ClosedPollLandingPageRelativeLayout.setVisibility(View.GONE);


        extras = getIntent().getExtras();
        checkAndHandleNotification();
    }

    private void setupPageDetails() {

        clonePoll = pollListHandler.getPollCloneFromMap(pollID);
        if (clonePoll == null){
            Toast.makeText(PollLandingPage.this, "No Poll found", LENGTH_LONG).show();
            finish();
        }

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        popupWidth = (int) (dm.widthPixels * 0.8);
        popupHeight = (int) (dm.heightPixels * 0.8);

        linlaHeaderProgress.setVisibility(View.GONE);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.pollLandingPage);

        if (clonePoll.getStatus().equals(ObjectStatus.OPEN)){
            OpenPollLandingPageRelativeLayout.setVisibility(View.VISIBLE);

            pollDesc = (TextView)findViewById(R.id.openPollDescription);

            editButton = (ImageButton)findViewById(R.id.editButton);
            editButton.setImageResource(R.drawable.edit);

            votePoll = (Button)findViewById(R.id.votePoll);
            closePoll = (Button)findViewById(R.id.closePoll);



            pollOptionsList = clonePoll.getPollOptions();
            pollDesc.setText(clonePoll.getDescription());

            ScrollView checkboxScrollView = (ScrollView) findViewById(R.id.checkboxScrollView);

            if (!(clonePoll.getOwner().equals(myUserId))) {
                closePoll.setVisibility(View.GONE);
            }

            //Depending on the type of clonePoll it will be displayed differently

            isMultiChoice = clonePoll.getMultiChoice();

            /*
             * Relative Layout(newPollOptionsRelativeView) is composed of 2 Linear Layouts, as mentioned below, besides each other
             * 1. Linear Layout for list of clonePoll options
             * 2. Linear Layout for list of buttons for voter count (voterButtonLinearLayout)
             */
            final LinearLayout pollOptionsListLinearLayout  = (LinearLayout) findViewById(R.id.pollOptionsListLinearLayout);
            final LinearLayout voterButtonLinearLayout = (LinearLayout) findViewById(R.id.votersListLinearLayout);


            for (Integer i = 0; i < pollOptionsList.size(); i++){
                PollOption pollOption = clonePoll.getPollOptions().get(i);

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
                        PollOption pollOption = pollOptionsList.get(id);
                        if (!isMultiChoice) {
                            pollOptionsListLinearLayout.getChildAt(id).setBackgroundColor(Color.BLUE);
                            voterButtonLinearLayout.getChildAt(id).setBackgroundColor(Color.BLUE);
                            if (!selectedSingleChoiceOption.equals(INVALID_SELECTED_INDEX) && !selectedSingleChoiceOption.equals(id)) {
                                pollOptionsListLinearLayout.getChildAt(selectedSingleChoiceOption).setBackgroundColor(Color.TRANSPARENT);
                                voterButtonLinearLayout.getChildAt(selectedSingleChoiceOption).setBackgroundColor(Color.TRANSPARENT);
                            }
                            selectedSingleChoiceOption = id;
                        }else{
                            if (selectedPollMap.containsKey(id)){
                                selectedPollMap.remove(id);
                                notSelectedPollMap.put(id, pollOption);
                                pollOptionsListLinearLayout.getChildAt(id).setBackgroundColor(Color.TRANSPARENT);
                                voterButtonLinearLayout.getChildAt(id).setBackgroundColor(Color.TRANSPARENT);
                            }else{
                                selectedPollMap.put(id, pollOption);
                                notSelectedPollMap.remove(id);
                                pollOptionsListLinearLayout.getChildAt(id).setBackgroundColor(Color.BLUE);
                                voterButtonLinearLayout.getChildAt(id).setBackgroundColor(Color.BLUE);
                            }
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


                //Insert poll Option and the voter count button into the LinearLayouts
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, POLL_OPTION_ROW_HEIGHT);

                pollOptionsListLinearLayout.addView(pollOptionDescription, layoutParams);
                voterButtonLinearLayout.addView(voterCountButton, layoutParams);

                if(pollOption.getVoters().contains(myUserId)) {
                    pollOptionsListLinearLayout.getChildAt(i).setBackgroundColor(Color.BLUE);
                    voterButtonLinearLayout.getChildAt(i).setBackgroundColor(Color.BLUE);
                    if (!isMultiChoice) {
                        originalSelectedSingleChoiceOption = i;
                        selectedSingleChoiceOption = i;
                    }else{
                        selectedPollMap.put(i, pollOption);
                        originalSelectedPollMap.put(i, pollOption);
                    }
                }else{
                    if (isMultiChoice) {
                        notSelectedPollMap.put(i, pollOption);
                    }
                }
            }

            votePoll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    map = new HashMap<String, Object>();
                    Set<String> voteOption = new HashSet<String>();

                    Poll pollVersionObj = new Poll();
                    pollVersionObj.setEventId(clonePoll.getEventId());
                    pollVersionObj.setId(clonePoll.getId());
                    pollVersionObj.setVersion(clonePoll.getVersion());

                    if(isMultiChoice) {
                        if (originalSelectedPollMap.isEmpty() && selectedPollMap.isEmpty()){
                            Toast.makeText(PollLandingPage.this, "Select atleast one option to Vote", LENGTH_LONG).show();
                            return;
                        }else if(originalSelectedPollMap.equals(selectedPollMap)){
                            Toast.makeText(PollLandingPage.this, "No change to selected options", LENGTH_LONG).show();
                            return;
                        }else if(!originalSelectedPollMap.isEmpty() && selectedPollMap.isEmpty()){ //Can happen when user wants to unvote from all options
                            if (BuildConfig.DEBUG && (selectedPollMap.size() + notSelectedPollMap.size() != pollOptionsList.size())) {
                                throw new AssertionError();
                            }
                            //TODO: Handle this case on backend. Not implemented on backend
                            map.put("poll", pollVersionObj);
                            map.put("voteOption", voteOption);
                        }else{
                            if (BuildConfig.DEBUG && (selectedPollMap.size() + notSelectedPollMap.size() != pollOptionsList.size())) {
                                throw new AssertionError();
                            }


                            for(Map.Entry<Integer, PollOption> entry : selectedPollMap.entrySet()){
                                PollOption mapPollOption = entry.getValue();
                                voteOption.add(mapPollOption.getId());
                            }

                            map.put("poll", pollVersionObj);
                            map.put("voteOption", voteOption);
                        }
                    }else {
                        if (originalSelectedSingleChoiceOption.equals(INVALID_SELECTED_INDEX)
                                && selectedSingleChoiceOption.equals(INVALID_SELECTED_INDEX)) {
                            Toast.makeText(PollLandingPage.this, "Select an option to Vote", LENGTH_LONG).show();
                            return;
                        } else if (originalSelectedSingleChoiceOption.equals(selectedSingleChoiceOption)) {
                            Toast.makeText(PollLandingPage.this, "No change to selected options", LENGTH_LONG).show();
                            return;
                        } else {
                            Toast.makeText(PollLandingPage.this, "Selected option is " + pollOptionsList.get(selectedSingleChoiceOption).getOption(), LENGTH_LONG).show();

                            voteOption.add((pollOptionsList.get(selectedSingleChoiceOption)).getId());

                            map.put("poll", pollVersionObj);
                            map.put("voteOption", voteOption);
                        }
                    }
                    updatePollToServer();
                }
            });

            closePoll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PickPollWinnerIntent.putExtra("eventID", eventID);
                    PickPollWinnerIntent.putExtra("pollID", pollID);
                    PickPollWinnerIntent.putExtra("bundleType", isNotification);
                    startActivity(PickPollWinnerIntent);
                    finish();
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditPollPageIntent.putExtra("eventID", eventID);
                    EditPollPageIntent.putExtra("pollID", pollID);
                    EditPollPageIntent.putExtra("bundleType", isNotification);
                    startActivity(EditPollPageIntent);
                    finish();
                }
            });


        }else if (clonePoll.getStatus().equals(ObjectStatus.CLOSED)){
            ClosedPollLandingPageRelativeLayout.setVisibility(View.VISIBLE);

            pollDesc = (TextView)findViewById(R.id.closedPollDescription);
            winnerPollOptionTV = (TextView) findViewById(R.id.winnerPollOptionTextView);

            final Button changeWinner = (Button)findViewById(R.id.changeWinner);
            final Button reOpenPoll = (Button)findViewById(R.id.reOpenPoll);

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
                    PickPollWinnerIntent.putExtra("bundleType", isNotification);
                    startActivity(PickPollWinnerIntent);
                    finish();
                }
            });

            reOpenPoll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                            pollListHandler.removePollFromListAndMap(eventID, clonePoll, mContext);
                            pollListHandler.addPollToListAndMap(eventID, receivedPoll, mContext);
                            PollLandingPageReloadIntent.putExtra("eventID", eventID);
                            PollLandingPageReloadIntent.putExtra("pollID", pollID);
                            PollLandingPageReloadIntent.putExtra("bundleType", isNotification);
                            startActivity(PollLandingPageReloadIntent);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(PollLandingPage.this, android.R.layout.simple_spinner_item, voters);
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
    public void checkAndHandleNotification() {
        if (extras == null) return; //TODO: How to handle such conditions

        isNotification = extras.getString("bundleType");

        eventID = extras.getString("eventID");
        pollID = extras.getString("pollID");

        Log.d(TAG, "******eventID is " + eventID);
        Log.d(TAG, "******pollID is " + pollID);

        if (isNotification == null) {
            setupPageDetails();
        } else {               //Else the bundleType is "notification"
            //API call to get updated poll
            getUpdatedPollFromServer();
        }
    }

    private void updatePollToServer(){
        serviceHandler.getPollHandler().updatePoll(new BaseFaroRequestCallback<Poll>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to send cast vote request");
            }

            @Override
            public void onResponse(final Poll poll, HttpError error) {
                if (error == null ) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            pollListHandler.addPollToListAndMap(eventID, poll, mContext);
                            //Reload current activity
                            PollLandingPageReloadIntent.putExtra("eventID", eventID);
                            PollLandingPageReloadIntent.putExtra("pollID", pollID);
                            PollLandingPageReloadIntent.putExtra("bundleType", isNotification);
                            startActivity(PollLandingPageReloadIntent);
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
                            pollListHandler.addPollToListAndMap(eventID, receivedPoll, mContext);
                            setupPageDetails();
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
    protected void onResume() {
        if (isNotification == null) {
            // Check if the version is same. It can be different if this page is loaded and a notification
            // is received for this later which updates the global memory but clonedata on this page remains
            // stale.
            // This check is not necessary when opening this page directly through a notification.
            Long versionInGlobalMemory = pollListHandler.getOriginalPollFromMap(pollID).getVersion();
            if (!clonePoll.getVersion().equals(versionInGlobalMemory)) {
                Intent pollLandingPageReloadIntent = new Intent(PollLandingPage.this, PollLandingPage.class);
                pollLandingPageReloadIntent.putExtra("eventID", eventID);
                pollLandingPageReloadIntent.putExtra("pollID", pollID);
                finish();
                startActivity(pollLandingPageReloadIntent);
            }
        }

        super.onResume();
    }
}
