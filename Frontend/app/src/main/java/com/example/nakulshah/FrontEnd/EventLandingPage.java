package com.example.nakulshah.FrontEnd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class EventLandingPage extends Activity {

    public static final int NO_CHANGES = 0;
    private Intent AppLandingPage = null;
    private DateFormat sdf = DateFormat.getDateInstance();
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private  static EventListHandler eventListHandler = EventListHandler.getInstance();
    public static Event E;

    private Button statusYes = null;
    private Button statusNo = null;
    private Button statusMaybe = null;
    private ImageButton pollButton = null;
    private TextView event_status = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_landing_page);

        TextView event_name = (TextView)findViewById(R.id.eventNameText);
        event_status = (TextView)findViewById(R.id.eventStatusText);
        TextView startDate = (TextView)findViewById(R.id.startDateDisplay);
        TextView startTime = (TextView)findViewById(R.id.startTimeDisplay);

        TextView endDate = (TextView)findViewById(R.id.endDateDisplay);
        TextView endTime = (TextView)findViewById(R.id.endTimeDisplay);

        pollButton = (ImageButton)findViewById(R.id.pollImageButton);
        pollButton.setImageResource(R.drawable.poll_icon);

        statusYes = (Button)findViewById(R.id.statusYes);
        statusNo = (Button)findViewById(R.id.statusNo);
        statusMaybe = (Button)findViewById(R.id.statusMaybe);

        AppLandingPage = new Intent(EventLandingPage.this, AppLandingPage.class);
        final Intent PollLandingPage = new Intent(EventLandingPage.this, PollLandingPage.class);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            String eventID = extras.getString("eventID");
            E = eventListHandler.getEventFromMap(eventID);
            if(E != null) {

                //Display elements based on Event Status
                eventStateBasedView();

                String ev_name = E.getEventName();
                event_name.setText(ev_name);

                startDate.setText(sdf.format(E.getStartDateCalendar().getTime()));
                startTime.setText(stf.format(E.getStartDateCalendar().getTime()));

                endDate.setText(sdf.format(E.getEndDateCalendar().getTime()));
                endTime.setText(stf.format(E.getEndDateCalendar().getTime()));

                statusYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        E.setEventStatusProperties(EventStatus.ACCEPTEDANDCOMPLETE);
                        eventStateBasedView();
                    }
                });

                statusMaybe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        E.setEventStatusProperties(EventStatus.MAYBE);
                        eventStateBasedView();
                    }
                });

                //TODO Handle listener for statusNo
            }
        }

        pollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(NO_CHANGES);
                startActivity(PollLandingPage);
            }
        });
    }

    private void eventStateBasedView(){
        switch (this.E.getEventStatus()){
            case ACCEPTEDANDPENDING:
                event_status.setText("Accepted but Pending");
                break;
            case ACCEPTEDANDCOMPLETE:
                event_status.setText("Accepted and not Pending");
                break;
            case MAYBE:
                event_status.setText("Maybe");
                break;
            case NOTRESPONDED:
                event_status.setText("Not responded");
                break;
        }

        if (this.E.isEventCreator()){
            statusYes.setVisibility(View.GONE);
            statusNo.setVisibility(View.GONE);
            statusMaybe.setVisibility(View.GONE);
            pollButton.setVisibility(View.VISIBLE);
        }else if (this.E.getEventStatus() == EventStatus.ACCEPTEDANDCOMPLETE){
            statusYes.setVisibility(View.GONE);
            statusNo.setVisibility(View.GONE);
            statusMaybe.setVisibility(View.GONE);
            pollButton.setVisibility(View.VISIBLE);
        }else if (this.E.getEventStatus() == EventStatus.NOTRESPONDED) {
            statusMaybe.setVisibility(View.VISIBLE);
            statusYes.setVisibility(View.VISIBLE);
            statusNo.setVisibility(View.VISIBLE);
            pollButton.setVisibility(View.GONE);
        } else if (this.E.getEventStatus() == EventStatus.MAYBE){
            statusYes.setVisibility(View.VISIBLE);
            statusNo.setVisibility(View.VISIBLE);
            statusMaybe.setVisibility(View.GONE);
            pollButton.setVisibility(View.GONE);
        }
    }

    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //**********************************************************************************************
    //This would probably lead to increase in stack size for Intents since the previous intents are
    //not popped out of the stack. Check how to see the stack size of the intents.
    @Override
    public void onBackPressed() {
        eventListHandler.deleteEventFromMapIfNotInList(E);
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
