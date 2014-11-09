package com.zik.faro.commons;

/**
 * Created by pgore on 11/4/14.
 */
public final class Constants {

    public static final String HTTP_OK                                          = "OK";

    //----- Query String parameters ----- //
    public static final String SIGNATURE_QUERY_PARAM                            = "Signature";
    public static final String FARO_USER_ID_PARAM                               = "userId";


    //----- Path Parameters ----//
    public static final String EVENT_ID_PATH_PARAM                              = "eventId";
    public static final String EVENT_ID_PATH_PARAM_STRING                       = "/{" + EVENT_ID_PATH_PARAM +"}";

    public static final String ACTIVITY_ID_PATH_PARAM                           = "activityId";
    public static final String ACTIVITY_ID_PATH_PARAM_STRING                    = "/{" + ACTIVITY_ID_PATH_PARAM +"}";

    public static final String ASSIGNMENT_ID_PATH_PARAM                           = "assignmentId";
    public static final String ASSIGNMENT_ID_PATH_PARAM_STRING                    = "/{" + ASSIGNMENT_ID_PATH_PARAM +"}";


    //---- Path Constants ---- //
    public static final String EXPENSE_ID_PATH_CONST                            = "/expenseIds";
    public static final String EXPENSE_PATH_CONST                               = "/expense";
    public static final String CREATE_EXPENSE_GROUP_PATH_CONST                  = "/createExpenseGroup";

    public static final String FRIENDS_PATH_CONST                               = "/friends";
    public static final String INVITE_PATH_CONST                                = "/invite";
    public static final String REMOVE_PATH_CONST                                = "/remove";

    public static final String PROFILE_PATH_CONST                               = "/profile";

    public static final String FEEDBACK_PATH_CONST                              = "/feedback";

    public static final String EVENT_PATH_CONST                                 = "/event";
    public static final String EVENT_DETAILS_PATH_CONST                         = "/details";
    public static final String EVENT_INVITEES_PATH_CONST                        = "/invitees";
    public static final String EVENT_DISABLE_CONTROL_PATH_CONST                 = "/disableControl";
    public static final String EVENT_REMOVE_ATTENDEE_PATH_CONST                 = "/removeAttendee";
    public static final String EVENT_CREATE_PATH_CONST                          = "/create";

    public static final String ACTIVITY_PATH_CONST                              = "/activity";
    public static final String ACTIVITIES_PATH_CONST                            = "/activities";
    public static final String ACTIVITY_UPDATE_PATH_CONST                       = "/update";
    public static final String ACTIVITY_CREATE_PATH_CONST                       = "/create";

    public static final String ASSIGNMENT_PATH_CONST                            = "/assignment";
    public static final String ASSIGNMENT_UPDATE_PATH_CONST                     = "/updateItems";
    public static final String ASSIGNMENT_PENDING_COUNT_PATH_CONST              = "/pending/count";



    public static String getPathParamString(final String paramName){
        return String.format("{%s}", paramName);
    }


}
