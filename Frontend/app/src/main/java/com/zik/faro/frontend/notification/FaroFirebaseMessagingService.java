package com.zik.faro.frontend.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zik.faro.frontend.AppLandingPage;
import com.zik.faro.frontend.R;


import org.json.JSONObject;

public class FaroFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = null;
        String body = null;
        String notificationType = null;
        JSONObject data = null;


        if (remoteMessage.getData().size() > 0){
            data = new JSONObject(remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null){
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            notificationType = remoteMessage.getNotification().getClickAction();
            Log.d(TAG, "title is " + title);
            Log.d(TAG, "body is" + body);
            Log.d(TAG, "notification type is " + notificationType);
        }
        sendNotification(title, body, notificationType, data);
    }

    private void sendNotification(String title, String messageBody, String notificationType, JSONObject data) {
        NotificationCompat.Builder notificationBuilder =
                FaroNotificationBuilder.getNotficationBuilder(this, title,
                        messageBody, notificationType, data);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
