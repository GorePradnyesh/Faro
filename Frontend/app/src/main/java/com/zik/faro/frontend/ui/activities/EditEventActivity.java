package com.zik.faro.frontend.ui.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.common.collect.Sets;
import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.data.Location;
import com.zik.faro.data.UpdateRequest;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.util.FaroExceptionHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.util.GetLocationAddressString;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import static android.widget.Toast.LENGTH_LONG;


/*
* In this class we update the existing event object with the updated information that the user
* inputs.
* Special care should be taken that the updates should not reflect in the Event object until the
* user presses the OK button. Until then simply collect all the updated information in local
* variables.*/
public class EditEventActivity extends Activity {
    private Button startDateButton = null;
    private Button startTimeButton = null;
    private Button endTimeButton = null;
    private Button endDateButton = null;
    private ImageButton deleteLocation = null;

    private TextView eventAddress = null;

    private DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");

    private EventListHandler eventListHandler = EventListHandler.getInstance(this);
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private Event cloneEvent;

    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private Calendar currentCalendar = Calendar.getInstance();

    private RelativeLayout popUpRelativeLayout;

    private String eventId;
    private static String TAG = "EditEventActivity";

    private Intent eventLandingPageIntent = null;

    private int PLACE_PICKER_REQUEST = 1;

    private Context mContext;
    private Activity mActivity = this;

    private Location location = null;

    private LinearLayout linlaHeaderProgress = null;
    private RelativeLayout editEventRelativeLayout = null;
    private Bundle extras = null;

    private Set<String> updatedFields = Sets.newHashSet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        mContext = this;

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        editEventRelativeLayout = (RelativeLayout) findViewById(R.id.editEventRelativeLayout);
        editEventRelativeLayout.setVisibility(View.GONE);

        extras = getIntent().getExtras();
        if (extras == null)return; //TODO: How to handle such conditions

        try {
            setupPageDetails();
        } catch (FaroObjectNotFoundException e) {
            //Event has been deleted.
            Toast.makeText(this, "Event has been deleted", LENGTH_LONG).show();
            Log.e(TAG, MessageFormat.format("Event {0} has been deleted", eventId));
            finish();
        }
    }

    private void setupPageDetails () throws FaroObjectNotFoundException{
        linlaHeaderProgress.setVisibility(View.GONE);
        editEventRelativeLayout.setVisibility(View.VISIBLE);

        eventId = extras.getString(FaroIntentConstants.EVENT_ID);

        /*
        * Do not remove the event from the list and the map in the eventListHandler below.
        * Make a clone of the event and make changes to that.
        * When the OK button is clicked, only then remove the event and insert the clone.
        * This way if the user makes some changes but then clicks back without clicking OK button,
        * the original event is not affected.
        */

        cloneEvent = eventListHandler.getCloneObject(eventId);

        startDateButton = (Button) findViewById(R.id.startDateButton);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);
        endDateButton = (Button) findViewById(R.id.endDateButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);
        deleteLocation = (ImageButton) findViewById(R.id.deleteLocation);
        deleteLocation.setImageResource(R.drawable.cancel);
        deleteLocation.setVisibility(View.GONE);

        TextView eventNameTextView = (TextView) findViewById(R.id.eventNameTextView);
        final EditText eventDescriptionEditText = (EditText) findViewById(R.id.eventDescriptionEditText);
        eventAddress = (TextView) findViewById(R.id.locationAddressTextView);

        Button editEventOK = (Button) findViewById(R.id.editEventOK);
        Button deleteEventButton = (Button) findViewById(R.id.deleteEvent);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.editEventPage);

        eventLandingPageIntent = new Intent(EditEventActivity.this, EventLandingPage.class);

        String eventName = cloneEvent.getEventName();
        eventNameTextView.setText(eventName);
        eventDescriptionEditText.setText(cloneEvent.getEventDescription());
        setBothTimeAndDateToEventDates();

        //TODO: User should warned that this can lead to mismatch with previously created activities which might not lie in the event's new time period.
        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStartDate();
            }
        });

        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEndDate();
            }
        });

        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStartTime();
            }
        });

        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEndTime();
            }
        });

        if (cloneEvent.getLocation() != null){
            String str = GetLocationAddressString.getLocationAddressString(cloneEvent.getLocation());
            eventAddress.setText(str);
            eventAddress.setTextColor(Color.BLUE);
            eventAddress.setPaintFlags(eventAddress.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            deleteLocation.setVisibility(View.VISIBLE);
        }

        eventAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    Intent intent = builder.build(mActivity);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        deleteLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location = null;
                eventAddress.setText("");
                deleteLocation.setVisibility(View.GONE);
            }
        });

        /*
        * Since OK button is pressed we will remove the event from the list and insert the clone.
        */
        editEventOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cloneEvent.setStartDate(startDateCalendar);
                cloneEvent.setEndDate(endDateCalendar);
                cloneEvent.setLocation(location);

                updatedFields.add(Event.START_DATE);
                updatedFields.add(Event.END_DATE);
                updatedFields.add(Event.LOCATION);

                UpdateRequest<Event> eventUpdateRequest = new UpdateRequest<>(updatedFields, cloneEvent);

                serviceHandler.getEventHandler().updateEvent(new BaseFaroRequestCallback<Event>() {
                    @Override
                    public void onFailure(Request request, IOException ex) {
                        Log.e(TAG, "failed to send event create request");
                    }

                    @Override
                    public void onResponse(final Event receivedEvent, HttpError error) {
                        if (error == null) {
                            // Since update to server was successful, adding event to List and Map below
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // Since update to server was successful, adding event to List and Map below
                                    Log.i(TAG, "Event update completed successfully");
                                    eventListHandler.addEventToListAndMap(receivedEvent, EventInviteStatus.ACCEPTED);
                                    FaroIntentInfoBuilder.eventIntent(eventLandingPageIntent, eventId);
                                    startActivity(eventLandingPageIntent);
                                    finish();
                                }
                            });
                        } else {
                            Log.e(TAG, MessageFormat.format("Failed to update event. code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                            // TODO : Handle errors and update version conflict
                        }
                    }
                }, eventId, eventUpdateRequest);
            }
        });

        deleteEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmEventDeletePopUP(v);
            }
        });

        eventDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                cloneEvent.setEventDescription(eventDescriptionEditText.getText().toString());
                updatedFields.add(Event.DESCRIPTION);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                String locationNameStr = null;
                String locationAddressStr = null;

                /*
                 * TODO: Check if the below condition is OK. Basic testing showed that for places
                 * which do not have a valid name, getName returns the cordinates and we do not want
                 * to show that. The placeType for these places was set to TYPE_OTHER.
                 */
                if (!place.getPlaceTypes().contains(Place.TYPE_OTHER)){
                    locationNameStr = place.getName().toString();
                }

                if (!place.getAddress().equals("")){
                    locationAddressStr = place.getAddress().toString();
                }

                GeoPosition geoPosition = new GeoPosition(place.getLatLng().latitude, place.getLatLng().longitude);
                location = new Location(locationNameStr, locationAddressStr, geoPosition);

                String str = GetLocationAddressString.getLocationAddressString(location);
                eventAddress.setText(str);
                deleteLocation.setVisibility(View.VISIBLE);
            }
        }
    }

    private void confirmEventDeletePopUP(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.delete_popup, null);
        final PopupWindow popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        TextView message = (TextView)container.findViewById(R.id.questionTextView);
        message.setText("Are you sure you want to delete the Event?");
        Button delete = (Button)container.findViewById(R.id.popUpDeleteButton);
        Button cancel = (Button)container.findViewById(R.id.popUpCancelButton);

        popupWindow.showAtLocation(popUpRelativeLayout, Gravity.CENTER, 0, 0);

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return false;
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                serviceHandler.getEventHandler().deleteEvent(new BaseFaroRequestCallback<String>() {
                    @Override
                    public void onFailure(Request request, IOException ex) {
                        Log.e(TAG, "failed to delete event");
                    }

                    @Override
                    public void onResponse(String s, HttpError error) {
                        if (error == null ) {
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    eventListHandler.removeEventFromListAndMap(eventId);
                                    popupWindow.dismiss();
                                    Toast.makeText(EditEventActivity.this, cloneEvent.getEventName() + " is Deleted", LENGTH_LONG).show();
                                    Toast.makeText(EditEventActivity.this, cloneEvent.getEventName() + " is Deleted", LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        }else {
                            Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                        }
                    }
                }, eventId);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void updateStartDateCalendarDate(Calendar calendar){
        startDateCalendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        startDateButton.setText(sdf.format(startDateCalendar.getTime()));
    }
    private void updateStartDateCalendarTime(Calendar calendar){
        startDateCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        startDateCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        startTimeButton.setText(stf.format(startDateCalendar.getTime()));
    }

    private void updateEndDateCalendarDate(Calendar calendar){
        endDateCalendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        endDateButton.setText(sdf.format(endDateCalendar.getTime()));
    }

    private void updateEndDateCalendarTime(Calendar calendar){
        endDateCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        endDateCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        endTimeButton.setText(stf.format(endDateCalendar.getTime()));
    }

    private void setBothTimeAndDateToEventDates(){
        updateStartDateCalendarDate(cloneEvent.getStartDate());
        updateStartDateCalendarTime(cloneEvent.getStartDate());
        updateEndDateCalendarDate(cloneEvent.getEndDate());
        updateEndDateCalendarTime(cloneEvent.getEndDate());
    }

    private void resetEndDateAndTimeToStartDateAndTime(){
        updateEndDateCalendarDate(startDateCalendar);
        updateEndDateCalendarTime(startDateCalendar);
    }

    private void setStartDate(){
        new DatePickerDialog(EditEventActivity.this,
                startDate,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setStartTime(){
        new TimePickerDialog(EditEventActivity.this,
                startTime,
                startDateCalendar.get(Calendar.HOUR_OF_DAY),
                startDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    private void setEndDate(){
        new DatePickerDialog(EditEventActivity.this,
                endDate,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setEndTime(){
        new TimePickerDialog(EditEventActivity.this,
                endTime,
                endDateCalendar.get(Calendar.HOUR_OF_DAY),
                endDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    //startDate of the event should not be before current time
    private boolean isStartDateValid(Calendar eventStartCalendar){
        boolean ret_value = eventStartCalendar.after(currentCalendar);
        if (!ret_value) {
            Toast.makeText(EditEventActivity.this, "Event's Start Date cannot be before current time", LENGTH_LONG).show();
        }
        return ret_value;
    }

    //End Date of the event cannot be before Start date
    private boolean isEndDateValid(Calendar eventEndCalendar){
        boolean ret_value = (startDateCalendar.before(eventEndCalendar) ||
                startDateCalendar.equals(eventEndCalendar));
        if (!ret_value){
            Toast.makeText(EditEventActivity.this, "Event's End Date cannot be before Event's Start date", LENGTH_LONG).show();
        }
        return ret_value;
    }

    private DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            Calendar temp = Calendar.getInstance();
            temp.set(year, monthOfYear, dayOfMonth,
                    startDateCalendar.get(Calendar.HOUR_OF_DAY),
                    startDateCalendar.get(Calendar.MINUTE));

            //Event start date cannot be before current time
            if (isStartDateValid(temp)) {
                updateStartDateCalendarDate(temp);
            }else{
                updateStartDateCalendarDate(currentCalendar);
            }

            /* When setting the startTime, we compare the startCalendar to the endCalendar and if the
             * startCalendar is after the endCalendar then we set the endTime same as the startTime
             */
            if (startDateCalendar.after(endDateCalendar)){
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };


    private TimePickerDialog.OnTimeSetListener startTime = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar temp = Calendar.getInstance();
            temp.set(startDateCalendar.get(Calendar.YEAR),
                    startDateCalendar.get(Calendar.MONTH),
                    startDateCalendar.get(Calendar.DAY_OF_MONTH),
                    hourOfDay, minute);

            //Event start date cannot be before current time
            if (isStartDateValid(temp)) {
                updateStartDateCalendarTime(temp);
            }else{
                updateStartDateCalendarTime(currentCalendar);
            }

            /* When setting the startTime, we compare the startCalendar to the endCalendar and if the
             * startCalendar is after the endCalendar then we set the endTime same as the startTime
             */
            if (startDateCalendar.after(endDateCalendar)){
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };


    private DatePickerDialog.OnDateSetListener endDate = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar temp = Calendar.getInstance();
            temp.set(year, monthOfYear, dayOfMonth,
                    endDateCalendar.get(Calendar.HOUR_OF_DAY),
                    endDateCalendar.get(Calendar.MINUTE));

            /* When setting the endDate, we compare the startCalendar to the temp and if the
             * startCalendar is after the endCalendar then we set the endDate same as the startDate
             */
            if(isEndDateValid(temp)){
                updateEndDateCalendarDate(temp);
            }else{
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };

    private TimePickerDialog.OnTimeSetListener endTime = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar temp = Calendar.getInstance();
            temp.set(endDateCalendar.get(Calendar.YEAR),
                    endDateCalendar.get(Calendar.MONTH),
                    endDateCalendar.get(Calendar.DAY_OF_MONTH),
                    hourOfDay, minute);

            /* When setting the endDate, we compare the startCalendar to the temp and if the
             * startCalendar is after the endCalendar then we set the endDate same as the startDate
             */
            if(isEndDateValid(temp)){
                updateEndDateCalendarTime(temp);
            }else{
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };

    @Override
    public void onBackPressed() {
        FaroIntentInfoBuilder.eventIntent(eventLandingPageIntent, eventId);
        startActivity(eventLandingPageIntent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if the version is same. It can be different if this page is loaded and a notification
        // is received for this later which updates the cache but clonedata on this page remains
        // stale.
        try {
            if (!eventListHandler.checkObjectVersionIfLatest(eventId, cloneEvent.getVersion())) {
                setupPageDetails();
            }
        } catch (FaroObjectNotFoundException e) {
            //Event has been deleted.
            Toast.makeText(this, "Event has been deleted", LENGTH_LONG).show();
            Log.e(TAG, MessageFormat.format("Event {0} has been deleted", eventId));
            finish();
        }

    }
}
