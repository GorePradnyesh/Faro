package com.zik.faro.frontend;

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
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.Item;
import com.zik.faro.data.Unit;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;

/*
 * We can arrive on this page from 2 different pages.
 */
public class EditAssignment extends android.app.Activity {

    private static Event originalEvent;
    private static Activity originalActivity = null;
    private static Assignment cloneAssignment;

    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

    private String eventID = null;
    private String activityID = null;
    private String assigneeID = null;
    private String assignmentID = null;
    private Unit selectedUnit;

    private HashSet<Item> newItemSet = new HashSet<>();
    private HashSet<Item> originalItemSet = new HashSet<>();

    Intent AssignmentLandingPageTabsIntent;

    ArrayList <String> itemUnitArray = new ArrayList<>();

    private static String TAG = "EditAssignment";

    private int editItemPosition = -1;
    private boolean addEditedItem = false;

    private final Context mContext = this;

    private RelativeLayout popUpRelativeLayout = null;

    private Map<String, List<Item>> itemListMap = new HashMap<String, List<Item>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_assignment);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.editAssignmentRelativeLayout);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

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

            final Spinner inviteeSpinner = (Spinner) findViewById(R.id.inviteeSpinner);
            final Spinner itemUnitSpinner = (Spinner) findViewById(R.id.itemUnitSpinner);

            inviteeSpinner.setTag("EditAssignment");
            inviteeSpinner.setAdapter(eventFriendListHandler.getAcceptedFriendAdapter(eventID, mContext));


            for (Unit unit : Unit.values()){
                itemUnitArray.add(unit.toString());
            }
            ArrayAdapter<String> itemUnitListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemUnitArray);
            itemUnitListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            itemUnitSpinner.setAdapter(itemUnitListAdapter);

            final ImageButton addNewItemButton = (ImageButton)findViewById(R.id.addNewItemButton);
            addNewItemButton.setImageResource(R.drawable.plus);
            addNewItemButton.setEnabled(false);

            final ListView itemList = (ListView)findViewById(R.id.itemList);
            itemList.setTag("EditAssignment");
            final ItemsAdapter itemsAdapter = new ItemsAdapter(this, R.layout.item_cant_edit_row_style);
            itemList.setAdapter(itemsAdapter);


            itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Item item = (Item) parent.getItemAtPosition(position);
                    if (item.getStatus().equals(ActionStatus.INCOMPLETE)){
                        itemNameEditText.setText(item.getName());
                        itemCountEditText.setText(Integer.toString(item.getCount()));
                        inviteeSpinner.setSelection(getAssigneePositionInList(item.getAssigneeId()));
                        itemUnitSpinner.setSelection(getUnitPositionInList(item.getUnit()));
                        itemsAdapter.removeFromPosition(position);
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

            final Button createNewAssignmentOK = (Button) findViewById(R.id.createNewAssignmentOK);
            if (itemsAdapter.getCount() >= 1){
                createNewAssignmentOK.setEnabled(true);
            }

            AssignmentLandingPageTabsIntent = new Intent(EditAssignment.this, AssignmentLandingPage.class);

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
                    int itemCount = 0;
                    try {
                        itemCount = Integer.parseInt(itemCountEditText.getText().toString());
                    } catch (NumberFormatException e){
                        itemCountEditText.setText("0");
                        errorMsgPopUp("Number passed is too big");
                        return;
                    }


                    Item item = null;
                    try {
                        item = new Item(itemDescription, assigneeID, itemCount, selectedUnit);
                    } catch (IllegalDataOperation illegalDataOperation) {
                        illegalDataOperation.printStackTrace();
                    }

                    if (addEditedItem){
                        addEditedItem = false;
                        itemsAdapter.insertAtPosition(item, editItemPosition);
                        editItemPosition = -1;
                    }else {
                        itemsAdapter.insertAtBeginning(item);
                    }
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

                    if (itemsAdapter.getCount() == 0 && originalItemSet.size() == 0){
                        Toast.makeText(EditAssignment.this, "Add atleast one item to create an Assignment", LENGTH_LONG).show();
                        createNewAssignmentOK.setEnabled(false);
                        return;
                    }

                    final List<Item> updatedItemList = new LinkedList<>();
                    if (itemsAdapter.getCount() == 0){
                        Toast.makeText(EditAssignment.this, "Deleting all the previous assignments", LENGTH_LONG).show();
                    }else {
                        for (int i = 0; i < itemsAdapter.list.size(); i++) {
                            updatedItemList.add(itemsAdapter.list.get(i));
                            newItemSet.add(itemsAdapter.list.get(i));
                        }

                        if (originalItemSet.equals(newItemSet)){
                            Toast.makeText(EditAssignment.this, "No change made to assignment List", LENGTH_LONG).show();
                            newItemSet.clear();
                            return;
                        }
                    }

                    if (activityID != null) {
                        itemListMap.put(activityID, updatedItemList);
                    }else {
                        itemListMap.put(eventID, updatedItemList);
                    }

                    updateAssignmentToServer();
                }
            });
        }
    }

    public void updateAssignmentToServer () {
        serviceHandler.getAssignmentHandler().updateAssignment(new BaseFaroRequestCallback<Map<String, List<Item>>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to send new item list");
            }

            @Override
            public void onResponse(final Map<String, List<Item>> stringListMap, HttpError error) {
                if (error == null ) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully updated items list to server");
                            List<Item> receivedItemList;
                            if (activityID != null) {
                                receivedItemList = stringListMap.get(activityID);
                            }else {
                                receivedItemList = stringListMap.get(eventID);
                            }
                            cloneAssignment.setItems(receivedItemList);
                            assignmentListHandler.removeAssignmentFromListAndMap(eventID, cloneAssignment.getId(), mContext);
                            if (activityID != null){
                                originalActivity.setAssignment(cloneAssignment);
                            }else{
                                originalEvent.setAssignment(cloneAssignment);
                            }
                            assignmentListHandler.addAssignmentToListAndMap(eventID, cloneAssignment, activityID, mContext);
                            AssignmentLandingPageTabsIntent.putExtra("eventID", eventID);
                            AssignmentLandingPageTabsIntent.putExtra("activityID", activityID);
                            AssignmentLandingPageTabsIntent.putExtra("assignmentID", assignmentID);
                            startActivity(AssignmentLandingPageTabsIntent);
                            finish();
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }

            }
        }, eventID, itemListMap);
    }

    public int getAssigneePositionInList(String assigneeID){
        int i = 0;
        for (i = 0; i < eventFriendListHandler.getAcceptedFriendAdapter(eventID, mContext).list.size(); i++){
            InviteeList.Invitees invitees = eventFriendListHandler.getAcceptedFriendAdapter(eventID, mContext).list.get(i);
            if (invitees.getEmail().equals(assigneeID)){
                break;
            }
        }
        return i;
    }

    public int getUnitPositionInList(Unit unit){
        int i = 0;
        for (i = 0; i < itemUnitArray.size(); i++){
            String unitString = itemUnitArray.get(i);
            if (unitString.equals(unit.toString())){
                break;
            }
        }
        return i;
    }


    private void errorMsgPopUp(String errorMsg){
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.error_message_popup, null);
        final PopupWindow popupWindow = new PopupWindow(container,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        TextView errorMsgTextView = (TextView) container.findViewById(R.id.errorMsg);
        errorMsgTextView.setText(errorMsg);


        popupWindow.showAtLocation(popUpRelativeLayout, Gravity.CENTER, 0, 0);

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
        AssignmentLandingPageTabsIntent.putExtra("eventID", eventID);
        AssignmentLandingPageTabsIntent.putExtra("activityID", activityID);
        AssignmentLandingPageTabsIntent.putExtra("assignmentID", assignmentID);
        startActivity(AssignmentLandingPageTabsIntent);
        finish();
    }

}
