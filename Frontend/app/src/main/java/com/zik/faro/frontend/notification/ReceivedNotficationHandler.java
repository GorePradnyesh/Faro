package com.zik.faro.frontend.notification;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.zik.faro.frontend.ActivityLandingPage;
import com.zik.faro.frontend.AppLandingPage;
import com.zik.faro.frontend.AssignmentLandingPage;
import com.zik.faro.frontend.ClosedPollLandingPage;
import com.zik.faro.frontend.EventFriendListLandingPage;
import com.zik.faro.frontend.EventLandingPage;
import com.zik.faro.frontend.OpenPollLandingPage;
import com.zik.faro.frontend.PollLandingPage;
import com.zik.faro.frontend.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ReceivedNotficationHandler extends AppCompatActivity {
    private String type = "default";
    private static final String TAG = "RcvdNotficationHandler";
    private Intent intent = null;
    private String bundleType = null;
    private String dataStr = null;
    private Bundle extras = null;
    private JSONObject data = null;

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
         * There are 2 types of Bundles which can be received by ReceivedNotficationHandler
         * 1. When notification received in the foreground then bundle is constructed in
         *    FaroNotificationBuilder which sends "bundleType" as "foregroundNotification" and the
         *    bundle contains a JSON object converted to string  with key as "data"
         * 2. When notification received when app is shut, the OS Bundles the key-value pairs in
         *    data section in a Bundle.
         */
        bundleType = extras.getString("bundleType");

        if (bundleType != null && bundleType.equals("foregroundNotification")){
            Log.d(TAG, "Notification received in the foreground");
            dataStr = extras.getString("dataStr");
            try {
                data = new JSONObject(dataStr);
                type = data.getString("type");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            Log.d(TAG, "Notification received in the background");
            type = extras.getString("type");
        }

        if (type == null)return; // TODO: how to handle this condition?

        switch (type){
            case "EventInvite":
            case "EventDateChanged":
            case "EventTimeChanged":
            case "EventLocationChanged":
            case "EventDescriptionUpdate":
                intent = new Intent(this, EventLandingPage.class);
                break;
            case "NewPollCreated":
            case "PollOptionAdded":
            case "PollWinnerDecided":
                intent = new Intent(this, PollLandingPage.class);
                break;
            case "NewActivityCreated":
            case "ActivityDateChanged":
            case "ActivityTimeChanged":
            case "ActivityLocationChanged":
            case "ActivityDescriptionChanged":
                intent = new Intent(this, ActivityLandingPage.class);
                break;
            case "NewAssignmentForYou":
                intent = new Intent(this, AssignmentLandingPage.class);
                break;
            case "FriendInviteAccepted":
            case "FriendInviteMaybe":
            case "FriendInviteNotGoing":
                intent = new Intent(this, EventFriendListLandingPage.class);
                break;
            default:
                intent = new Intent(this, AppLandingPage.class);
                break;
        }

        if (bundleType != null && bundleType.equals("foregroundNotification")){
            for(Iterator iterator = data.keys(); iterator.hasNext();) {
                String key = (String) iterator.next();

                try {
                    intent.putExtra(key, (String)data.get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }else {
            for (String key : extras.keySet()){
                intent.putExtra(key, extras.getString(key));
            }

        }
        intent.putExtra("bundleType", "notification");
        startActivity(intent);
        finish();
    }
}
