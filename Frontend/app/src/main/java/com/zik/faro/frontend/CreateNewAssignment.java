package com.zik.faro.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.Item;
import com.zik.faro.data.Unit;

import java.util.ArrayList;

/*
 * We can arrive on this page from 2 different pages.
 */
public class CreateNewAssignment extends android.app.Activity {

    private static Event event;
    private static Activity activity = null;
    String activityID = null;
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private static String eventID = null;
    private static String assigneeName = null;

    Intent EventLandingPage;
    Intent ActivityLandingPage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_assignment);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            activityID = extras.getString("activityID");
            event = eventListHandler.getEventCloneFromMap(eventID);

            TextView assignmentDescription = (TextView)findViewById(R.id.assignmentDescription);
            if (activityID != null) {
                activity = activityListHandler.getActivityFromMap(activityID);
                assignmentDescription.setText("Assignment for " + activity.getName());
            }else {
                assignmentDescription.setText("Assignment for " + event.getEventName());
            }

            final EditText itemEditText = (EditText)findViewById(R.id.itemEditText);
            final EditText itemCountEditText = (EditText)findViewById(R.id.itemCount);
            itemCountEditText.setText("0");

            Spinner inviteeSpinner = (Spinner) findViewById(R.id.inviteeSpinner);
            Spinner itemUnitSpinner = (Spinner) findViewById(R.id.itemUnitSpinner);

            /* TODO the below code should be populated from the Event Invitee list.
             * The adapter should also be a custom adapter to show only the username and store the
             * User info in the list so that when selected, we can get the userID from that to make
             * the API call. Also add a setOnItemClickListener for the scroller
             */
            ArrayList <String> inviteeNamesArray = new ArrayList<>();
            inviteeNamesArray.add("TestUserID");
            inviteeNamesArray.add("GuestUserID");
            ArrayAdapter<String> inviteeNamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, inviteeNamesArray);
            inviteeNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            inviteeSpinner.setAdapter(inviteeNamesAdapter);

            //TODO: Change below code to get the units from the Unit enum. Also add a setOnItemClickListener for the scroller
            ArrayList <String> itemUnitArray = new ArrayList<>();
            itemUnitArray.add("Kg");
            itemUnitArray.add("Pounds");
            itemUnitArray.add("Number");
            itemUnitArray.add("Grams");
            ArrayAdapter<String> itemUnitListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemUnitArray);
            itemUnitListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            itemUnitSpinner.setAdapter(itemUnitListAdapter);


            final ImageButton addNewItemButton = (ImageButton)findViewById(R.id.addNewItemButton);
            addNewItemButton.setImageResource(R.drawable.plus);
            addNewItemButton.setEnabled(false);

            ListView itemList = (ListView)findViewById(R.id.itemList);
            itemList.setTag("CreateNewAssignment");
            final ItemsAdapter itemsAdapter = new ItemsAdapter(this, R.layout.assignment_create_item_row_style);
            itemList.setAdapter(itemsAdapter);

            final Button createNewAssignmentOK = (Button) findViewById(R.id.createNewAssignmentOK);
            createNewAssignmentOK.setEnabled(false);

            if (assignmentListHandler.assignmentAdapter == null) {
                assignmentListHandler.assignmentAdapter = new AssignmentAdapter(this, R.layout.event_row_style);
            }

            final Intent assignmentLandingPage = new Intent(CreateNewAssignment.this, AssignmentLandingPage.class);
            EventLandingPage = new Intent(CreateNewAssignment.this, EventLandingPage.class);
            ActivityLandingPage = new Intent(CreateNewAssignment.this, ActivityLandingPage.class);

            //Enable the addNewItem only after Users enters an item
            itemEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    boolean buttonStatus = !(itemEditText.getText().toString().trim().isEmpty());
                    addNewItemButton.setEnabled(buttonStatus);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            inviteeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //Below code will change when we insert UserInfo. We will get the username from that object
                    assigneeName = (String) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            addNewItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String itemDescription = itemEditText.getText().toString();
                    int itemCount = Integer.parseInt(itemCountEditText.getText().toString());

                    //TODO: Below instead of passing the assignee Name we need to pass the assignee ID which is the need of the constructor
                    Item item = null;
                    try {
                        item = new Item(itemDescription, assigneeName, itemCount, Unit.KG);
                    } catch (IllegalDataOperation illegalDataOperation) {
                        illegalDataOperation.printStackTrace();
                    }
                    itemsAdapter.insert(item, 0);
                    itemsAdapter.notifyDataSetChanged();
                    itemEditText.setText("");
                    itemCountEditText.setText("0");
                    if (itemsAdapter.getCount() == 1){
                        createNewAssignmentOK.setEnabled(true);
                    }
                }
            });


            createNewAssignmentOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemsAdapter.getCount() < 1){
                        createNewAssignmentOK.setEnabled(false);
                        return;
                    }
                    Assignment assignment = new Assignment(ActionStatus.INCOMPLETE);
                    assignment.setItems(itemsAdapter.list);
                    assignmentListHandler.addNewAssignment(assignment);
                    //Means the assignment is getting created for an event
                    if (activityID == null){
                        event.setAssignment(assignment);
                    }else{
                        activity.setAssignment(assignment);
                    }
                    assignmentLandingPage.putExtra("assignmentID", assignment.getId());
                    assignmentLandingPage.putExtra("eventID", event.getEventId());
                    assignmentLandingPage.putExtra("activityID", activityID);
                    startActivity(assignmentLandingPage);
                }
            });


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (activityID == null) {
            EventLandingPage.putExtra("eventID", eventID);
            startActivity(EventLandingPage);
            finish();
        } else {
            ActivityLandingPage.putExtra("eventID", eventID);
            ActivityLandingPage.putExtra("activityID", activityID);
            startActivity(ActivityLandingPage);
            finish();
        }
    }

}
