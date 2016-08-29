package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.okhttp.Request;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.Item;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.List;

public class AssignmentLandingPage extends Fragment{

    private static Event originalEvent;
    private static com.zik.faro.data.Activity originalActivity = null;
    String activityID = null;
    String eventID = null;
    String assignmentID = null;
    private static Assignment cloneAssignment = null;

    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;

    private static String TAG = "AssignmentLandingPage";

    ListView itemList;
    Intent EventLandingPage;
    Intent ActivityLandingPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_assignment_landing_page, container, false);
        super.onCreate(savedInstanceState);

        eventID = getArguments().getString("eventID");
        activityID = getArguments().getString("activityID");
        assignmentID = getArguments().getString("assignmentID");

        final Context mContext = this.getActivity();

        cloneAssignment = assignmentListHandler.getAssignmentCloneFromMap(assignmentID);

        originalEvent = eventListHandler.getOriginalEventFromMap(eventID);
        final TextView assignmentDescription = (TextView) view.findViewById(R.id.assignmentDescription);
        if (activityID != null) {
            originalActivity = activityListHandler.getOriginalActivityFromMap(activityID);
            assignmentDescription.setText("Assignment for " + originalActivity.getName());
        } else {
            assignmentDescription.setText("Assignment for " + originalEvent.getEventName());
        }

        ImageButton editButton = (ImageButton) view.findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        Button updateAssignment = (Button) view.findViewById(R.id.updateAssignment);

        EventLandingPage = new Intent(getActivity(), EventLandingPage.class);
        ActivityLandingPage = new Intent(getActivity(), ActivityLandingPage.class);
        final Intent EditAssignmentPage = new Intent(getActivity(), EditAssignment.class);
        final Intent AssignmentLandingPageReloadIntent = new Intent(getActivity(), AssignmentLandingPageTabs.class);


        itemList = (ListView) view.findViewById(R.id.itemList);
        itemList.setTag("AssignmentLandingPageTabsIntent");
        final ItemsAdapter itemsAdapter = new ItemsAdapter(getActivity(), R.layout.assignment_update_item_row_style);
        itemList.setAdapter(itemsAdapter);

        List<Item> items = cloneAssignment.getItems();
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
                                    //Reload AssignmentLandingPage
                                    AssignmentLandingPageReloadIntent.putExtra("eventID", eventID);
                                    AssignmentLandingPageReloadIntent.putExtra("activityID", activityID);
                                    AssignmentLandingPageReloadIntent.putExtra("assignmentID", assignmentID);
                                    getActivity().finish();
                                    startActivity(AssignmentLandingPageReloadIntent);
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

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditAssignmentPage.putExtra("eventID", eventID);
                EditAssignmentPage.putExtra("activityID", activityID);
                EditAssignmentPage.putExtra("assignmentID", assignmentID);
                startActivity(EditAssignmentPage);
                getActivity().finish();
            }
        });
        return view;
    }
}
