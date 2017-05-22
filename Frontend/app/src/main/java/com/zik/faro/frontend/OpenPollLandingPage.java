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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static android.widget.Toast.LENGTH_LONG;

import com.squareup.okhttp.Request;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.notification.NotificationPayloadHandler;

public class OpenPollLandingPage extends Activity implements NotificationPayloadHandler {

    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static Poll clonePoll;
    private static String eventID = null;
    private static String pollID = null;


    static FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;;
    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();

    //Maps to manage Multichoice polls
    private Map<Integer, PollOption> selectedPollMap = new ConcurrentHashMap<>();
    private Map<Integer, PollOption> notSelectedPollMap = new ConcurrentHashMap<>();
    private Map<Integer, PollOption> originalSelectedPollMap = new ConcurrentHashMap<>();

    //Below Integers to manage Single choice polls
    private static final Integer INVALID_SELECTED_INDEX = -1;
    private Integer originalSelectedSingleChoiceOption = INVALID_SELECTED_INDEX;
    private Integer selectedSingleChoiceOption = INVALID_SELECTED_INDEX;

    private boolean isMultiChoice = false;

    private static int popupWidth;
    private static int popupHeight;
    private RelativeLayout popUpRelativeLayout;
    private static final Integer POLL_OPTION_ROW_HEIGHT = 150;

    private List<PollOption> pollOptionsList;

    Intent PollListPageIntent = null;
    Intent PickPollWinnerIntent = null;
    Intent EditPollPageIntent = null;
    Intent ClosedPollLandingPageIntent = null;
    private Intent OpenPollLandingPageReloadIntent = null;

    private TextView pollDesc = null;
    private ImageButton editButton = null;
    private Button votePoll = null;
    private Button closePoll = null;

    private final Context mContext = this;

    private Map<String, Object> map = null;

    private LinearLayout linlaHeaderProgress = null;
    private RelativeLayout OpenPollLandingPageRelativeLayout = null;


    private static String TAG = "OpnPollLndngPageIntent";

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_poll_landing_page);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        OpenPollLandingPageRelativeLayout = (RelativeLayout) findViewById(R.id.openPollLandingPageRelativeLayout);
        OpenPollLandingPageRelativeLayout.setVisibility(View.GONE);

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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(OpenPollLandingPage.this, android.R.layout.simple_spinner_item, voters);
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

        linlaHeaderProgress.setVisibility(View.GONE);
        OpenPollLandingPageRelativeLayout.setVisibility(View.VISIBLE);

        pollDesc = (TextView)findViewById(R.id.pollDescription);

        editButton = (ImageButton)findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        votePoll = (Button)findViewById(R.id.votePoll);
        closePoll = (Button)findViewById(R.id.closePoll);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.pollLandingPage);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        popupWidth = (int) (dm.widthPixels * 0.8);
        popupHeight = (int) (dm.heightPixels * 0.8);

        PollListPageIntent = new Intent(OpenPollLandingPage.this, PollListPage.class);
        PickPollWinnerIntent = new Intent(OpenPollLandingPage.this, PickPollWinnerPage.class);
        EditPollPageIntent = new Intent(OpenPollLandingPage.this, EditPoll.class);
        OpenPollLandingPageReloadIntent = new Intent(OpenPollLandingPage.this, OpenPollLandingPage.class);

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
                    Toast.makeText(OpenPollLandingPage.this, "Select atleast one option to Vote", LENGTH_LONG).show();
                    return;
                }else if(originalSelectedPollMap.equals(selectedPollMap)){
                    Toast.makeText(OpenPollLandingPage.this, "No change to selected options", LENGTH_LONG).show();
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
                    Toast.makeText(OpenPollLandingPage.this, "Select an option to Vote", LENGTH_LONG).show();
                    return;
                } else if (originalSelectedSingleChoiceOption.equals(selectedSingleChoiceOption)) {
                    Toast.makeText(OpenPollLandingPage.this, "No change to selected options", LENGTH_LONG).show();
                    return;
                } else {
                    Toast.makeText(OpenPollLandingPage.this, "Selected option is " + pollOptionsList.get(selectedSingleChoiceOption).getOption(), LENGTH_LONG).show();

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
                PickPollWinnerIntent.putExtra("calledFrom", "OpenPollLandingPageIntent");
                startActivity(PickPollWinnerIntent);
                finish();
            }
        });

            editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPollPageIntent.putExtra("eventID", eventID);
                EditPollPageIntent.putExtra("pollID", pollID);
                startActivity(EditPollPageIntent);
                finish();
            }
        });
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
            clonePoll = pollListHandler.getPollCloneFromMap(pollID);
            if (clonePoll == null){
                Toast.makeText(OpenPollLandingPage.this, "No Poll found", LENGTH_LONG).show();
                finish();
            }
            setupPageDetails();
            return;
        }

        //Else the bundleType is "notification"

        //API call to get updated poll
        getUpdatedPollFromServer();


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
                            pollListHandler.addPollToListAndMap(poll);
                            //Reload current activity
                            OpenPollLandingPageReloadIntent.putExtra("eventID", eventID);
                            OpenPollLandingPageReloadIntent.putExtra("pollID", pollID);
                            finish();
                            startActivity(OpenPollLandingPageReloadIntent);
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
                            clonePoll = receivedPoll;
                            if(receivedPoll.getStatus().equals(ObjectStatus.OPEN)) {
                                setupPageDetails();
                            }else{
                                /*
                                 * We can hit is case in case the user receives the notification when
                                  * the poll was still open but clicks on it after the poll was closed.
                                 */
                                ClosedPollLandingPageIntent = new Intent(OpenPollLandingPage.this, ClosedPollLandingPage.class);
                                ClosedPollLandingPageIntent.putExtra("eventID", eventID);
                                ClosedPollLandingPageIntent.putExtra("pollID", pollID);
                                startActivity(ClosedPollLandingPageIntent);
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
}
