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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static android.widget.Toast.LENGTH_LONG;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
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
    private static Event event;
    private static String eventID = null;
    private static String pollID = null;


    static FaroUserContext faroUserContext = FaroUserContext.getInstance();
    String myUserId = faroUserContext.getEmail();
    private static FaroServiceHandler serviceHandler;
    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();

    //Maps to manage Multichoice polls
    private Map<Integer, PollOption> selectedPollMap = new ConcurrentHashMap<>();
    private Map<Integer, PollOption> notSelectedPollMap = new ConcurrentHashMap<>();
    private Map<Integer, PollOption> originalSelectedPollMap = new ConcurrentHashMap<>();

    //Below Integers to manage Single choice polls
    private static final Integer INVALID_SELECTED_INDEX = -1;
    private Integer originalSelectedRadioOption = INVALID_SELECTED_INDEX;
    private Integer selectedRadioOption = INVALID_SELECTED_INDEX;

    private static int popupWidth;
    private static int popupHeight;
    private RelativeLayout popUpRelativeLayout;
    private static final Integer POLL_OPTION_ROW_HEIGHT = 150;

    List<PollOption> pollOptionsList;

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
            event = eventListHandler.getEventCloneFromMap(eventID);
            if (clonePoll == null){
                Toast.makeText(OpenPollLandingPage.this, "No Poll found", LENGTH_LONG).show();
                finish();
            }

            pollOptionsList = clonePoll.getPollOptions();
            pollDesc.setText(clonePoll.getDescription());

            ScrollView radioScrollView = (ScrollView) findViewById(R.id.radioScrollView);
            ScrollView checkboxScrollView = (ScrollView) findViewById(R.id.checkboxScrollView);

            if (!(clonePoll.getCreatorId().equals(myUserId))) {
                closePoll.setVisibility(View.GONE);
            }

            //Depending on the type of clonePoll it will be displayed differently
            if(clonePoll.getMultiChoice()) {
                //Disable visibility for radio ScrollView
                radioScrollView.setVisibility(View.GONE);
                /*
                 * Relative Layout(pollOptionLayout) is composed of 2 Linear Layouts, as mentioned below, besides each other
                 * 1. Linear Layout for list of clonePoll options with checkbox (pollOptionsCheckboxList)
                 * 2. Linear Layout for list of buttons for voter count (voterButtonLinearLayout)
                 */

                LinearLayout pollOptionsCheckboxList = (LinearLayout) findViewById(R.id.pollOptionsCheckboxList);
                LinearLayout voterButtonLinearLayout = (LinearLayout) findViewById(R.id.voterButtonLinearLayout);
                for (Integer i = 0; i < pollOptionsList.size(); i++) {

                    //Create checkbox
                    final CheckBox checkBox = new CheckBox(this);
                    PollOption pollOption = pollOptionsList.get(i);
                    checkBox.setText(pollOption.getOption());
                    if(pollOption.getVoters().contains(myUserId)) {
                        checkBox.setChecked(true);
                        selectedPollMap.put(i, pollOption);
                        originalSelectedPollMap.put(i, pollOption);
                    }else{
                        notSelectedPollMap.put(i, pollOption);
                    }
                    checkBox.setId(i);
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CheckBox clickedCheckBox = (CheckBox)v;
                            Integer id = clickedCheckBox.getId();
                            PollOption pollOption = pollOptionsList.get(id);
                            if(clickedCheckBox.isChecked()){
                                selectedPollMap.put(id, pollOption);
                                notSelectedPollMap.remove(id);
                            }else{
                                selectedPollMap.remove(id);
                                notSelectedPollMap.put(id, pollOption);
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

                    //Insert checkBox into pollOptionsCheckboxList
                    RelativeLayout.LayoutParams checkBoxparams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, POLL_OPTION_ROW_HEIGHT);
                    pollOptionsCheckboxList.addView(checkBox, checkBoxparams);

                    //Insert voter count button into voterButtonLinearLayout
                    RelativeLayout.LayoutParams voterCountButtonParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, POLL_OPTION_ROW_HEIGHT);
                    voterButtonLinearLayout.addView(voterCountButton, voterCountButtonParams);
                }
            }else{
                //Disable visibility for checkbox ScrollView
                checkboxScrollView.setVisibility(View.GONE);
                /*
                 * Relative Layout(pollOptionLayout) is composed of 2 Linear Layouts, as mentioned below, besides each other
                 * 1. RadioGroup for list of clonePoll options with radio buttons (pollOptionsRadioGroup)
                 * 2. Linear Layout for list of buttons for voter count (voterButtonLinearLayout)
                 */
                LinearLayout voterButtonLinearLayout = (LinearLayout) findViewById(R.id.voterButtonLinearLayout1);
                RadioGroup pollOptionsRadioGroup = (RadioGroup) findViewById(R.id.pollOptionsRadioGroup);


                for (Integer i = 0; i < pollOptionsList.size(); i++) {

                    //Create RadioButton
                    final RadioButton button = new RadioButton(this);
                    PollOption pollOption = pollOptionsList.get(i);
                    button.setText(pollOption.getOption());
                    button.setId(i);
                    if(pollOption.getVoters().contains(myUserId)) {
                        button.setChecked(true);
                        originalSelectedRadioOption = i;
                        selectedRadioOption = i;
                    }
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RadioButton clickedRadioButton = (RadioButton)v;
                            Integer id = clickedRadioButton.getId();
                            if(clickedRadioButton.isChecked()){
                                selectedRadioOption = id;
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

                if(clonePoll.getMultiChoice()) {
                    if (originalSelectedPollMap.isEmpty() && selectedPollMap.isEmpty()){
                        Toast.makeText(OpenPollLandingPage.this, "Select atleast one option to Vote", LENGTH_LONG).show();
                        return;
                    }else if(originalSelectedPollMap.equals(selectedPollMap)){
                        Toast.makeText(OpenPollLandingPage.this, "No change to selected options", LENGTH_LONG).show();
                        return;
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
                    if (originalSelectedRadioOption.equals(INVALID_SELECTED_INDEX)
                            && selectedRadioOption.equals(INVALID_SELECTED_INDEX)) {
                        Toast.makeText(OpenPollLandingPage.this, "Select an option to Vote", LENGTH_LONG).show();
                        return;
                    } else if (originalSelectedRadioOption.equals(selectedRadioOption)) {
                        Toast.makeText(OpenPollLandingPage.this, "No change to selected options", LENGTH_LONG).show();
                        return;
                    } else {
                        Toast.makeText(OpenPollLandingPage.this, "Selected option is " + pollOptionsList.get(selectedRadioOption).getOption(), LENGTH_LONG).show();

                        voteOption.add((pollOptionsList.get(selectedRadioOption)).getId());

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
