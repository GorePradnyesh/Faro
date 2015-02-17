package com.faroapp;

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

import com.faroapp.R;

/*
 * This is the page where all the events are listed in separate tabs based on invitation "Accepted"
 * or not.
 * Within the Accepted group, the events could have some pending stuff like unfinished
 * assignment, pending poll, etc, which needs to be represented differently.
 * Within the Not Accepted group, 2 types of events are present. One of them is  for which response
 * to the invitation is pending and the other is for which the response is Maybe. These should be
 * represented differently.
 * From this page, we can
 *      1. Create a New Event (Bottom right corner)
 *      2. Click on and existing event and go to its Event Landing Page.
 *      3. Change the view to Calendar View. (Bottom left corner)
 *      4. Check the latest chats for the events. (Top right corner)
 *      5. See the App related options like Settings, Profile page, etc (Top Left corner)
*/

public class AppLandingPage extends Activity {

    /*public static final int NO_CHANGES = 0;
    public static final int EVENT_UPDATE_TYPE = 100;
    public static final int CREATE_EVENT = 101;*/


    EventGenerator evntGen = EventGenerator.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_landing_page);

        TabHost eventTabHost = (TabHost)findViewById(R.id.eventtabHost);

        eventTabHost.setup();
        TabHost.TabSpec acceptedTabSpec = eventTabHost.newTabSpec("accepted");
        acceptedTabSpec.setContent(R.id.accepted);
        acceptedTabSpec.setIndicator("Accepted");
        eventTabHost.addTab(acceptedTabSpec);

        TabHost.TabSpec notAcceptedTabSpec = eventTabHost.newTabSpec("not_accepted");
        notAcceptedTabSpec.setContent(R.id.not_accepted);
        notAcceptedTabSpec.setIndicator("Not Accepted");
        eventTabHost.addTab(notAcceptedTabSpec);

        ListView theAcceptedListView  = (ListView)findViewById(R.id.accepted_list);
        theAcceptedListView.setBackgroundColor(Color.BLACK);
        EventAdapter acceptedEventAdapter = new EventAdapter(this, R.layout.event_row_style);
        theAcceptedListView.setAdapter(acceptedEventAdapter);

        ListView theNotAcceptedListView  = (ListView)findViewById(R.id.not_accepted_list);
        theNotAcceptedListView.setBackgroundColor(Color.BLACK);
        EventAdapter notAcceptedEventAdapter = new EventAdapter(this, R.layout.event_row_style);
        theNotAcceptedListView.setAdapter(notAcceptedEventAdapter);

        ImageButton add_event = (ImageButton)findViewById(R.id.add_event);
        add_event.setImageResource(R.drawable.plus);
        ImageButton calendar_view = (ImageButton)findViewById(R.id.calendar_view);
        calendar_view.setImageResource(R.drawable.calendar);

        final Intent eventLanding = new Intent(AppLandingPage.this, EventLandingPage.class);
        final Intent create_event_page = new Intent(AppLandingPage.this, CreateNewEvent.class);
        final Intent eventCalendarView = new Intent(AppLandingPage.this, EventCalendarView.class);

        //TODO: Insert newly created Event in the list based on start Date if in view

        for (int i = 0; i < evntGen.getAcceptedEventCount(); i++){
            acceptedEventAdapter.add(evntGen.getAcceptedEventAtPosition(i));
        }

        for (int i = 0; i < evntGen.getNotAcceptedEventCount(); i++){
            notAcceptedEventAdapter.add(evntGen.getNotAcceptedEventAtPosition(i));
        }

        //Listener to go to the Event Landing Page for the event selected in the "Accepted" list
        theAcceptedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                eventSelectedFromList(parent, position, eventLanding);
            }
        });

        //Listener to go to the Event Landing Page for the event selected in the "Not Accepted" list
        theNotAcceptedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                eventSelectedFromList(parent, position, eventLanding);
            }
        });

        //Listener to go to the Create Event page when clicked on add_event button.
        add_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(create_event_page);
                finish();
            }
        });

        //Listener to change the view from List View to Calendar View
        calendar_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(eventCalendarView);
                //finish();
            }
        });
    }

    private void eventSelectedFromList(AdapterView<?> parent, int position, Intent eventLanding){
        Event E = (Event) parent.getItemAtPosition(position);
        eventLanding.putExtra("Event", E);
        eventLanding.putExtra("StartDateCalendar", E.getStartDateCalendar());
        eventLanding.putExtra("EndDateCalendar", E.getEndDateCalendar());
        startActivity(eventLanding);
        finish();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case  EVENT_UPDATE_TYPE:
                switch(resultCode){
                    case NO_CHANGES:
                        Toast.makeText(AppLandingPage.this, "No Changes made to the event", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(AppLandingPage.this, "resultCode failed", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case CREATE_EVENT:
                switch(resultCode) {
                    case NO_CHANGES:
                        Toast.makeText(AppLandingPage.this, "Event added", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(AppLandingPage.this, "Event not added", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            default:
                Toast.makeText(AppLandingPage.this, "requestCode failed", Toast.LENGTH_SHORT).show();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app_landing_page, menu);
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
