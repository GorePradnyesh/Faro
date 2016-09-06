package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
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

import com.zik.faro.data.Event;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;

import java.util.ArrayList;
import java.util.List;

public class ClosedPollLandingPage extends ActionBarActivity {

    private static String eventID = null;
    private static String pollID = null;
    private static Poll P;
    private static Event E;
    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private  static EventListHandler eventListHandler = EventListHandler.getInstance();

    List<PollOption> pollOptionsList;

    private static int popupWidth;
    private static int popupHeight;
    private RelativeLayout popUpRelativeLayout;
    private static final Integer POLL_OPTION_ROW_HEIGHT = 100;

    Intent PickPollWinner = null;
    Intent OpenPollLandingPage = null;
    Intent PollListPage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closed_poll_landing_page);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        final TextView pollDesc = (TextView)findViewById(R.id.pollDescription);
        final TextView winnerPollOptionTV = (TextView) findViewById(R.id.winnerPollOptionTextView);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.closedPollLandingPage);

        final Button changeWinner = (Button)findViewById(R.id.changeWinner);
        final Button reOpenPoll = (Button)findViewById(R.id.reOpenPoll);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        popupWidth = (int) (dm.widthPixels * 0.8);
        popupHeight = (int) (dm.heightPixels * 0.8);

        OpenPollLandingPage = new Intent(ClosedPollLandingPage.this, OpenPollLandingPage.class);
        PickPollWinner = new Intent(ClosedPollLandingPage.this, PickPollWinner.class);
        PollListPage = new Intent(ClosedPollLandingPage.this, PollListPage.class);


        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            pollID = extras.getString("pollID");
            P = pollListHandler.getPollCloneFromMap(pollID);
            E = eventListHandler.getEventCloneFromMap(eventID);
            pollOptionsList = P.getPollOptions();
            pollDesc.setText(P.getDescription());

            LinearLayout pollOptionsTextList = (LinearLayout) findViewById(R.id.pollOptionsTextList);
            LinearLayout voterButtonLinearLayout = (LinearLayout) findViewById(R.id.voterButtonLinearLayout);
            for (Integer i = 0; i < pollOptionsList.size(); i++) {
                //Create TextView to display poll Option
                final TextView textView = new TextView(this);
                PollOption pollOption = pollOptionsList.get(i);

                textView.setText(pollOption.getOption());
                textView.setGravity(Gravity.CENTER_VERTICAL);
                //Setting the TextView to display the winning Option
                if ((pollOption.getId().equals(P.getWinnerId()))){
                    winnerPollOptionTV.setText("Winner is: " + pollOption.getOption());
                    textView.setTextColor(Color.RED);
                }

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
                    PickPollWinner.putExtra("eventID", E.getEventId());
                    PickPollWinner.putExtra("pollID", P.getId());
                    PickPollWinner.putExtra("calledFrom", "ClosedPollLandingPage");
                    startActivity(PickPollWinner);
                    finish();
                }
            });

            reOpenPoll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO update server of reopening the Poll

                    P.setWinnerId(null);
                    pollListHandler.changePollStatusToOpen(P);
                    OpenPollLandingPage.putExtra("eventID", E.getEventId());
                    OpenPollLandingPage.putExtra("pollID", P.getId());
                    startActivity(OpenPollLandingPage);
                    finish();
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
            voters.add(temp);
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
}
