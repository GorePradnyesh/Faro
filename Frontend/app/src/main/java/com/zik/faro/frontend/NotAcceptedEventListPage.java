package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zik.faro.data.Event;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.ui.activities.EventLandingPage;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;

public class NotAcceptedEventListPage extends Activity {

    private ListView theNotAcceptedListView = null;
    private EventListHandler eventListHandler = EventListHandler.getInstance(this);
    private Intent eventLanding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_accepted_event_list_page);

        eventLanding = new Intent(this, EventLandingPage.class);

        theNotAcceptedListView  = (ListView) findViewById(R.id.notAcceptedList);

        theNotAcceptedListView.setAdapter(eventListHandler.notAcceptedEventAdapter);
        theNotAcceptedListView.setSelectionFromTop(
                eventListHandler.getNotAcceptedFutureEventsStartingPosition(), 0);

        //Listener to go to the Event Landing Page for the event selected in the "Not Accepted" list
        theNotAcceptedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                eventSelectedFromList(parent, position, eventLanding);
            }
        });
    }

    private void eventSelectedFromList(AdapterView<?> parent, int position, Intent eventLanding) {
        Event event = (Event) parent.getItemAtPosition(position);
        FaroIntentInfoBuilder.eventIntent(eventLanding, event.getId());
        //TODO: check if startActivityForResult is a better way
        startActivity(eventLanding);
    }
}
