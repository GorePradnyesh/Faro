package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.notification.NotificationPayloadHandler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class EventLandingPage extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NotificationPayloadHandler{

    public static final int NO_CHANGES = 0;
    private DateFormat sdf = new SimpleDateFormat(" EEE, MMM d, yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();

    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;;

    private static String TAG = "EventLandingPage";

    private static Event cloneEvent;

    private Button statusYes = null;
    private Button statusNo = null;
    private Button statusMaybe = null;
    private ImageButton pollButton = null;
    private ImageButton eventAssignmentButton = null;
    private ImageButton activityButton = null;
    private ImageButton editButton = null;
    private ImageButton guestListImageButton = null;
    private ImageButton mapMarker = null;

    private String eventID;
    final Context mContext = this;
    private Intent EventLandingPageReload;

    private GoogleMap mMap;

    private boolean mLocationPermissionGranted;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;

    private CameraPosition mCameraPosition;

    private GoogleApiClient mGoogleApiClient;

    private LatLng mEventLocation;
    private LatLng mDefaultLocation = new LatLng(0, 0);
    private SupportMapFragment mapFragment;
    private LinearLayout linlaHeaderProgress = null;

    private TextView event_name = null;
    private TextView eventDescription = null;
    private TextView startDateAndTime = null;
    private TextView endDateAndTime = null;
    private TextView eventAddress = null;

    private RelativeLayout EventLandingPageRelativeLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_landing_page);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        EventLandingPageRelativeLayout = (RelativeLayout) findViewById(R.id.eventLandingPageRelativeLayout);
        EventLandingPageRelativeLayout.setVisibility(View.GONE);

        Bundle extras = getIntent().getExtras();
        checkAndHandleNotification(extras);
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
                                    EventLandingPageReload.putExtra("eventID", eventID);
                                    finish();
                                    startActivity(EventLandingPageReload);
                                    break;
                                case MAYBE:
                                    eventListHandler.addEventToListAndMap(cloneEvent, EventInviteStatus.MAYBE);
                                    //Reload EventLandingPage
                                    EventLandingPageReload.putExtra("eventID", eventID);
                                    finish();
                                    startActivity(EventLandingPageReload);
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
        }else{
            statusYes.setVisibility(View.VISIBLE);
            statusNo.setVisibility(View.VISIBLE);
            pollButton.setVisibility(View.GONE);
            eventAssignmentButton.setVisibility(View.GONE);
            guestListImageButton.setVisibility(View.GONE);
            activityButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
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

    //TODO Clear all cloneEvent related datastructures
    @Override
    public void onBackPressed() {
        eventListHandler.deleteEventFromMapIfNotInList(cloneEvent);
        activityListHandler.clearActivityListAndMap();
        eventFriendListHandler.clearFriendListAndMap();
        assignmentListHandler.clearAssignmentListAndMap();
        finish();
        super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_landing_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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

    private void setEventLocationOnMap() {
        if (mMap == null) {
            return;
        }

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
    public void checkAndHandleNotification(Bundle extras) {

        if (extras == null)return; //TODO: How to handle such conditions

        String bundleType = extras.getString("bundleType");
        eventID = extras.getString("eventID");

        Log.d(TAG, "******eventID is " + eventID);

        if (bundleType == null){
            setupPageDetails();
            return;
        }

        //Else the bundleType is "notification"

        /*******************************************************************
         * Remove below code when server starts sending correct event ID.
         ********************************************************************/
        eventID = "af26e365-f569-452f-b44b-86fa8b032736";

        //API call to get event
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                setupPageDetails();
            }
        };

        Handler mainHandler = new Handler(mContext.getMainLooper());
        mainHandler.postDelayed(myRunnable, 5000);
        /********************************************************************
         ********************************************************************/

        //TODO: Call the get events API and insert into list and map. This API needs to change to return the invite status as well.
        getUpdatedEventFromServer();

    }

    private void setupPageDetails(){

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
        mapMarker = (ImageButton)findViewById(R.id.mapMarker);
        mapMarker.setImageResource(R.drawable.map_marker);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        statusYes = (Button) findViewById(R.id.statusYes);
        statusNo = (Button) findViewById(R.id.statusNo);
        statusMaybe = (Button) findViewById(R.id.statusMaybe);

        final Intent PollListPage = new Intent(EventLandingPage.this, PollListPage.class);
        final Intent EditEvent = new Intent(EventLandingPage.this, EditEvent.class);
        final Intent ActivityListPage = new Intent(EventLandingPage.this, ActivityListPage.class);
        final Intent EventFriendListLandingPageIntent = new Intent(EventLandingPage.this, EventFriendListLandingPage.class);
        final Intent AssignmentLandingPageTabsIntent = new Intent(EventLandingPage.this, AssignmentLandingPage.class);
        EventLandingPageReload = new Intent(EventLandingPage.this, EventLandingPage.class);

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

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
                finish();
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

        cloneEvent = eventListHandler.getEventCloneFromMap(eventID);
        if (cloneEvent == null){
            return; //TODO How to handle such a case?
        }

        eventStateBasedView(cloneEvent);

        controlFlagBasedView();

        String ev_name = cloneEvent.getEventName();
        event_name.setText(ev_name);

        String eventDescr = cloneEvent.getEventDescription();
        eventDescription.setText(eventDescr);

        startDateAndTime.setText(sdf.format(cloneEvent.getStartDate().getTime()) + " at " +
                stf.format(cloneEvent.getStartDate().getTime()));

        endDateAndTime.setText(sdf.format(cloneEvent.getEndDate().getTime()) + " at " +
                stf.format(cloneEvent.getEndDate().getTime()));

        final com.zik.faro.data.Location eventLocation = cloneEvent.getLocation();
        if (eventLocation == null){
            eventAddress.setVisibility(View.GONE);
            mapFragment.getView().setVisibility(View.GONE);
            mapMarker.setVisibility(View.GONE);
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
                    String label = eventLocation.getLocationName();
                    String uriBegin = "geo:" + latitude + "," + longitude;
                    String query = latitude + "," + longitude + "(" + label + ")";
                    String encodedQuery = Uri.encode(query);
                    String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                    Uri uri = Uri.parse(uriString);

                    Intent googleMapsAppIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(googleMapsAppIntent);
                }
            });
            setEventLocationOnMap();
        }

        //Make API call to get all all invitees for this event
        getEventInviteesFromServer();

        //Add event's assignment to the Assignment Handler
        Event originalEvent = eventListHandler.getOriginalEventFromMap(eventID);
        assignmentListHandler.addAssignmentToListAndMap(originalEvent.getAssignment(), null);

        //Make API call to get all activities for this event
        getEventActivitiesFromServer();
    }

    private void getUpdatedEventFromServer(){
        /*
        TODO: Call the get events API and insert into list and map. This API needs to change to return the invite status as well.
        serviceHandler.getEventHandler().getUpdatedEventFromServer(new BaseFaroRequestCallback<Event>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get the updated Event");
            }

            @Override
            public void onResponse(final Event receivedEvent, HttpError error) {
                if (error == null ) {
                    //Since update to server successful
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            eventListHandler.addEventToListAndMap(receivedEvent, );
                            eventListHandler.addDownloadedEventsToListAndMap();
                            setupPageDetails();
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }
                else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID);
        */
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
                            activityListHandler.addDownloadedActivitiesToListAndMap(activities, eventID);
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
                            eventFriendListHandler.addDownloadedFriendsToListAndMap(inviteeList);
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
}
