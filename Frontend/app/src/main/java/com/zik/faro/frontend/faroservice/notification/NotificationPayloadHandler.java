package com.zik.faro.frontend.faroservice.notification;

import com.zik.faro.frontend.util.FaroObjectNotFoundException;

// Implement this interface on all Activities which would be opened directly through a notification
public interface NotificationPayloadHandler {
    void checkAndHandleNotification() throws FaroObjectNotFoundException;
}
