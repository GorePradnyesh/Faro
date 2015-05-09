package com.zik.faro.frontend;

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
    //private Intent AppLandingPage = null;
    private DateFormat sdf = DateFormat.getDateInstance();
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private  static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static Event E;

    private Button statusYes = null;
    private Button statusNo = null;
    private Button statusMaybe = null;
    private ImageButton pollButton = null;
    private ImageButton editButton = null;
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
        editButton = (ImageButton)findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);


        statusYes = (Button)findViewById(R.id.statusYes);
        statusNo = (Button)findViewById(R.id.statusNo);
        statusMaybe = (Button)findViewById(R.id.statusMaybe);

        //AppLandingPage = new Intent(EventLandingPage.this, AppLandingPage.class);
        final Intent PollLandingPage = new Intent(EventLandingPage.this, PollLandingPage.class);
        final Intent EditEvent = new Intent(EventLandingPage.this, EditEvent.class);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

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
                        eventListHandler.changeEventStatusToYes(E);
                        eventStateBasedView();
                    }
                });

                statusMaybe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        eventListHandler.changeEventStatusToMaybe(E);
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

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditEvent.putExtra("eventID", E.getEventId());
                startActivity(EditEvent);
                finish();
            }
        });
    }

    private void eventStateBasedView(){
        switch (E.getEventStatus()){
            case ACCEPTED:
                event_status.setText("Accepted");
                break;
            case MAYBE:
                event_status.setText("Maybe");
                break;
            case NOTRESPONDED:
                event_status.setText("Not responded");
                break;
        }

        if (E.isEventCreator()){
            statusYes.setVisibility(View.GONE);
            statusNo.setVisibility(View.GONE);
            statusMaybe.setVisibility(View.GONE);
            pollButton.setVisibility(View.VISIBLE);
        }else if (E.getEventStatus() == EventStatus.ACCEPTED){
            statusYes.setVisibility(View.GONE);
            statusNo.setVisibility(View.GONE);
            statusMaybe.setVisibility(View.GONE);
            pollButton.setVisibility(View.VISIBLE);
        }else if (E.getEventStatus() == EventStatus.NOTRESPONDED) {
            statusMaybe.setVisibility(View.VISIBLE);
            statusYes.setVisibility(View.VISIBLE);
            statusNo.setVisibility(View.VISIBLE);
            pollButton.setVisibility(View.GONE);
        } else if (E.getEventStatus() == EventStatus.MAYBE){
            statusYes.setVisibility(View.VISIBLE);
            statusNo.setVisibility(View.VISIBLE);
            statusMaybe.setVisibility(View.GONE);
            pollButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        eventListHandler.deleteEventFromMapIfNotInList(E);
        finish();
        super.onBackPressed();
    }


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
