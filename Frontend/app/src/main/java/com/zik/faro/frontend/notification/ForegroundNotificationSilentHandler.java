package com.zik.faro.frontend.notification;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.Poll;
import com.zik.faro.frontend.ActivityListHandler;
import com.zik.faro.frontend.AssignmentListHandler;
import com.zik.faro.frontend.EventFriendListHandler;
import com.zik.faro.frontend.EventListHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.PollListHandler;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;

public class ForegroundNotificationSilentHandler {
    private String faroNotificationDataStr = null;
    private JSONObject faroNotificationDataJSON = null;
    private String payloadNotificationType = "default";
    private Long version;
    private String eventId = null;
    private String pollId = null;
    private String assignmentId = null;
    private String activityId = null;
    private String TAG = "FrgndNtfnSilentHndlr";
    private Context mContext = null;

    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private PollListHandler pollListHandler = PollListHandler.getInstance();
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    public void updateObjectInCacheIfPresent(Context context, JSONObject notificationDataJSON) throws JSONException {
        faroNotificationDataStr = notificationDataJSON.getString(FaroIntentConstants.FARO_NOTIFICATION_DATA);
        faroNotificationDataJSON = new JSONObject(faroNotificationDataStr);
        payloadNotificationType = faroNotificationDataJSON.getString(FaroIntentConstants.PAYLOAD_NOTIFICATION_TYPE);
        version = Long.parseLong(faroNotificationDataJSON.getString(FaroIntentConstants.VERSION));
        mContext = context;

        switch (payloadNotificationType){
            case "notificationType_EventInvite":
            case "notificationType_EventDateChanged":
            case "notificationType_EventTimeChanged":
            case "notificationType_EventLocationChanged":
            case "notificationType_EventDescriptionUpdate":
            case "notificationType_EventGeneric":
                updateEventObjectInCache();
                break;
            case "notificationType_PollCreated":
            case "notificationType_PollModified":
            case "notificationType_PollClosed":
            case "notificationType_PollGeneric":
                updatePollObjectInCache();
                break;
            case "notificationType_ActivityCreated":
            case "notificationType_ActivityDateChanged":
            case "notificationType_ActivityTimeChanged":
            case "notificationType_ActivityLocationChanged":
            case "notificationType_ActivityDescriptionChanged":
            case "notificationType_ActivityGeneric":
                updateActivityObjectInCache();
                break;
            case "notificationType_NewAssignmentForYou":
            case "notificationType_AssignmentCreated":
            case "notificationType_AssignmentPending":
                updateAssignmentObjectInCache();
                break;
            case "notificationType_FriendInviteAccepted":
            case "notificationType_FriendInviteMaybe":
            case "notificationType_FriendInviteNotGoing":
                updateEventFriendListInCache();
                break;
            case "notificationType_RemovedFromEvent":
            case "notificationType_EventDeleted":
            case "notificationType_ActivityDeleted":
            case "notificationType_PollDeleted":
            default:
                break;
        }
    }

    private void updateEventObjectInCache() throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);

        //Check if event object present in cache for this eventId.
        Event event = null;
        try {
            event = (Event) eventListHandler.getOriginalObject(eventId);

            // Check version passed in the notification against the version in the event object
            // If greater only then make an api call.
            if (version > event.getVersion()) {
                getEventFromServer();
            }
        } catch (FaroObjectNotFoundException e) {
            Log.i(TAG, MessageFormat.format("Event {0} not present in cache", eventId));
        }
    }

    private void updatePollObjectInCache() throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);
        pollId = faroNotificationDataJSON.getString(FaroIntentConstants.POLL_ID);

        //Check if poll object is present in cache.
        Poll poll = null;
        try {
            poll = (Poll) pollListHandler.getOriginalObject(pollId);

            // Check version passed in the notification against the version in the poll object
            // If greater only then make an api call.
            if (version > poll.getVersion()) {
                getUpdatedPollFromServer();
            }

        } catch (FaroObjectNotFoundException e) {
            //Poll has been deleted.
            Log.i(TAG, MessageFormat.format("Poll {0} not present in cache", pollId));
        }
    }

    private void updateActivityObjectInCache() throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);
        activityId = faroNotificationDataJSON.getString(FaroIntentConstants.ACTIVITY_ID);

        //Check if Activity object is present in cache.
        Activity activity = null;
        try {
            activity = (Activity) activityListHandler.getOriginalObject(activityId);

            // Check version passed in the notification against the version in the activity object
            // If greater only then make an api call.
            if (version > activity.getVersion()) {
                getUpdatedActivityFromServer();
            }

        } catch (FaroObjectNotFoundException e) {
            Log.i(TAG, MessageFormat.format("Activity {0} not present in cache", activityId));
        }
    }

    private void updateAssignmentObjectInCache() throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);
        activityId = faroNotificationDataJSON.getString(FaroIntentConstants.ACTIVITY_ID);
        assignmentId = faroNotificationDataJSON.getString(FaroIntentConstants.ASSIGNMENT_ID);

        Event event = null;
        Activity activity = null;
        Long cacheVersion;

        //Check if Assignment object is present in cache.
        Assignment assignment =  assignmentListHandler.getOriginalAssignmentFromMap(assignmentId);
        if (assignment == null)
            return;

        try {
            if (activityId.equals("")) {
                event = (Event) eventListHandler.getOriginalObject(eventId);
                cacheVersion = event.getVersion();
            } else {
                activity = (Activity) activityListHandler.getOriginalObject(activityId);
                cacheVersion = activity.getVersion();
            }

            // Check version passed in the notification against the version in the evnt/activity object
            // If greater only then make an api call.
            if (version > cacheVersion) {
                getUpdatedAssignmentFromServer();
            }

        } catch (FaroObjectNotFoundException e) {
            Log.i(TAG, MessageFormat.format("{0} {1} not present in cache",
                    activityId.equals("") ? "Event" : "Activity",
                    activityId.equals("") ? eventId : activityId));
        }
    }

    private void updateEventFriendListInCache() throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);

        //Check if event object present in cache for this eventId.
        Event event = null;
        try {
            event = (Event) eventListHandler.getOriginalObject(eventId);

            // check version passed in the notification against the version in the event object
            // If greater only then make an api call.
            if (version > event.getVersion()) {
                getEventInviteesFromServer();
            }

        } catch (FaroObjectNotFoundException e) {
            Log.i(TAG, MessageFormat.format("Event {0} not present in cache", eventId));
        }
    }

    private void getEventFromServer(){
        serviceHandler.getEventHandler().getEvent(new BaseFaroRequestCallback<EventInviteStatusWrapper>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get Event from server");
            }

            @Override
            public void onResponse(final EventInviteStatusWrapper eventInviteStatusWrapper, HttpError error) {
                if (error == null ) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Event from server");
                            Event event = eventInviteStatusWrapper.getEvent();
                            eventListHandler.addEventToListAndMap(event,
                                    eventInviteStatusWrapper.getInviteStatus());
                        }
                    });
                }else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId);
    }

    private void getUpdatedPollFromServer(){
        serviceHandler.getPollHandler().getPoll(new BaseFaroRequestCallback<Poll>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get the updated Poll Object");
            }

            @Override
            public void onResponse(final Poll receivedPoll, HttpError error) {
                if (error == null ) {
                    //Since update to server successful
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pollListHandler.addPollToListAndMap(eventId, receivedPoll, mContext);
                        }
                    });
                }
                else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }

            }
        }, eventId, pollId);
    }

    private void getUpdatedActivityFromServer() {
        serviceHandler.getActivityHandler().getActivity(new BaseFaroRequestCallback<Activity>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get activity list");
            }

            @Override
            public void onResponse(final Activity activity, HttpError error) {
                if (error == null) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received activities from the server!!");
                            activityListHandler.addActivityToListAndMap(eventId, activity, mContext);
                        }
                    });
                } else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId, activityId);
    }

    private void getUpdatedAssignmentFromServer() {
        serviceHandler.getAssignmentHandler().getAssignment(new BaseFaroRequestCallback<Assignment>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get Event from server");
            }

            @Override
            public void onResponse(final Assignment assignment, HttpError error) {
                if (error == null ) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Assignment from server");
                            assignmentListHandler.addAssignmentToListAndMap(eventId,
                                    assignment, activityId, mContext);
                        }
                    });
                }else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId, assignmentId, activityId);
    }

    public void getEventInviteesFromServer(){
        serviceHandler.getEventHandler().getEventInvitees(new BaseFaroRequestCallback<InviteeList>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get cloneEvent Invitees");
            }

            @Override
            public void onResponse(final InviteeList inviteeList, HttpError error) {
                if (error == null) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Invitee List for the cloneEvent");
                            eventFriendListHandler.addDownloadedFriendsToListAndMap(eventId, inviteeList, mContext);
                        }
                    });
                } else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId);
    }
}
