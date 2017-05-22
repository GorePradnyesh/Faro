package com.zik.faro.frontend.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

public interface NotificationPayloadHandler {
    void checkAndHandleNotification(Bundle extras);
}
