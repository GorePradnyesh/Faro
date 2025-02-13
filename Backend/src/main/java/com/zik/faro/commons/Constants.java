package com.zik.faro.commons;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pgore on 11/4/14.
 */
public final class Constants {

	public static final String HTTP_OK                                          = "OK";

    //----- Query String parameters ----- //
    public static final String FARO_USER_ID_PARAM                               = "userId";
    public static final String COUNT_PARAM										= "count";
	public static final String LOGIN_USERNAME_PARAM                             = "username";
    public static final String FARO_USERNAME_PARAM                              = "username";
    public static final String FARO_TOKEN_PARAM                                 = "token";

    //----- Path Parameters ----//
    public static final String EVENT_ID_PATH_PARAM                              = "eventId";
    public static final String EVENT_ID_PATH_PARAM_STRING                       = "/{" + EVENT_ID_PATH_PARAM +"}";

    public static final String ACTIVITY_ID_PATH_PARAM                           = "activityId";
    public static final String ACTIVITY_ID_PATH_PARAM_STRING                    = "/{" + ACTIVITY_ID_PATH_PARAM +"}";

    public static final String ASSIGNMENT_ID_PATH_PARAM                         = "assignmentId";
    public static final String ASSIGNMENT_ID_PATH_PARAM_STRING                  = "/{" + ASSIGNMENT_ID_PATH_PARAM +"}";
    
    public static final String POLL_ID_PATH_PARAM                               = "pollId";
    public static final String POLL_ID_PATH_PARAM_STRING                        = "/{" + POLL_ID_PATH_PARAM +"}";

    public static final String IMAGE_ID_PATH_PARAM                              = "imageId";
    public static final String IMAGE_ID_PATH_PARAM_STRING                       = "/{" + IMAGE_ID_PATH_PARAM + "}";


    // ---- Path Constants ---- //

    // --- External expense managemtn path constants --- //
    public static final String EXPENSE_ID_PATH_CONST                            = "/expenseIds";
    public static final String EXPENSE_PATH_CONST                               = "/expense";
    public static final String CREATE_EXPENSE_GROUP_PATH_CONST                  = "/createExpenseGroup";

    // ---- Friend Invitation path constants --- //
    public static final String FRIENDS_PATH_CONST                               = "/friends";
    public static final String FACEBOOK_FRIENDS_PATH_CONST                      = "/fbFriendsInvite";
    public static final String INVITE_PATH_CONST                                = "/invite";
    public static final String REMOVE_PATH_CONST                                = "/remove";

    // --- User profile path constants --- //
    public static final String PROFILE_PATH_CONST                               = "/profile";
    public static final String PROFILE_CREATE_PATH_CONST                        = "/create";
    public static final String PROFILE_UPDATE_PATH_CONST                        = "/update";
    public static final String PROFILE_UPSERT_PATH_CONST                        = "/upsert";
    public static final String PROFILE_ADD_USER_REGISTRATION_TOKEN				= "/add/registrationToken";
    public static final String PROFILE_REMOVE_USER_REGISTRATION_TOKEN			= "/remove/registrationToken";


    // --- Feedback path constant --- //
    public static final String FEEDBACK_PATH_CONST                              = "/feedback";

    // --- Event path constants --- //
    public static final String EVENT_PATH_CONST                                 = "/event";
    public static final String EVENTS_PATH_CONST                                = "/events";
    public static final String EVENT_DETAILS_PATH_CONST                         = "/details";
    public static final String EVENT_INVITEES_PATH_CONST                        = "/invitees";
    public static final String EVENT_DISABLE_CONTROL_PATH_CONST                 = "/disableControl";
    public static final String EVENT_REMOVE_ATTENDEE_PATH_CONST                 = "/removeAttendee";
    public static final String UPDATE_INVITE_STATUS_PATH_CONST                  = "/updateInviteStatus";
    public static final String EVENT_CREATE_PATH_CONST                          = "/create";
    public static final String EVENT_ADD_FRIENDS_CONST							= "/add";
    public static final String EVENT_UPDATE_PATH_CONST							= "/updateEvent";

    // --- Activity path constants --- //
    public static final String ACTIVITY_PATH_CONST                              = "/activity";
    public static final String ACTIVITIES_PATH_CONST                            = "/activities";
    public static final String ACTIVITY_UPDATE_PATH_CONST                       = "/update";
    public static final String ACTIVITY_CREATE_PATH_CONST                       = "/create";

    // ---- Assignment path constants ---- //
    public static final String ASSIGNMENT_PATH_CONST                            = "/assignment";
    public static final String ASSIGNMENTS_PATH_CONST                           = "/assignments";
    public static final String ASSIGNMENT_UPDATE_PATH_CONST                     = "/updateItems";
    public static final String ASSIGNMENT_PENDING_COUNT_PATH_CONST              = "/pending/count";
    public static final String ASSIGNMENT_DELETE_PATH_CONST                     = "/pending/count";

    // ---- Poll path constants ---- //
    public static final String POLL_PATH_CONST                                  = "/poll";
    public static final String POLLS_PATH_CONST                                 = "/polls";
    public static final String POLL_UNVOTED_COUNT_CONST                         = "/unvoted/count";
    public static final String POLL_CREATE_PATH_CONST                           = "/create";
    public static final String POLL_VOTE_PATH_CONST                             = "/vote";
    public static final String POLL_UPDATE_PATH_CONST                             = "/updatePoll";
    public static final String POLL_UPDATE_POLLOPTIONS_PATH_CONST               = "/updatePollOptions";
    public static final String POLL_CAST_VOTE_PATH_CONST					= "/castVote";
    public static final String POLL_ADD_OPTION_PATH_CONST                       = "/addOption";
    public static final String POLL_CLOSE_PATH_CONST                            = "/close";

    // ---- Images path constants ---- //
    public static final String IMAGE_PATH_CONST                                 = "/image";
    public static final String IMAGES_PATH_CONST                                = "/images";
    public static final String IMAGES_CREATE_CONST                              = "/create";

    // ---- Misc constants ---- //
    public static final String ALL												= "all";
    public static final int MAX_ITEMS_TO_FETCH_FROM_DATASTORE					= 20;

    // ---- notificatioin constants ---- //
    public static final String FCM_ENDPOINT 									= "https://fcm.googleapis.com";
    public static final String IID_ENDPOINT										= "https://iid.googleapis.com";
    public static final String SEND_NOTIFICATION_PATH_CONST						= "/fcm/send";
    public static final String SUBSCRIBE_TOKEN_PATH								= "/iid/v1:batchAdd";
    public static final String UNSUBSCRIBE_TOKEN_PATH							= "/iid/v1:batchRemove";
    public static final String AUTHORIZATION_HEADER_KEY							= "authorization";
    public static final String FARO_EVENT_TOPIC_CONST							= "/topics/";
    public static final String CLICK_ACTION_DEFAULT								= "DEFAULTACTION";
    public static final String NOTIFICATION_TYPE_EVENT_GENERIC					= "notificationType_EventGeneric";
    public static final String NOTIFICATION_TYPE_EVENT_INVITE					= "notificationType_EventInvite";
    public static final String NOTIFICATION_TYPE_EVENT_DELETED					= "notificationType_EventDeleted";
    public static final String NOTIFICATION_TYPE_ACTIVITY_CREATED				= "notificationType_ActivityCreated";
    public static final String NOTIFICATION_TYPE_ACTIVITY_GENERIC				= "notificationType_ActivityGeneric";
    public static final String NOTIFICATION_TYPE_ACTIVITY_DELETED				= "notificationType_ActivityDeleted";
    public static final String NOTIFICATION_TYPE_POLL_CREATED					= "notificationType_PollCreated";
    public static final String NOTIFICATION_TYPE_POLL_GENERIC					= "notificationType_PollGeneric";
    public static final String NOTIFICATION_TYPE_POLL_DELETED					= "notificationType_PollDeleted";
    public static final String NOTIFICATION_TYPE_POLL_CLOSED					= "notificationType_PollClosed";
    public static final String NOTIFICATION_TYPE_ASSIGNMENT_CREATED				= "notificationType_AssignmentCreated";
    public static final String NOTIFICATION_TYPE_ASSIGNMENT_PENDING				= "notificationType_AssignmentPending";
    public static final String NOTIFICATION_TYPE_MEDIA_ADDED					= "notificationType_MediaAdded";
    public static final String NOTIFICATION_TYPE_UPLOAD_MEDIA_REMINDER			= "notificationType_UploadMediaReminder";
    public static final String NOTIFICATION_TYPE_CONST							= "type";
    public static final String NOTIFICATION_TITLE_CONST							= "title";
    public static final String NOTIFICATION_BODY_CONST							= "body";
    public static final String NOTIFICATION_CLICK_ACTION_CONST					= "click_action";
    public static final String NOTIFICATION_USER_CONST							= "user";
    
    public static final String NOTIFICATION_EVENTID_CONST						= "eventId";
    public static final String NOTIFICATION_POLLID_CONST						= "pollId";
    public static final String NOTIFICATION_ACTIVITYID_CONST					= "activityId";
    public static final String NOTIFICATION_ASSIGNMENTID_CONST					= "assignmentId";
    public static final String NOTIFICATION_VERSION_CONST						= "version";
    public static final String NOTIFICATION_ID_CONST						 	= "notificationId";
    

    // -- Authentication path constants -- //
    public static final String AUTH_PATH_CONST                                  = "/nativeLogin";
    public static final String AUTH_LOGIN_PATH_CONST                            = "/login";
    public static final String AUTH_LOGOUT_PATH_CONST                           = "/logout";
    public static final String AUTH_SIGN_UP_PATH_CONST                          = "/signup";
    public static final String AUTH_PASSWORD_PATH_CONST                         = "/password";
    public static final String AUTH_RESET_PASSWORD_PATH_CONST                   = "/resetPassword";
    public static final String AUTH_FORGOT_PASSWORD_PATH_CONST                  = "/forgotPassword";
    public static final String AUTH_FORGOT_PASSWORD_FORM_PATH_CONST             = "/forgotPasswordForm";
    public static final String AUTH_NEW_PASSWORD_PATH_CONST                     = "/newPassword";

    public static final String FIREBASE_AUTH_CONST                              = "/firebaseLogin";

    // -- Test -- //
    public static final String TESTS_API_PATH_CONST                             = "/test";
   
}
