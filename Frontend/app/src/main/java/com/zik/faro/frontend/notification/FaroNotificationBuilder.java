package com.zik.faro.frontend.notification;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;

import org.json.JSONException;
import org.json.JSONObject;

public class FaroNotificationBuilder {
    private static final String TAG = "FaroNotificationBuilder";


    public static NotificationCompat.Builder getNotificationBuilder(Context context, String title,
                                                                    String  messageBody,
                                                                    String notificationType,
                                                                    JSONObject data){

        Intent intent = null;
        PendingIntent pendingIntent = null;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

        switch (notificationType){
            case "DEFAULTACTION":
                intent = new Intent(context, ReceivedNotificationHandler.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                FaroIntentInfoBuilder.notificationHandlerIntent(intent,
                        FaroIntentConstants.FOREGROUND_NOTIFICATION, data.toString());
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
                Log.d(TAG, "In RECEIVEDNOTIFICATIONHANDLER case when building builder ====");
                break;
            default:
                intent = new Intent(context, ReceivedNotificationHandler.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                FaroIntentInfoBuilder.notificationHandlerIntent(intent,
                        FaroIntentConstants.FOREGROUND_NOTIFICATION, data.toString());
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
                Log.d(TAG, "In default case when building builder ====");
                break;
        }

        ForegroundNotificationSilentHandler foregroundNotificationSilentHandler =
                new ForegroundNotificationSilentHandler();
        try {
            foregroundNotificationSilentHandler.updateGlobalMemoryIfPresent(context, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                notificationBuilder.setSmallIcon(R.drawable.blue)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        return notificationBuilder;
    }
}
