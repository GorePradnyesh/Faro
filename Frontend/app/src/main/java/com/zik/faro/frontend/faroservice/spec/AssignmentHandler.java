package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.frontend.data.Assignment;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.Map;

public interface AssignmentHandler {
    void getAssignments(final BaseFaroRequestCallback<Map<String, Assignment>> callback, final String eventId);
    void getAssignment(final BaseFaroRequestCallback<Assignment> callback, final String eventId, final String assignmentId, final String activityId);
    void getPendingAssignmentCount(final BaseFaroRequestCallback<Integer> callback, final String eventId);
    void deleteAssignment(final BaseFaroRequestCallback<String> callback, final String eventId, final String assignmentId);
    void updateAssignment(final BaseFaroRequestCallback<Void> callback, final String eventId, final String activityId);
}
