package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.Item;
import com.zik.faro.data.Unit;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

/*
 * We can arrive on this page from 2 different pages.
 */
public class CreateNewAssignment extends android.app.Activity {

    private static Event originalEvent;
    private static Activity originalActivity = null;
    private static Assignment cloneAssignment;

    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;

    private String eventID = null;
    private String activityID = null;
    private String assigneeID = null;
    private String assignmentID = null;
    private Unit selectedUnit;

    Intent AssignmentLandingPageIntent;
    Intent ActivityLandingPage;

    private static String TAG = "CreateNewAssignment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_assignment);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            activityID = extras.getString("activityID");
            assignmentID = extras.getString("assignmentID");

            originalEvent = eventListHandler.getOriginalEventFromMap(eventID);
            if (activityID != null) {
                originalActivity = activityListHandler.getOriginalActivityFromMap(activityID);
            }
            cloneAssignment = assignmentListHandler.getAssignmentCloneFromMap(assignmentID);

            TextView assignmentDescription = (TextView)findViewById(R.id.assignmentDescription);
            if (activityID != null) {
                assignmentDescription.setText("Assignment for " + originalActivity.getName());
            }else {
                assignmentDescription.setText("Assignment for " + originalEvent.getEventName());
            }

            final EditText itemNameEditText = (EditText)findViewById(R.id.itemNameEditText);
            final EditText itemCountEditText = (EditText)findViewById(R.id.itemCount);
            itemCountEditText.setText("0");

            Spinner inviteeSpinner = (Spinner) findViewById(R.id.inviteeSpinner);
            Spinner itemUnitSpinner = (Spinner) findViewById(R.id.itemUnitSpinner);

            inviteeSpinner.setAdapter(eventFriendListHandler.acceptedFriendAdapter);

            final Context mContext = this;

            ArrayList <String> itemUnitArray = new ArrayList<>();
            for (Unit unit : Unit.values()){
                itemUnitArray.add(unit.toString());
            }
            ArrayAdapter<String> itemUnitListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemUnitArray);
            itemUnitListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            itemUnitSpinner.setAdapter(itemUnitListAdapter);

            final ImageButton addNewItemButton = (ImageButton)findViewById(R.id.addNewItemButton);
            addNewItemButton.setImageResource(R.drawable.plus);
            addNewItemButton.setEnabled(false);

            ListView itemList = (ListView)findViewById(R.id.itemList);
            itemList.setTag("CreateNewAssignment");
            final ItemsAdapter itemsAdapter = new ItemsAdapter(this, R.layout.item_cant_edit_row_style);
            itemList.setAdapter(itemsAdapter);

            for (Integer i = cloneAssignment.getItems().size() - 1; i >= 0; i--) {
                Item item = cloneAssignment.getItems().get(i);
                if (item.getStatus() == ActionStatus.COMPLETE) {
                    itemsAdapter.insertAtEnd(item);
                } else {
                    itemsAdapter.insertAtBeginning(item);
                }
            }

            final Button createNewAssignmentOK = (Button) findViewById(R.id.createNewAssignmentOK);
            if (itemsAdapter.getCount() >= 1){
                createNewAssignmentOK.setEnabled(true);
            }

            ActivityLandingPage = new Intent(CreateNewAssignment.this, ActivityLandingPage.class);
            AssignmentLandingPageIntent = new Intent(CreateNewAssignment.this, AssignmentLandingPage.class);

            //Enable the addNewItem only after Users enters an item
            itemNameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    boolean buttonStatus = !(itemNameEditText.getText().toString().trim().isEmpty());
                    addNewItemButton.setEnabled(buttonStatus);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            inviteeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    InviteeList.Invitees invitees = (InviteeList.Invitees) parent.getItemAtPosition(position);
                    assigneeID = invitees.getEmail();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            itemUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedUnitString = (String) parent.getItemAtPosition(position);
                    selectedUnit = Unit.valueOf(selectedUnitString);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            addNewItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String itemDescription = itemNameEditText.getText().toString();
                    int itemCount = Integer.parseInt(itemCountEditText.getText().toString());

                    Item item = null;
                    try {
                        item = new Item(itemDescription, assigneeID, itemCount, selectedUnit);
                    } catch (IllegalDataOperation illegalDataOperation) {
                        illegalDataOperation.printStackTrace();
                    }
                    itemsAdapter.insertAtBeginning(item);
                    itemsAdapter.notifyDataSetChanged();
                    itemNameEditText.setText("");
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
                        Toast.makeText(CreateNewAssignment.this, "Add atleast one item to create an Assignment", LENGTH_LONG).show();
                        createNewAssignmentOK.setEnabled(false);
                        return;
                    }

                    serviceHandler.getAssignmentHandler().updateAssignment(new BaseFaroRequestCallback<String>() {
                        @Override
                        public void onFailure(Request request, IOException ex) {
                            Log.e(TAG, "failed to send new item list");
                        }

                        @Override
                        public void onResponse(String s, HttpError error) {
                            if (error == null ) {
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Successfully updated items list to server");
                                        cloneAssignment.setItems(itemsAdapter.list);
                                        assignmentListHandler.removeAssignmentFromListAndMap(cloneAssignment.getId());
                                        if (activityID != null){
                                            originalActivity.setAssignment(cloneAssignment);
                                        }else{
                                            originalEvent.setAssignment(cloneAssignment);
                                        }
                                        assignmentListHandler.addAssignmentToListAndMap(cloneAssignment);
                                        AssignmentLandingPageIntent.putExtra("eventID", eventID);
                                        AssignmentLandingPageIntent.putExtra("activityID", activityID);
                                        AssignmentLandingPageIntent.putExtra("assignmentID", assignmentID);
                                        startActivity(AssignmentLandingPageIntent);
                                        finish();
                                    }
                                };
                                Handler mainHandler = new Handler(mContext.getMainLooper());
                                mainHandler.post(myRunnable);
                            }else {
                                Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                            }

                        }
                    }, eventID, cloneAssignment.getId(), activityID, itemsAdapter.list);
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
        }
    }

}
