package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.zik.faro.data.Event;
import com.zik.faro.data.Poll;

public class PollListPage extends Activity {

    Intent EventLandingPage = null;
    Intent OpenPollLandingPage = null;
    Intent ClosedPollLandingPage = null;
    static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static Event E;
    private static String eventID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_list_page);

        EventLandingPage = new Intent(PollListPage.this, EventLandingPage.class);
        OpenPollLandingPage = new Intent(PollListPage.this, OpenPollLandingPage.class);
        ClosedPollLandingPage = new Intent(PollListPage.this, ClosedPollLandingPage.class);
        final Intent CreateNewPoll = new Intent(PollListPage.this, CreateNewPoll.class);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            //TODO: Error condition. Handle it. Below code is incorrect
            EventLandingPage.putExtra("eventID", E.getEventId());
            startActivity(EventLandingPage);
            finish();
            return;
        }

        eventID = extras.getString("eventID");
        E = eventListHandler.getEventCloneFromMap(eventID);

        TextView Poll = (TextView)findViewById(R.id.polls);
        //Poll.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        TabHost pollTabHost = (TabHost)findViewById(R.id.polltabHost);
        pollTabHost.setup();

        TabHost.TabSpec openTabSpec = pollTabHost.newTabSpec("openTab");
        openTabSpec.setContent(R.id.openTab);
        openTabSpec.setIndicator("Open Polls");
        pollTabHost.addTab(openTabSpec);

        TabHost.TabSpec closedTabSpec = pollTabHost.newTabSpec("closedTab");
        closedTabSpec.setContent(R.id.closedTab);
        closedTabSpec.setIndicator("Closed Polls");
        pollTabHost.addTab(closedTabSpec);

        ListView openPollsListView  = (ListView)findViewById(R.id.openPollsList);
        openPollsListView.setBackgroundColor(Color.BLACK);
        if (pollListHandler.openPollsAdapter == null) {
            pollListHandler.openPollsAdapter = new PollAdapter(this, R.layout.poll_list_page_row_style);
        }

        ListView closedPollsListView  = (ListView)findViewById(R.id.closedPollsList);
        closedPollsListView.setBackgroundColor(Color.BLACK);
        if (pollListHandler.closedPollsAdapter == null) {
            pollListHandler.closedPollsAdapter = new PollAdapter(this, R.layout.poll_list_page_row_style);
        }

        //TODO: Based on eventID make API call to get Polls for this event
        /*
        for(each poll in event){
            pollListHandler.addNewPoll(poll);
        }
        */

        openPollsListView.setAdapter(pollListHandler.openPollsAdapter);
        closedPollsListView.setAdapter(pollListHandler.closedPollsAdapter);


        ImageButton createNewPoll = (ImageButton)findViewById(R.id.add_new_poll);
        createNewPoll.setImageResource(R.drawable.plus);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        //Listener to go to the Create New Poll  Page for the poll selected in the "Open" list
        openPollsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pollSelectedFromList(parent, position, OpenPollLandingPage);
            }
        });

        //Listener to go to the Create New Poll  Page for the poll selected in the "Closed" list
        closedPollsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pollSelectedFromList(parent, position, ClosedPollLandingPage);
            }
        });



        createNewPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pollListHandler.getCombinedListSize() == PollListHandler.MAX_TOTAL_POLLS_PER_EVENT) {
                    //DO NOT ALLOW NEW POLL CREATION
                }else {
                    CreateNewPoll.putExtra("eventID", E.getEventId());
                    startActivity(CreateNewPoll);
                    finish();
                }
            }
        });


    }

    private void pollSelectedFromList(AdapterView<?> parent, int position, Intent intent) {
        Poll poll = (Poll) parent.getItemAtPosition(position);
        intent.putExtra("eventID", E.getEventId());
        intent.putExtra("pollID", poll.getId());
        startActivity(intent);
        finish();
    }







    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //This would probably lead to increase in stack size for Intents since the previous intents are
    //not popped out of the stack. Check how to see the stack size of the intents.
    @Override
    public void onBackPressed() {
        EventLandingPage.putExtra("eventID", eventID);
        startActivity(EventLandingPage);
        finish();
        super.onBackPressed();
    }
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************



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
