package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.Item;

import java.util.List;

public class AssignmentLandingPage extends Activity{

    private static Event event;
    private static com.zik.faro.data.Activity activity = null;
    String activityID = null;
    String eventID = null;
    private static Assignment assignment = null;
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();

    ListView itemList;
    Intent EventLandingPage;
    Intent ActivityLandingPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_landing_page);
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            event = eventListHandler.getEventFromMap(eventID);

            activityID = extras.getString("activityID");
            String assignmentID = extras.getString("assignmentID");
            assignment = assignmentListHandler.getAssignmentFromMap(assignmentID);

            final TextView assignmentDescription = (TextView) findViewById(R.id.assignmentDescription);
            if (activityID != null) {
                activity = activityListHandler.getActivityFromMap(activityID);
                assignmentDescription.setText("Assignment for " + activity.getName());
            } else {
                assignmentDescription.setText("Assignment for " + event.getEventName());
            }

            ImageButton editButton = (ImageButton) findViewById(R.id.editButton);
            editButton.setImageResource(R.drawable.edit);

            Button updateAssignment = (Button) findViewById(R.id.updateAssignment);

            EventLandingPage = new Intent(AssignmentLandingPage.this, EventLandingPage.class);
            ActivityLandingPage = new Intent(AssignmentLandingPage.this, ActivityLandingPage.class);
            final Intent EditAssignmentPage = new Intent(AssignmentLandingPage.this, EditAssignment.class);


            itemList = (ListView) findViewById(R.id.itemList);
            itemList.setTag("AssignmentLandingPage");
            final ItemsAdapter itemsAdapter = new ItemsAdapter(this, R.layout.assignment_update_item_row_style);
            itemList.setAdapter(itemsAdapter);

            List<Item> items = assignment.getItems();
            for (Integer i = items.size() - 1; i >= 0; i--) {
                Item item = items.get(i);
                if (item.getStatus().equals(ActionStatus.COMPLETE)) {
                    itemsAdapter.insertAtEnd(item);
                } else {
                    itemsAdapter.insertAtBeginning(item);
                }
            }

            updateAssignment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Update the server with the update for the items
                    assignment.setItems(itemsAdapter.list);
                    if (activityID != null) {
                        ActivityLandingPage.putExtra("eventID", eventID);
                        ActivityLandingPage.putExtra("activityID", activityID);
                        startActivity(ActivityLandingPage);
                        finish();
                    } else {
                        EventLandingPage.putExtra("eventID", eventID);
                        startActivity(EventLandingPage);
                        finish();

                    }
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditAssignmentPage.putExtra("eventID", eventID);
                    EditAssignmentPage.putExtra("activityID", activityID);
                    startActivity(EditAssignmentPage);
                    finish();
                }
            });


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (activityID != null) {
            ActivityLandingPage.putExtra("eventID", eventID);
            ActivityLandingPage.putExtra("activityID", activityID);
            startActivity(ActivityLandingPage);
            finish();
        } else {
            EventLandingPage.putExtra("eventID", eventID);
            startActivity(EventLandingPage);
            finish();
        }
    }
}
