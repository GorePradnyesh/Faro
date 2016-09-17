package com.zik.faro.frontend;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import com.google.common.collect.Lists;
import com.squareup.okhttp.Request;
import java.util.Date;
import java.util.List;

import com.zik.faro.data.Event;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class EventLandingPage extends Activity {

    public static final int NO_CHANGES = 0;
    private DateFormat sdf = new SimpleDateFormat(" EEE, MMM d, yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();

    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;

    private static Event cloneEvent;

    private Button statusYes = null;
    private Button statusNo = null;
    private Button statusMaybe = null;
    private ImageButton pollButton = null;
    private ImageButton eventAssignmentButton = null;
    private ImageButton activityButton = null;
    private ImageButton editButton = null;
    private ImageButton addFriendsButton = null;
    private Button photosButton = null;
    private Button cameraButton = null;
    private Button uploadPhotosButton = null;
    private TextView event_status = null;

    private String eventID;
    final Context mContext = this;
    private Intent EventLandingPageReload;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTOS = 2;
    private static final String TAG = "EventLandingPage";

    private static final String CAPTURED_PHOTO_PATH_KEY = "capturedPhotoPath";
    private String mCurrentPhotoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_landing_page);

        final TextView event_name = (TextView) findViewById(R.id.eventNameText);
        TextView eventDescription = (TextView) findViewById(R.id.eventDescriptionTextView);

        TextView startDateAndTime = (TextView) findViewById(R.id.startDateAndTimeDisplay);

        TextView endDateAndTime = (TextView) findViewById(R.id.endDateAndTimeDisplay);

        pollButton = (ImageButton) findViewById(R.id.pollImageButton);
        pollButton.setImageResource(R.drawable.poll_icon);
        eventAssignmentButton = (ImageButton) findViewById(R.id.eventAssignmentImageButton);
        eventAssignmentButton.setImageResource(R.drawable.assignment_icon);
        activityButton = (ImageButton) findViewById(R.id.activityImageButton);
        activityButton.setImageResource(R.drawable.activity);
        addFriendsButton = (ImageButton) findViewById(R.id.addFriendsImageButton);
        addFriendsButton.setImageResource(R.drawable.friend_list);
        editButton = (ImageButton) findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);
        photosButton = (Button)findViewById(R.id.photosButton);
        cameraButton = (Button)findViewById(R.id.cameraButton);
        uploadPhotosButton = (Button)findViewById(R.id.uploadPhotosButton);


        statusYes = (Button) findViewById(R.id.statusYes);
        statusNo = (Button) findViewById(R.id.statusNo);
        statusMaybe = (Button) findViewById(R.id.statusMaybe);

        final Intent PollListPage = new Intent(EventLandingPage.this, PollListPage.class);
        final Intent EditEvent = new Intent(EventLandingPage.this, EditEvent.class);
        final Intent ActivityListPage = new Intent(EventLandingPage.this, ActivityListPage.class);
        final Intent InviteFriendToEventPage = new Intent(EventLandingPage.this, InviteFriendToEventPage.class);
        final Intent CreateEventAssignment = new Intent(EventLandingPage.this, CreateNewAssignment.class);
        final Intent AssignmentLandingPage = new Intent(EventLandingPage.this, AssignmentLandingPage.class);
        EventLandingPageReload = new Intent(EventLandingPage.this, EventLandingPage.class);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventID = extras.getString("eventID");
            cloneEvent = eventListHandler.getEventCloneFromMap(eventID);
            if (cloneEvent != null) {

                //Display elements based on Event Status
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

                assignmentListHandler.addAssignmentToListAndMap(cloneEvent.getAssignment());

            }
        }

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
                AssignmentLandingPage.putExtra("eventID", eventID);
                AssignmentLandingPage.putExtra("assignmentID", cloneEvent.getAssignment().getId());
                startActivity(AssignmentLandingPage);
            }
        });

        activityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityListPage.putExtra("eventID", eventID);
                startActivity(ActivityListPage);
            }
        });

        addFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InviteFriendToEventPage.putExtra("eventID", eventID);
                startActivity(InviteFriendToEventPage);
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

        photosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imagesViewIntent = new Intent(EventLandingPage.this, ImageGridView.class);
                imagesViewIntent.putExtra("eventID", cloneEvent.getEventId());
                imagesViewIntent.putExtra("eventName", cloneEvent.getEventName());
                startActivity(imagesViewIntent);
                finish();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        uploadPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                chooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(chooseIntent, REQUEST_PICK_PHOTOS);
            }
        });

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
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File imageFile = new File(mCurrentPhotoPath);
        Uri photoURI = Uri.fromFile(imageFile);
        mediaScanIntent.setData(photoURI);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Log.i(TAG, "Photo captured successfully. uri : Add photo to gallery");
            if (mCurrentPhotoPath != null) {
                Log.i(TAG, "mCurrentPhotoPath = " + mCurrentPhotoPath);
                galleryAddPic();
                uploadPhoto(mCurrentPhotoPath, cloneEvent.getEventName());
            } else {
                Log.e(TAG, "mCurrentPhotoPath = " + mCurrentPhotoPath);
            }
        } else if (requestCode == REQUEST_PICK_PHOTOS && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                List<String> filePaths = Lists.newArrayList();
                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    ClipData.Item item = mClipData.getItemAt(i);
                    String filePath = getRealPathFromURI(item.getUri());
                    filePaths.add(filePath);
                    Log.i(TAG, "filePath = " + filePath);
                }
                Log.i(TAG, "Selected Images size : " + filePaths.size());
                Log.i(TAG, "Selected Images : " + filePaths);

                Log.d(TAG, MessageFormat.format("Uploading images to album {0} ", cloneEvent.getEventName()));
                for (String filePath : filePaths) {
                    Log.i(TAG, MessageFormat.format("uploading image .. {0}", filePath));
                    uploadPhoto(filePath, cloneEvent.getEventName());
                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void uploadPhoto(String photoPath, String eventName) {
        String accessTokenString = "EAACEdEose0cBADakuvqmAlQlGnbBgLDwX29rCKcEtyufZCq1eZAZA1RdO3M1otZARANsZBXVR8MuVDPyVxNdO2s5VwnS9RSBBkBZC81v" +
                "2NCM77RSzNxr4O8pk3a9XQiOmyFbONzqLuya7nX4NBp4FliH9KATbm9SeNVWLD8ZCTyJQZDZD";
        String userId = "10155071787680006";
        FbGraphApiService fbGraphApiService = new FbGraphApiService(accessTokenString, userId);
        fbGraphApiService.uploadPhoto(photoPath, eventName);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
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
            addFriendsButton.setVisibility(View.VISIBLE);
            activityButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
        }else{
            statusYes.setVisibility(View.VISIBLE);
            statusNo.setVisibility(View.VISIBLE);
            pollButton.setVisibility(View.GONE);
            eventAssignmentButton.setVisibility(View.GONE);
            addFriendsButton.setVisibility(View.GONE);
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
        pollListHandler.clearPollListsAndMap();
        activityListHandler.clearActivityListAndMap();
        eventFriendListHandler.clearFriendListAndMap();
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
}
