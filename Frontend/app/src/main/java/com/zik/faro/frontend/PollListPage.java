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

public class PollListPage extends Activity {

    Intent eventLanding = null;
    Intent pollLanding = null;
    static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static String eventID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_list_page);

        eventLanding = new Intent(PollListPage.this, EventLandingPage.class);
        pollLanding = new Intent(PollListPage.this, PollLandingPage.class);
        final Intent CreateNewPoll = new Intent(PollListPage.this, CreateNewPoll.class);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
             eventID = extras.getString("eventID");
        }


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
        pollListHandler.openPollsAdapter = new PollAdapter(this, R.layout.poll_list_page_row_style);
        openPollsListView.setAdapter(pollListHandler.openPollsAdapter);


        ListView closedPollsListView  = (ListView)findViewById(R.id.closedPollsList);
        closedPollsListView.setBackgroundColor(Color.BLACK);
        pollListHandler.closedPollsAdapter = new PollAdapter(this, R.layout.poll_list_page_row_style);
        closedPollsListView.setAdapter(pollListHandler.closedPollsAdapter);


        ImageButton createNewPoll = (ImageButton)findViewById(R.id.add_new_poll);
        createNewPoll.setImageResource(R.drawable.plus);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        //Listener to go to the Create New Poll  Page for the poll selected in the "Open" list
        openPollsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pollSelectedFromList(parent, position, pollLanding);
            }
        });

        //Listener to go to the Create New Poll  Page for the poll selected in the "Closed" list
        closedPollsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pollSelectedFromList(parent, position, pollLanding);
            }
        });



        createNewPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pollListHandler.getCombinedListSize() == PollListHandler.MAX_TOTAL_POLLS_PER_EVENT) {
                    //DO NOT ALLOW NEW POLL CREATION
                }else {
                    CreateNewPoll.putExtra("eventID", eventID);
                    startActivity(CreateNewPoll);
                    //finish();
                }
            }
        });


    }

    private void pollSelectedFromList(AdapterView<?> parent, int position, Intent eventLanding) {
        Poll poll = (Poll) parent.getItemAtPosition(position);
            pollLanding.putExtra("pollID", poll.getPollId());
            startActivity(pollLanding);
    }







    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //This would probably lead to increase in stack size for Intents since the previous intents are
    //not popped out of the stack. Check how to see the stack size of the intents.
    @Override
    public void onBackPressed() {
        eventLanding.putExtra("eventID", eventID);
        startActivity(eventLanding);
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
