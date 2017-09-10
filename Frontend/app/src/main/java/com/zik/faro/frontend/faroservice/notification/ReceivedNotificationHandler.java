package com.zik.faro.frontend.faroservice.notification;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.zik.faro.frontend.ui.activities.ActivityLandingPage;
import com.zik.faro.frontend.ui.activities.AppLandingPage;
import com.zik.faro.frontend.ui.activities.AssignmentLandingPage;
import com.zik.faro.frontend.ui.activities.EventFriendListLandingPage;
import com.zik.faro.frontend.ui.activities.EventLandingPage;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.ui.activities.PollLandingPage;
import com.zik.faro.frontend.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ReceivedNotificationHandler extends AppCompatActivity {
    private String payloadNotificationType = "default";
    private static final String TAG = "RcvdNotficationHandler";
    private Intent intent = null;
    private String notificationType = null;
    private String notificationDataStr = null;
    private Bundle extras = null;
    private JSONObject notificationDataJSON = null;
    private String faroNotificationDataStr = null;
    private JSONObject faroNotificationDataJSON = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_notfication_handler);

        extras = getIntent().getExtras();
        if (extras == null){
            //TODO: How to handle this??
            return;
        }

        /*
         * There are 2 types of Bundles which can be received by ReceivedNotificationHandler
         * 1. When notification received in the foreground then bundle is constructed in
         *    FaroNotificationBuilder which sends "notificationType" as
         *    "FaroIntentConstants.FOREGROUND_NOTIFICATION" and the bundle contains a JSON object
         *    converted to string  with key as "FaroIntentConstants.NOTIFICATION_DATA"
         * 2. When notification received when app is shut, the OS Bundles the key-value pairs in
         *    data section in a Bundle.
         */
        notificationType = extras.getString(FaroIntentConstants.NOTIFICATION_TYPE);

        if (notificationType != null && notificationType.equals(FaroIntentConstants.FOREGROUND_NOTIFICATION)){
            Log.d(TAG, "Notification received in the foreground");
            notificationDataStr = extras.getString(FaroIntentConstants.NOTIFICATION_DATA);
            try {
                notificationDataJSON = new JSONObject(notificationDataStr);
                faroNotificationDataStr = notificationDataJSON.getString(FaroIntentConstants.FARO_NOTIFICATION_DATA);
                faroNotificationDataJSON = new JSONObject(faroNotificationDataStr);
                payloadNotificationType = faroNotificationDataJSON.getString(FaroIntentConstants.PAYLOAD_NOTIFICATION_TYPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            Log.d(TAG, "Notification received in the background");
            faroNotificationDataStr = extras.getString(FaroIntentConstants.FARO_NOTIFICATION_DATA);
            try {
                faroNotificationDataJSON = new JSONObject(faroNotificationDataStr);
                payloadNotificationType = faroNotificationDataJSON.getString(FaroIntentConstants.PAYLOAD_NOTIFICATION_TYPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (payloadNotificationType == null)return; // TODO: how to handle this condition?

        switch (payloadNotificationType){
            case "notificationType_EventInvite":
            case "notificationType_EventDateChanged":
            case "notificationType_EventTimeChanged":
            case "notificationType_EventLocationChanged":
            case "notificationType_EventDescriptionUpdate":
            case "notificationType_EventGeneric":
                intent = new Intent(this, EventLandingPage.class);
                break;
            case "notificationType_PollCreated":
            case "notificationType_PollModified":
            case "notificationType_PollClosed":
            case "notificationType_PollGeneric":
                intent = new Intent(this, PollLandingPage.class);
                break;
            case "notificationType_ActivityCreated":
            case "notificationType_ActivityDateChanged":
            case "notificationType_ActivityTimeChanged":
            case "notificationType_ActivityLocationChanged":
            case "notificationType_ActivityDescriptionChanged":
            case "notificationType_ActivityGeneric":
                intent = new Intent(this, ActivityLandingPage.class);
                break;
            case "notificationType_NewAssignmentForYou":
            case "notificationType_AssignmentCreated":
            case "notificationType_AssignmentPending":
                intent = new Intent(this, AssignmentLandingPage.class);
                break;
            case "notificationType_FriendInviteAccepted":
            case "notificationType_FriendInviteMaybe":
            case "notificationType_FriendInviteNotGoing":
                intent = new Intent(this, EventFriendListLandingPage.class);
                break;
            case "notificationType_RemovedFromEvent":
            case "notificationType_EventDeleted":
            case "notificationType_ActivityDeleted":
            case "notificationType_PollDeleted":
            default:
                // TODO: How to handle this incase when user is on some landing page.
                intent = new Intent(this, AppLandingPage.class);
                break;
        }

        for(Iterator iterator = faroNotificationDataJSON.keys(); iterator.hasNext();) {
            String key = (String) iterator.next();
            try {
                intent.putExtra(key, (String) faroNotificationDataJSON.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        intent.putExtra(FaroIntentConstants.BUNDLE_TYPE, FaroIntentConstants.IS_NOTIFICATION);
        startActivity(intent);
        finish();
    }
}
