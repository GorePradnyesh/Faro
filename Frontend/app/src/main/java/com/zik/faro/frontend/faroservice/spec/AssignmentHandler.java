package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.data.Assignment;
import com.zik.faro.data.AssignmentCount;
import com.zik.faro.data.Item;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;
import java.util.Map;

public interface AssignmentHandler {
    void getAssignments(final BaseFaroRequestCallback<Map<String, Assignment>> callback, final String eventId);
    void getAssignment(final BaseFaroRequestCallback<Assignment> callback, final String eventId, final String assignmentId, final String activityId);
    void getPendingAssignmentCount(BaseFaroRequestCallback<AssignmentCount> callback, String eventId);
    void deleteAssignment(BaseFaroRequestCallback<String> callback, String eventId, String assignmentId, String activityId);
    void updateAssignment(final BaseFaroRequestCallback<Map<String, List<Item>>> callback, final String eventId, Map<String, List<Item>> items);
    void addAssignment(final BaseFaroRequestCallback<String> callback, final String eventId, final Assignment assignment, final String activityId);
}
