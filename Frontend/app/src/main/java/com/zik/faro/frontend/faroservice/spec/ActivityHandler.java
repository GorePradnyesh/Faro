package com.zik.faro.frontend.faroservice.spec;


import com.zik.faro.frontend.data.Activity;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;

public interface ActivityHandler {
    void getActivities(final BaseFaroRequestCallback<List<Activity>> callback, final String eventId);
    void getActivity(final BaseFaroRequestCallback<Activity> callback, final String eventId, final String activityId);
    void createActivity(final BaseFaroRequestCallback<Activity> callback, final String eventId, final Activity activity);
    void updateActivity(final BaseFaroRequestCallback<String> callback, 
                        final String eventId, 
                        final String activityId, 
                        final Activity.ActivityUpdateData updateData);
    void deleteActivity(final BaseFaroRequestCallback<String> callback, final String eventId, final String activityId);
    
}
