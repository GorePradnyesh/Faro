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
import android.view.Menu;
import android.view.MenuItem;
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
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

public class OpenPollLandingPage extends Activity {

    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static Poll clonePoll;
    private static String eventID = null;
    private static String pollID = null;


    static FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();
    private static FaroServiceHandler serviceHandler;
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

    Intent PollListPage = null;
    Intent PickPollWinner = null;
    Intent EditPollPage = null;
    private Intent OpenPollLandingPageReload = null;

    private static String TAG = "OpenPollLandingPage";

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_poll_landing_page);

        serviceHandler = eventListHandler.serviceHandler;

        final TextView pollDesc = (TextView)findViewById(R.id.pollDescription);

        ImageButton editButton = (ImageButton)findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        Button votePoll = (Button)findViewById(R.id.votePoll);
        final Button closePoll = (Button)findViewById(R.id.closePoll);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.pollLandingPage);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        popupWidth = (int) (dm.widthPixels * 0.8);
        popupHeight = (int) (dm.heightPixels * 0.8);

        PollListPage = new Intent(OpenPollLandingPage.this, PollListPage.class);
        PickPollWinner = new Intent(OpenPollLandingPage.this, PickPollWinnerPage.class);
        EditPollPage = new Intent(OpenPollLandingPage.this, EditPoll.class);
        OpenPollLandingPageReload = new Intent(OpenPollLandingPage.this, OpenPollLandingPage.class);
        final Context mContext = this;


        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            pollID = extras.getString("pollID");
            clonePoll = pollListHandler.getPollCloneFromMap(pollID);
            if (clonePoll == null){
                Toast.makeText(OpenPollLandingPage.this, "No Poll found", LENGTH_LONG).show();
                finish();
            }

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
        }

        votePoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> map = new HashMap<String, Object>();
                Set<String> voteOption = new HashSet<String>();

                Poll pollVersionObj = new Poll();
                pollVersionObj.setEventId(clonePoll.getEventId());
                pollVersionObj.setId(clonePoll.getId());
                pollVersionObj.setVersion

                        (clonePoll.getVersion());

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
                                    OpenPollLandingPageReload.putExtra("eventID", eventID);
                                    OpenPollLandingPageReload.putExtra("pollID", pollID);
                                    finish();
                                    startActivity(OpenPollLandingPageReload);
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
        });

        closePoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickPollWinner.putExtra("eventID", eventID);
                PickPollWinner.putExtra("pollID", pollID);
                PickPollWinner.putExtra("calledFrom", "OpenPollLandingPage");
                startActivity(PickPollWinner);
                finish();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPollPage.putExtra("eventID", eventID);
                EditPollPage.putExtra("pollID", pollID);
                startActivity(EditPollPage);
                finish();
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_poll_landing_page, menu);
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
