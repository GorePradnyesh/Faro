package com.zik.faro.frontend;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.notification.NotificationPayloadHandler;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class EventLandingPage extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NotificationPayloadHandler{

    public static final int NO_CHANGES = 0;
    private DateFormat sdf = new SimpleDateFormat(" EEE, MMM d, yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();

    private static FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

    private Event cloneEvent;

    private Button statusYes = null;
    private Button statusNo = null;
    private Button statusMaybe = null;
    private ImageButton pollButton = null;
    private ImageButton eventAssignmentButton = null;
    private ImageButton activityButton = null;
    private ImageButton editButton = null;
    private Button photosButton = null;
    private Button cameraButton = null;
    private Button uploadPhotosButton = null;
    private ImageButton guestListImageButton = null;
    private ImageButton mapMarker = null;
    private ImageView transparentImageView = null;

    private String eventID;
    private Context mContext = this;
    private Intent eventLandingPageReload;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTOS = 2;
    private final int PERMISSIONS_REQ_EXTERNAL_STORAGE = 1;

    private static final String TAG = "EventLandingPage";
    private static final String CAPTURED_PHOTO_PATH_KEY = "capturedPhotoPath";

    private String cameraTakenPhotoPath = null;

    private FbLoginFragment fbLoginFragment;

    private GoogleMap mMap;

    private boolean mLocationPermissionGranted;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;

    private CameraPosition mCameraPosition;

    private GoogleApiClient mGoogleApiClient;

    private LatLng mEventLocation;
    private LatLng mDefaultLocation = new LatLng(0, 0);
    private SupportMapFragment mapFragment;
    private ScrollView mainScrollView = null;
    private LinearLayout linlaHeaderProgress = null;

    private TextView event_name = null;
    private TextView eventDescription = null;
    private TextView startDateAndTime = null;
    private TextView endDateAndTime = null;
    private TextView eventAddress = null;

    private RelativeLayout mapsStuffRelativeLayout = null;

    private Bundle extras = null;

    private RelativeLayout EventLandingPageRelativeLayout = null;
    private LinearLayout photosStuffLinearLayout = null;
    private String isNotification = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_landing_page);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        EventLandingPageRelativeLayout = (RelativeLayout) findViewById(R.id.eventLandingPageRelativeLayout);
        EventLandingPageRelativeLayout.setVisibility(View.GONE);

        extras = getIntent().getExtras();
        if (extras == null)return; //TODO: How to handle such conditions

        checkAndHandleNotification();
    }



    private void updateUserEventInviteStatus(final EventInviteStatus eventInviteStatus) {
        serviceHandler.getEventHandler().updateEventUserInviteStatus(new BaseFaroRequestCallback<String>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to send cloneEvent Invite Status");
            }

            @Override
            public void onResponse(String s, HttpError error) {
                if (error == null ) {
                    //Since update to server successful
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            switch (eventInviteStatus){
                                case ACCEPTED:
                                    eventListHandler.addEventToListAndMap(cloneEvent, EventInviteStatus.ACCEPTED);
                                    //Reload EventLandingPage
                                    eventLandingPageReload.putExtra("eventID", eventID);
                                    finish();
                                    startActivity(eventLandingPageReload);
                                    break;
                                case MAYBE:
                                    eventListHandler.addEventToListAndMap(cloneEvent, EventInviteStatus.MAYBE);
                                    //Reload EventLandingPage
                                    eventLandingPageReload.putExtra("eventID", eventID);
                                    finish();
                                    startActivity(eventLandingPageReload);
                                    break;
                                case DECLINED:
                                    eventListHandler.removeEventFromListAndMap(eventID);
                                    finish();
                                    break;
                            }
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                } else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID, eventInviteStatus);
    }

    private void eventStateBasedView(Event event){

        EventInviteStatus inviteStatus = eventListHandler.getUserEventStatus(eventID);

        if (inviteStatus == EventInviteStatus.ACCEPTED){
            statusYes.setVisibility(View.GONE);
            statusNo.setVisibility(View.GONE);
            statusMaybe.setVisibility(View.GONE);
            pollButton.setVisibility(View.VISIBLE);
            eventAssignmentButton.setVisibility(View.VISIBLE);
            guestListImageButton.setVisibility(View.VISIBLE);
            activityButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
            photosStuffLinearLayout.setVisibility(View.VISIBLE);
        }else{
            statusYes.setVisibility(View.VISIBLE);
            statusNo.setVisibility(View.VISIBLE);
            pollButton.setVisibility(View.GONE);
            eventAssignmentButton.setVisibility(View.GONE);
            guestListImageButton.setVisibility(View.GONE);
            activityButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
            photosStuffLinearLayout.setVisibility(View.GONE);
        }

        if (inviteStatus == EventInviteStatus.INVITED) {
            statusMaybe.setVisibility(View.VISIBLE);
        } else if (inviteStatus == EventInviteStatus.MAYBE){
            statusMaybe.setVisibility(View.GONE);
        }
    }

    private void controlFlagBasedView() {
        if(cloneEvent.getEventCreatorId() == null) {
            if (cloneEvent.getControlFlag()) {
                editButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        eventListHandler.deleteEventFromMapIfNotInList(cloneEvent);
        finish();
        super.onBackPressed();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Play services connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Todo: Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void checkAndHandleNotification() {
        isNotification = extras.getString("bundleType");
        eventID = extras.getString("eventID");

        Log.d(TAG, "******eventID is " + eventID);

        if (isNotification == null){
            setupPageDetails();
            return;
        }

        //Else the bundleType is "notification"
        getEventFromServer();
    }

    private void setupPageDetails(){

        cloneEvent = eventListHandler.getEventCloneFromMap(eventID);
        if (cloneEvent == null){
            return; //TODO How to handle such a case?
        }

        linlaHeaderProgress.setVisibility(View.GONE);
        EventLandingPageRelativeLayout.setVisibility(View.VISIBLE);

        event_name = (TextView) findViewById(R.id.eventNameText);
        eventDescription = (TextView) findViewById(R.id.eventDescriptionTextView);

        startDateAndTime = (TextView) findViewById(R.id.startDateAndTimeDisplay);

        endDateAndTime = (TextView) findViewById(R.id.endDateAndTimeDisplay);

        eventAddress = (TextView) findViewById(R.id.locationAddressTextView);

        pollButton = (ImageButton) findViewById(R.id.pollImageButton);
        pollButton.setImageResource(R.drawable.poll_icon);
        eventAssignmentButton = (ImageButton) findViewById(R.id.eventAssignmentImageButton);
        eventAssignmentButton.setImageResource(R.drawable.assignment_icon);
        activityButton = (ImageButton) findViewById(R.id.activityImageButton);
        activityButton.setImageResource(R.drawable.activity);
        guestListImageButton = (ImageButton) findViewById(R.id.guestListImageButton);
        guestListImageButton.setImageResource(R.drawable.friend_list);
        editButton = (ImageButton) findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);
        photosButton = (Button) findViewById(R.id.photosButton);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        uploadPhotosButton = (Button) findViewById(R.id.uploadPhotosButton);
        mapMarker = (ImageButton) findViewById(R.id.mapMarker);
        mapMarker.setImageResource(R.drawable.map_marker);

        mainScrollView = (ScrollView) findViewById(R.id.mainScrollView);
        transparentImageView = (ImageView) findViewById(R.id.transparentImageOnMap);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        statusYes = (Button) findViewById(R.id.statusYes);
        statusNo = (Button) findViewById(R.id.statusNo);
        statusMaybe = (Button) findViewById(R.id.statusMaybe);

        final Intent PollListPage = new Intent(EventLandingPage.this, PollListPage.class);
        final Intent EditEvent = new Intent(EventLandingPage.this, EditEvent.class);
        final Intent ActivityListPage = new Intent(EventLandingPage.this, ActivityListPage.class);
        final Intent EventFriendListLandingPageIntent = new Intent(EventLandingPage.this, EventFriendListLandingPage.class);
        final Intent AssignmentLandingPageTabsIntent = new Intent(EventLandingPage.this, AssignmentLandingPage.class);
        eventLandingPageReload = new Intent(EventLandingPage.this, EventLandingPage.class);

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        photosStuffLinearLayout = (LinearLayout) findViewById(R.id.photosStuff);
        mapsStuffRelativeLayout = (RelativeLayout) findViewById(R.id.mapStuff);

        statusYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserEventInviteStatus(EventInviteStatus.ACCEPTED);
            }
        });

        statusMaybe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserEventInviteStatus(EventInviteStatus.MAYBE);
            }
        });

        statusNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserEventInviteStatus(EventInviteStatus.DECLINED);
            }
        });

        pollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PollListPage.putExtra("eventID", eventID);
                startActivity(PollListPage);
            }
        });

        eventAssignmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssignmentLandingPageTabsIntent.putExtra("eventID", eventID);
                AssignmentLandingPageTabsIntent.putExtra("assignmentID", cloneEvent.getAssignment().getId());
                startActivity(AssignmentLandingPageTabsIntent);
            }
        });

        activityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityListPage.putExtra("eventID", eventID);
                startActivity(ActivityListPage);
            }
        });

        guestListImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventFriendListLandingPageIntent.putExtra("eventID", eventID);
                startActivity(EventFriendListLandingPageIntent);
                //finish();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditEvent.putExtra("eventID", eventID);
                startActivity(EditEvent);
                finish();
            }
        });

        photosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imagesViewIntent = new Intent(EventLandingPage.this, ImageGridViewActivity.class);
                imagesViewIntent.putExtra("eventId", cloneEvent.getId());
                imagesViewIntent.putExtra("eventName", cloneEvent.getEventName());
                startActivity(imagesViewIntent);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissionsForUploadingPhotos()) {
                    // Proceed with the workflow to capture a photo and upload
                    dispatchTakePictureIntent();
                }
            }
        });

        uploadPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissionsForUploadingPhotos()) {
                    // Proceed with upload photos workflow
                    startPhotosUploadWorkflow();
                }
            }
        });

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        eventStateBasedView(cloneEvent);

        controlFlagBasedView();

        String ev_name = cloneEvent.getEventName();
        event_name.setText(ev_name);

        String eventDescr = cloneEvent.getEventDescription();
        if (Strings.isNullOrEmpty(eventDescr)){
            eventDescription.setVisibility(View.GONE);
        } else {
            eventDescription.setText(eventDescr);
        }

        startDateAndTime.setText(sdf.format(cloneEvent.getStartDate().getTime()) + " at " +
                stf.format(cloneEvent.getStartDate().getTime()));

        endDateAndTime.setText(sdf.format(cloneEvent.getEndDate().getTime()) + " at " +
                stf.format(cloneEvent.getEndDate().getTime()));

        final com.zik.faro.data.Location eventLocation = cloneEvent.getLocation();
        if (eventLocation == null){

            mapsStuffRelativeLayout.setVisibility(View.GONE);
        }else{
            mEventLocation = new LatLng(eventLocation.getPosition().getLatitude(), eventLocation.getPosition().getLongitude());
            String str = GetLocationAddressString.getLocationAddressString(eventLocation);
            eventAddress.setText(str);
            eventAddress.setTextColor(Color.BLUE);
            eventAddress.setPaintFlags(eventAddress.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            mapMarker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setEventLocationOnMap();
                }
            });
            mapMarker.setBackgroundColor(Color.TRANSPARENT);

            eventAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Creating url for Google Maps.
                    double latitude = eventLocation.getPosition().getLatitude();
                    double longitude = eventLocation.getPosition().getLongitude();
                    String label = GetLocationAddressString.getLocationAddressString(eventLocation);
                    String uriBegin = "geo:" + latitude + "," + longitude;
                    String query = latitude + "," + longitude + "(" + label + ")";
                    String encodedQuery = Uri.encode(query);
                    String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                    Uri uri = Uri.parse(uriString);

                    Intent googleMapsAppIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(googleMapsAppIntent);
                }
            });

            checkPermissionForLocationServices ();
        }


        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });


        //Make API call to get all all invitees for this event
        getEventInviteesFromServer();

        //Add event's assignment to the Assignment Handler
        Event originalEvent = eventListHandler.getOriginalEventFromMap(eventID);
        assignmentListHandler.addAssignmentToListAndMap(eventID, originalEvent.getAssignment(), null, mContext);

        //Make API call to get all activities for this event
        getEventActivitiesFromServer();

        // Initialize facebook fragment for login
        fbLoginFragment = (FbLoginFragment) getSupportFragmentManager().findFragmentById(R.id.fb_login_page);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        checkPermissionForLocationServices();

        // Get the location of the event and set the position of the map also mark mylocation on the map.
        setEventLocationOnMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        setEventLocationOnMap();
    }

    private void checkPermissionForLocationServices() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)){
                    Toast.makeText(this, "Enable location services to see your location on the map", Toast.LENGTH_SHORT).show();
                }
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            mLocationPermissionGranted = true;
        }

        if (mMap != null) {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
    }

    private void setEventLocationOnMap() {
        if (mMap == null) {
            return;
        }

        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mEventLocation != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mEventLocation, DEFAULT_ZOOM));
            mMap.addMarker(new MarkerOptions().position(mEventLocation));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }


    private void getEventFromServer(){
        serviceHandler.getEventHandler().getEvent(new BaseFaroRequestCallback<EventInviteStatusWrapper>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get Event from server");
            }

            @Override
            public void onResponse(final EventInviteStatusWrapper eventInviteStatusWrapper, HttpError error) {
                if (error == null ) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Event from server");
                            Event event = eventInviteStatusWrapper.getEvent();
                            eventListHandler.addEventToListAndMap(event,
                                    eventInviteStatusWrapper.getInviteStatus());
                            setupPageDetails();
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID);
    }

    public void getEventActivitiesFromServer(){
        serviceHandler.getActivityHandler().getActivities(new BaseFaroRequestCallback<List<com.zik.faro.data.Activity>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get activity list");
            }

            @Override
            public void onResponse(final List<com.zik.faro.data.Activity> activities, HttpError error) {
                if (error == null) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received activities from the server!!");
                            activityListHandler.addDownloadedActivitiesToListAndMap(eventID, activities, mContext);
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                } else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID);
    }

    private boolean checkPermissionsForUploadingPhotos() {
        // Beginning in Android 6.0 (API level 23), users grant permissions to
        // apps while the app is running, not when they install the app.

        // Check Permission to access READ and WRITE to the External Storage and request for access if not already granted
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Show asynchronously, an explanation of why the permission is required
                /*if (ActivityCompat.shouldShowRequestPermissionRationale((android.app.Activity) mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                }*/

                Log.i(TAG, "Requesting permission to access external write storage for uploading photos");
                ActivityCompat.requestPermissions((android.app.Activity) mContext,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQ_EXTERNAL_STORAGE);
                return false;
            }
        }

        return true;
    }

    private void requestPublishPrivileges() {
        // Initiate FB login
        Log.i(TAG, "initiate login again to get additional permissions");
        LoginManager.getInstance().logInWithPublishPermissions(fbLoginFragment, Lists.newArrayList("publish_actions"));
    }

    private void requestUserPhotosPrivileges() {
        // Initiate FB login
        Log.i(TAG, "initiate login again to get additional permissions");
        LoginManager.getInstance().logInWithReadPermissions(fbLoginFragment, Lists.newArrayList("user_photos"));
    }

    private void initiateFbLogin() {
        LoginManager.getInstance().logInWithReadPermissions(fbLoginFragment, Lists.newArrayList("public_profile", "email", "user_photos"));
    }

    private void uploadPhoto(List<String> photoPaths, Event event) {
        try {
            FbGraphApiService fbGraphApiService = new FbGraphApiService();
            fbGraphApiService.uploadPhotos(photoPaths, event, this);
        } catch (Exception e) {
            Log.e(TAG, MessageFormat.format("could not upload photos : {0}", photoPaths), e);
        }
    }

    private void startPhotosUploadWorkflow() {
        // Get FB access token
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken == null || accessToken.isExpired()) {
            // Initiate FB login and get user photos privileges
            initiateFbLogin();
        } else if (!accessToken.getPermissions().contains("user_photos")) {
            requestUserPhotosPrivileges();
        } else if (!accessToken.getPermissions().contains("publish_actions")) {
            requestPublishPrivileges();
        } else {
            startActivityForResult(new Intent(EventLandingPage.this, ImagePickerActivity.class), REQUEST_PICK_PHOTOS);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "could not create image file to save photo to be taken");
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                // TODO : Use FileProvider to get URI for the file
                // which can be used by the camera App

                /*Uri photoURI = FileProvider.getUriForFile(this,
                        "com.zik.faro.fileprovider",
                        photoFile);*/
                Uri photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = MessageFormat.format("FARO_JPEG_{0}_", timeStamp);
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        cameraTakenPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File imageFile = new File(cameraTakenPhotoPath);
        Uri photoURI = Uri.fromFile(imageFile);
        mediaScanIntent.setData(photoURI);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (cameraTakenPhotoPath != null) {
                Log.i(TAG, "Photo captured successfully. Add photo to gallery. cameraTakenPhotoPath = " + cameraTakenPhotoPath);
                galleryAddPic();
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken == null || accessToken.isExpired()) {
                    initiateFbLogin();
                } else if (!accessToken.getPermissions().contains("publish_actions")) {
                    requestPublishPrivileges();
                } else {
                    uploadPhoto(Lists.newArrayList(cameraTakenPhotoPath), cloneEvent);
                }
            } else {
                Log.e(TAG, "cameraTakenPhotoPath = " + cameraTakenPhotoPath);
            }
        } else if (requestCode == REQUEST_PICK_PHOTOS && resultCode == RESULT_OK) {
            List<String> filePaths = data.getStringArrayListExtra("images");

            if (filePaths != null && !filePaths.isEmpty()) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken == null || accessToken.isExpired()) {
                    initiateFbLogin();
                } else if (!accessToken.getPermissions().contains("publish_actions")) {
                    requestPublishPrivileges();
                } else {
                    Log.d(TAG, MessageFormat.format("Uploading images to album {0} ", cloneEvent.getEventName()));
                    uploadPhoto(filePaths, cloneEvent);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, cameraTakenPhotoPath);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        cameraTakenPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
    }

    public void getEventInviteesFromServer(){
        serviceHandler.getEventHandler().getEventInvitees(new BaseFaroRequestCallback<InviteeList>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get cloneEvent Invitees");
            }

            @Override
            public void onResponse(final InviteeList inviteeList, HttpError error) {
                if (error == null) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Invitee List for the cloneEvent");
                            eventFriendListHandler.addDownloadedFriendsToListAndMap(eventID, inviteeList, mContext);
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                } else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID);
    }

    @Override
    protected void onResume() {
        if (isNotification == null) {
            // Check if the version is same. It can be different if this page is loaded and a notification
            // is received for this later which updates the global memory but clonedata on this page remains
            // stale.
            // This check is not necessary when opening this page directly through a notification.
            Long versionInGlobalMemory = eventListHandler.getOriginalEventFromMap(eventID).getVersion();
            if (!cloneEvent.getVersion().equals(versionInGlobalMemory)) {
                Intent eventLandingPageReloadIntent = new Intent(EventLandingPage.this, EventLandingPage.class);
                eventLandingPageReloadIntent.putExtra("eventID", eventID);
                finish();
                startActivity(eventLandingPageReloadIntent);
            }
        }
        super.onResume();
    }
}
