package com.zik.faro.frontend.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.Item;
import com.zik.faro.data.Unit;
import com.zik.faro.data.UpdateCollectionRequest;
import com.zik.faro.frontend.handlers.ActivityListHandler;
import com.zik.faro.frontend.handlers.AssignmentListHandler;
import com.zik.faro.frontend.handlers.EventFriendListHandler;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.util.FaroExceptionHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.ui.adapters.ItemsAdapter;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

/*
 * We can arrive on this page from 2 different pages.
 */
public class EditAssignmentActivity extends android.app.Activity {
    private Event originalEvent;
    private Activity originalActivity = null;
    private Event cloneEvent = null;
    private Activity cloneActivity = null;
    private Assignment cloneAssignment;

    private EventListHandler eventListHandler = EventListHandler.getInstance(this);
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

    private String eventId = null;
    private String activityId = null;
    private String assigneeId = null;
    private String assignmentId = null;
    private Unit selectedUnit;

    private HashSet<Item> newItemSet = new HashSet<>();
    private HashSet<Item> originalItemSet = new HashSet<>();

    private Intent assignmentLandingPageTabsIntent;

    private ArrayList <String> itemUnitArray = new ArrayList<>();

    private static String TAG = "EditAssignmentActivity";

    private int editItemPosition = -1;
    private boolean addEditedItem = false;

    private Context mContext;

    private LinearLayout linlaHeaderProgress = null;
    private RelativeLayout editAssignmentRelativeLayout = null;

    private List<Item> eventAssignmentItems = new ArrayList<>();
    private List<Item> activityAssignmentItems = new ArrayList<>();
    private Bundle extras = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_assignment);

        mContext = this;

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        editAssignmentRelativeLayout = (RelativeLayout) findViewById(R.id.editAssignmentRelativeLayout);
        editAssignmentRelativeLayout.setVisibility(View.GONE);

        extras = getIntent().getExtras();
        if (extras == null) {
            return; //TODO: How to handle such conditions
        }

        try {
            setupPageDetails();
        } catch (FaroObjectNotFoundException e) {
            //Activity has been deleted.
            Toast.makeText(this, activityId == null ? "Event" : "Activity" + " has been deleted", LENGTH_LONG).show();
            Log.e(TAG, MessageFormat.format("{0} {1} has been deleted",
                    activityId == null ? "Event" : "Activity",
                    activityId == null ? eventId : activityId));
            finish();
        }
    }

    private void setupPageDetails () throws FaroObjectNotFoundException {
        linlaHeaderProgress.setVisibility(View.GONE);
        editAssignmentRelativeLayout.setVisibility(View.VISIBLE);

        eventId = extras.getString(FaroIntentConstants.EVENT_ID);
        activityId = extras.getString(FaroIntentConstants.ACTIVITY_ID);
        assignmentId = extras.getString(FaroIntentConstants.ASSIGNMENT_ID);

        originalEvent = eventListHandler.getOriginalObject(eventId);
        cloneEvent = eventListHandler.getCloneObject(eventId);

        if (activityId != null) {
            originalActivity = activityListHandler.getOriginalObject(activityId);
            cloneActivity = activityListHandler.getCloneObject(activityId);
        }

        cloneAssignment = assignmentListHandler.getAssignmentCloneFromMap(assignmentId);

        TextView assignmentDescription = (TextView)findViewById(R.id.assignmentDescription);
        if (activityId != null) {
            assignmentDescription.setText("Assignment for " + originalActivity.getName());
        } else {
            assignmentDescription.setText("Assignment for " + originalEvent.getEventName());
        }

        final EditText itemNameEditText = (EditText)findViewById(R.id.itemNameEditText);
        final EditText itemCountEditText = (EditText)findViewById(R.id.itemCount);
        final TextView itemIdTextView = (TextView) findViewById(R.id.itemId);

        itemIdTextView.setVisibility(View.GONE);
        itemCountEditText.setText("0");

        final Spinner inviteeSpinner = (Spinner) findViewById(R.id.inviteeSpinner);
        final Spinner itemUnitSpinner = (Spinner) findViewById(R.id.itemUnitSpinner);

        inviteeSpinner.setTag("EditAssignmentActivity");
        inviteeSpinner.setAdapter(eventFriendListHandler.getAcceptedFriendAdapter(eventId, mContext));


        for (Unit unit : Unit.values()) {
            itemUnitArray.add(unit.toString());
        }
        ArrayAdapter<String> itemUnitListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemUnitArray);
        itemUnitListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemUnitSpinner.setAdapter(itemUnitListAdapter);

        final ImageButton addNewItemButton = (ImageButton)findViewById(R.id.addNewItemButton);
        addNewItemButton.setImageResource(R.drawable.plus);
        addNewItemButton.setEnabled(false);

        final ListView itemList = (ListView)findViewById(R.id.itemList);
        itemList.setTag("EditAssignmentActivity");
        final ItemsAdapter itemsAdapter = new ItemsAdapter(this, R.layout.item_cant_edit_row_style);
        itemList.setAdapter(itemsAdapter);


        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = (Item) parent.getItemAtPosition(position);
                if (item.getStatus().equals(ActionStatus.INCOMPLETE)) {
                    itemNameEditText.setText(item.getName());
                    itemCountEditText.setText(Integer.toString(item.getCount()));
                    inviteeSpinner.setSelection(getAssigneePositionInList(item.getAssigneeId()));
                    itemUnitSpinner.setSelection(getUnitPositionInList(item.getUnit()));
                    itemIdTextView.setText(item.getId());
                    //itemsAdapter.removeFromPosition(position);
                    //TODO: Can change color of the element which is being changed
                    itemsAdapter.notifyDataSetChanged();
                    editItemPosition = position;
                    addEditedItem = true;
                }
            }
        });

        for (Integer i = cloneAssignment.getItems().size() - 1; i >= 0; i--) {
            Item item = cloneAssignment.getItems().get(i);
            if (item.getStatus() == ActionStatus.COMPLETE) {
                itemsAdapter.insertAtEnd(item);
            } else {
                itemsAdapter.insertAtBeginning(item);
            }
            originalItemSet.add(item);
        }

        final Button createNewAssignmentOkButton = (Button) findViewById(R.id.createNewAssignmentOK);
        if (itemsAdapter.getCount() >= 1) {
            createNewAssignmentOkButton.setEnabled(true);
        }

        assignmentLandingPageTabsIntent = new Intent(EditAssignmentActivity.this, AssignmentLandingPage.class);

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
                assigneeId = invitees.getEmail();
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
                int itemCount;
                try {
                    itemCount = Integer.parseInt(itemCountEditText.getText().toString());
                } catch (NumberFormatException e){
                    itemCountEditText.setText("0");
                    errorMsgPopUp("Number passed is too big");
                    return;
                }

                Item item;

                if (addEditedItem) {
                    addEditedItem = false;
                    itemsAdapter.removeFromPosition(editItemPosition);

                    String itemId = itemIdTextView.getText().toString();
                    item = new Item(itemDescription, assigneeId, itemCount, selectedUnit, itemId);

                    itemsAdapter.insertAtPosition(item, editItemPosition);
                    editItemPosition = -1;
                } else {
                    item = new Item(itemDescription, assigneeId, itemCount, selectedUnit);

                    itemsAdapter.insertAtBeginning(item);
                }

                itemsAdapter.notifyDataSetChanged();
                itemNameEditText.setText("");
                itemCountEditText.setText("0");

                if (itemsAdapter.getCount() == 1) {
                    createNewAssignmentOkButton.setEnabled(true);
                }
            }
        });


        createNewAssignmentOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (itemsAdapter.getCount() == 0 && originalItemSet.size() == 0){
                    Toast.makeText(EditAssignmentActivity.this, "Add atleast one item to create an Assignment", LENGTH_LONG).show();
                    createNewAssignmentOkButton.setEnabled(false);
                    return;
                }

                final List<Item> updatedItemList = new LinkedList<>();
                if (itemsAdapter.getCount() == 0){
                    Toast.makeText(EditAssignmentActivity.this, "Deleting all the previous assignments", LENGTH_LONG).show();
                }else {
                    for (int i = 0; i < itemsAdapter.list.size(); i++) {
                        updatedItemList.add(itemsAdapter.list.get(i));
                        newItemSet.add(itemsAdapter.list.get(i));
                    }

                    if (originalItemSet.equals(newItemSet)){
                        Toast.makeText(EditAssignmentActivity.this, "No change made to assignment List", LENGTH_LONG).show();
                        newItemSet.clear();
                        return;
                    }
                }

                if (activityId != null) {
                    activityAssignmentItems.addAll(updatedItemList);
                    //itemListMap.put(activityId, updatedItemList);
                } else {
                    eventAssignmentItems.addAll(updatedItemList);
                    //itemListMap.put(eventId, updatedItemList);
                }

                updateAssignmentToServer();
            }
        });
    }

    public void updateAssignmentToServer() {
        if (activityId != null) {
            UpdateCollectionRequest<Activity, Item> activityUpdateCollectionRequest = new UpdateCollectionRequest<>();
            activityUpdateCollectionRequest.setToBeAdded(activityAssignmentItems);
            activityUpdateCollectionRequest.setUpdate(cloneActivity);

            serviceHandler.getActivityHandler().updateActivityAssignment(new BaseFaroRequestCallback<Activity>() {
                @Override
                public void onFailure(Request request, IOException ex) {
                    Log.e(TAG, MessageFormat.format("Failed to send new item list for activity {0}", activityId));
                }

                @Override
                public void onResponse(final Activity activity, HttpError error) {
                    if (error == null) {
                        Handler mainHandler = new Handler(mContext.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "Successfully updated items list to server");
                                cloneAssignment.setItems(activity.getAssignment().getItems());
                                assignmentListHandler.removeAssignmentFromListAndMap(eventId, cloneAssignment.getId(), mContext);

                                originalActivity.setAssignment(cloneAssignment);

                                assignmentListHandler.addAssignmentToListAndMap(eventId, cloneAssignment, activityId, mContext);
                                FaroIntentInfoBuilder.assignmentIntent(assignmentLandingPageTabsIntent,
                                        eventId, activityId, assignmentId);
                                startActivity(assignmentLandingPageTabsIntent);
                                finish();
                            }
                        });
                    } else {
                        Log.e(TAG, MessageFormat.format("code = {0} , message =  {1}", error.getCode(), error.getMessage()));
                    }
                }
            }, eventId, activityId, activityUpdateCollectionRequest);

        } else {
            UpdateCollectionRequest<Event, Item> eventUpdateCollectionRequest = new UpdateCollectionRequest<>();
            eventUpdateCollectionRequest.setToBeAdded(eventAssignmentItems);
            eventUpdateCollectionRequest.setUpdate(cloneEvent);

            serviceHandler.getAssignmentHandler().updateAssignment(new BaseFaroRequestCallback<Event>() {
                @Override
                public void onFailure(Request request, IOException ex) {
                    Log.e(TAG, "failed to send new item list");
                }

                @Override
                public void onResponse(final Event event, HttpError error) {
                    if (error == null) {
                        Handler mainHandler = new Handler(mContext.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "Successfully updated items list to server");
                                cloneAssignment.setItems(event.getAssignment().getItems());
                                assignmentListHandler.removeAssignmentFromListAndMap(eventId, cloneAssignment.getId(), mContext);

                                originalEvent.setAssignment(cloneAssignment);

                                assignmentListHandler.addAssignmentToListAndMap(eventId, cloneAssignment, activityId, mContext);
                                FaroIntentInfoBuilder.assignmentIntent(assignmentLandingPageTabsIntent,
                                        eventId, activityId, assignmentId);
                                startActivity(assignmentLandingPageTabsIntent);
                                finish();
                            }
                        });
                    } else {
                        Log.e(TAG, MessageFormat.format("code = {0} , message =  {1}", error.getCode(), error.getMessage()));
                    }
                }
            }, eventId, eventUpdateCollectionRequest);
        }
    }

    public int getAssigneePositionInList(String assigneeId) {
        int i;
        for (i = 0; i < eventFriendListHandler.getAcceptedFriendAdapter(eventId, mContext).list.size(); i++){
            InviteeList.Invitees invitees = eventFriendListHandler.getAcceptedFriendAdapter(eventId, mContext).list.get(i);
            if (invitees.getEmail().equals(assigneeId)){
                break;
            }
        }
        return i;
    }

    public int getUnitPositionInList(Unit unit) {
        int i;
        for (i = 0; i < itemUnitArray.size(); i++) {
            String unitString = itemUnitArray.get(i);
            if (unitString.equals(unit.toString())){
                break;
            }
        }
        return i;
    }


    private void errorMsgPopUp(String errorMsg) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.error_message_popup, null);
        final PopupWindow popupWindow = new PopupWindow(container,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        TextView errorMsgTextView = (TextView) container.findViewById(R.id.errorMsg);
        errorMsgTextView.setText(errorMsg);


        popupWindow.showAtLocation(editAssignmentRelativeLayout, Gravity.CENTER, 0, 0);

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FaroIntentInfoBuilder.assignmentIntent(assignmentLandingPageTabsIntent, eventId,
                activityId, assignmentId);
        startActivity(assignmentLandingPageTabsIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if the version is same. It can be different if this page is loaded and a notification
        // is received for this later which updates the cache but clonedata on this page remains
        // stale.
        try {
            if (activityId == null) {
                if (!eventListHandler.checkObjectVersionIfLatest(eventId, cloneEvent.getVersion())) {
                    setupPageDetails();
                }
            } else {
                if (!activityListHandler.checkObjectVersionIfLatest(activityId, cloneActivity.getVersion())) {
                    setupPageDetails();
                }
            }
        } catch (FaroObjectNotFoundException e) {
            //Activity has been deleted.
            Toast.makeText(this, activityId == null ? "Event" : "Activity" + " has been deleted", LENGTH_LONG).show();
            Log.e(TAG, MessageFormat.format("{0} {1} has been deleted",
                    activityId == null ? "Event" : "Activity",
                    activityId == null ? eventId : activityId));
            finish();
        }
    }
}
