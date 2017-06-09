package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.Poll;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.List;

public class PollListPage extends Activity {
    private Intent EventLandingPage = null;
    private Intent PollLandingPageIntent = null;
    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private String eventID = null;
    private static FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private Intent CreateNewPoll = null;
    private Bundle extras = null;
    private TextView Poll = null;
    private TabHost pollTabHost = null;
    private ListView openPollsListView = null;
    private ListView closedPollsListView = null;
    private ImageButton createNewPoll = null;
    private String TAG = "PollListPage";
    private Context mContext = this;
    private RelativeLayout pollListPageRelativeLayout = null;
    private LinearLayout linlaHeaderProgress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_list_page);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        EventLandingPage = new Intent(PollListPage.this, EventLandingPage.class);
        PollLandingPageIntent = new Intent(PollListPage.this, PollLandingPage.class);
        CreateNewPoll = new Intent(PollListPage.this, CreateNewPoll.class);

        extras = getIntent().getExtras();
        if(extras == null) return; //TODO How to handle this case?

        linlaHeaderProgress = (LinearLayout)findViewById(R.id.linlaHeaderProgress);
        pollListPageRelativeLayout = (RelativeLayout) findViewById(R.id.pollListPageRelativeLayout);
        pollListPageRelativeLayout.setVisibility(View.GONE);

        eventID = extras.getString("eventID");

        getPollsFromServer();
    }

    private void setupPageDetails() {

        linlaHeaderProgress.setVisibility(View.GONE);
        pollListPageRelativeLayout.setVisibility(View.VISIBLE);

        Poll = (TextView)findViewById(R.id.polls);
        //Poll.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        pollTabHost = (TabHost)findViewById(R.id.polltabHost);
        pollTabHost.setup();

        TabHost.TabSpec openTabSpec = pollTabHost.newTabSpec("openTab");
        openTabSpec.setContent(R.id.openTab);
        openTabSpec.setIndicator("Open Polls");
        pollTabHost.addTab(openTabSpec);

        TabHost.TabSpec closedTabSpec = pollTabHost.newTabSpec("closedTab");
        closedTabSpec.setContent(R.id.closedTab);
        closedTabSpec.setIndicator("Closed Polls");
        pollTabHost.addTab(closedTabSpec);

        openPollsListView  = (ListView)findViewById(R.id.openPollsList);
        openPollsListView.setBackgroundColor(Color.BLACK);

        closedPollsListView  = (ListView)findViewById(R.id.closedPollsList);
        closedPollsListView.setBackgroundColor(Color.BLACK);

        openPollsListView.setAdapter(pollListHandler.getOpenPollAdapter(eventID, this));
        closedPollsListView.setAdapter(pollListHandler.getClosedPollAdapter(eventID, this));

        createNewPoll = (ImageButton)findViewById(R.id.add_new_poll);
        createNewPoll.setImageResource(R.drawable.plus);

        //Listener to go to the Create New Poll  Page for the poll selected in the "Open" list
        openPollsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pollSelectedFromList(parent, position, PollLandingPageIntent);
            }
        });

        //Listener to go to the Create New Poll  Page for the poll selected in the "Closed" list
        closedPollsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pollSelectedFromList(parent, position, PollLandingPageIntent);
            }
        });

        createNewPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pollListHandler.getCombinedListSize(eventID, mContext) == PollListHandler.MAX_TOTAL_POLLS_PER_EVENT) {
                    //DO NOT ALLOW NEW POLL CREATION
                }else {
                    CreateNewPoll.putExtra("eventID", eventID);
                    startActivity(CreateNewPoll);
                }
            }
        });
    }

    private void getPollsFromServer (){
        //Based on eventID make API call to get Polls for this cloneEvent
        serviceHandler.getPollHandler().getPolls(new BaseFaroRequestCallback<List<com.zik.faro.data.Poll>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get polls list");
            }

            @Override
            public void onResponse(final List<Poll> polls, HttpError error) {
                if (error == null ) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received polls from the server!!");
                            pollListHandler.addDownloadedPollsToListAndMap(eventID, polls, mContext);
                            setupPageDetails();
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else{
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID);
    }

    private void pollSelectedFromList(AdapterView<?> parent, int position, Intent intent) {
        Poll poll = (Poll) parent.getItemAtPosition(position);
        intent.putExtra("eventID", eventID);
        intent.putExtra("pollID", poll.getId());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
