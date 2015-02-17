package com.faroapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.faroapp.R;


public class EventLandingPage extends Activity {

    public static final int NO_CHANGES = 0;
    Intent AppLandingPage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_landing_page);

        TextView event_name = (TextView)findViewById(R.id.eventNameText);
        TextView event_status = (TextView)findViewById(R.id.eventStatusText);
        TextView startDate = (TextView)findViewById(R.id.startDateDisplay);
        TextView startTime = (TextView)findViewById(R.id.startTimeDisplay);

        TextView endDate = (TextView)findViewById(R.id.endDateDisplay);
        TextView endTime = (TextView)findViewById(R.id.endTimeDisplay);

        ImageButton backButton = (ImageButton)findViewById(R.id.back_to_app_landing);
        backButton.setImageResource(R.drawable.round_left_arrow);

        ImageButton pollButton = (ImageButton)findViewById(R.id.pollImageButton);
        pollButton.setImageResource(R.drawable.poll_icon);

        AppLandingPage = new Intent(EventLandingPage.this, com.faroapp.AppLandingPage.class);
        final Intent PollLandingPage = new Intent(EventLandingPage.this, com.faroapp.PollLandingPage.class);


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            Event E = extras.getParcelable("Event");
            if(extras != null){
                String ev_name = E.getEventName();
                int ev_status = E.getStatus();

                event_name.setText(ev_name);
                switch (ev_status){
                    case 0:
                        event_status.setText("Accepted");
                        break;
                    case 1:
                        event_status.setText("Pending");
                        break;
                    case 2:
                        event_status.setText("Not Accepted");
                        break;
                }
            }
            startDate.setText(E.getStartDateStr());
            startTime.setText(E.getStartTimeStr());

            endDate.setText(E.getEndDateStr());
            endTime.setText(E.getEndTimeStr());
        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(NO_CHANGES);
                startActivity(AppLandingPage);
                finish();
            }
        });

        pollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(NO_CHANGES);
                startActivity(PollLandingPage);
            }
        });
    }

    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //This would probably lead to increase in stack size for Intents since the previous intents are
    //not popped out of the stack. Check how to see the stack size of the intents.
    @Override
    public void onBackPressed() {
        setResult(NO_CHANGES);
        startActivity(AppLandingPage);
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
        getMenuInflater().inflate(R.menu.menu_event_landing_page, menu);
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
