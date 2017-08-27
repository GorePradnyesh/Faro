package com.zik.faro.frontend.ui.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.data.Location;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.util.FaroExceptionHandler;
import com.zik.faro.frontend.util.GetLocationAddressString;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;

import static android.widget.Toast.LENGTH_LONG;


/*
 * On this page an Event can be created with certain basic requirements like the Event Name, Start
 * Date and Time and End Date and Time as mandatory requirements.
 * Other fields like Invitees, Location, Description of the event are optional which can be edited
 * later from the Event Landing Page.
 * The Start Date/Time and the End Date/Time will be set to the current Date/Time by default.
 * Checks are made to make sure that the Start Date/Time is not set after the End Date/Time.
 *
 */
public class CreateNewEventActivity extends Activity {

    //public static final int NO_CHANGES = 0;
    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private Calendar currentCalendar = Calendar.getInstance();
    private Button startDateButton = null;
    private Button startTimeButton = null;
    private Button endTimeButton = null;
    private Button endDateButton = null;
    private ImageButton deleteLocation = null;

    private TextView eventAddress = null;

    private DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");

    private static String TAG = "CreateNewEventActivity";

    private int PLACE_PICKER_REQUEST = 1;

    private Activity mActivity = this;

    private Location location = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        final EditText eventName = (EditText) findViewById(R.id.eventNameTextEdit);
        final EditText eventDescription = (EditText) findViewById(R.id.eventDescriptionEditText);
        eventAddress = (TextView) findViewById(R.id.locationAddressTextView);
        deleteLocation = (ImageButton) findViewById(R.id.deleteLocation);
        deleteLocation.setImageResource(R.drawable.cancel);
        deleteLocation.setVisibility(View.GONE);

        startDateButton = (Button) findViewById(R.id.startDateButton);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);

        endDateButton = (Button) findViewById(R.id.endDateButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);

        final CheckBox controlFlagCheckBox = (CheckBox)findViewById(R.id.controlFlag);

        final Button createNewEventOK = (Button) findViewById(R.id.createNewEventOK);

        final Intent EventLanding = new Intent(CreateNewEventActivity.this, EventLandingPage.class);

        final Context mContext = this;


        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        eventName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                createNewEventOK.setEnabled(!(eventName.getText().toString().trim().isEmpty()));

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        controlFlagCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Implement popup: This flag once set cannot be changed.
            }
        });


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

        eventDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        createNewEventOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String event_Name = eventName.getText().toString();
                String eventDesc = eventDescription.getText().toString();
                FaroUserContext faroUserContext = FaroUserContext.getInstance();
                String eventCreatorId = faroUserContext.getEmail();


                final Event event = new Event(event_Name,
                        startDateCalendar,
                        endDateCalendar,
                        controlFlagCheckBox.isChecked(),
                        eventDesc,
                        null,
                        location,
                        eventCreatorId);


                serviceHandler.getEventHandler().createEvent(new BaseFaroRequestCallback<Event>() {
                    @Override
                    public void onFailure(Request request, IOException ex) {
                        Log.e(TAG, "failed to send event create request");
                    }

                    @Override
                    public void onResponse(final Event receivedEvent, HttpError error) {
                        if (error == null ) {
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    //Since update to server successful, adding event to List and Map below
                                    Log.i(TAG, "Event Create Response received Successfully");
                                    eventListHandler.addEventToListAndMap(receivedEvent, EventInviteStatus.ACCEPTED);
                                    FaroIntentInfoBuilder.eventIntent(EventLanding, receivedEvent.getId());
                                    startActivity(EventLanding);
                                    finish();
                                }
                            };
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(myRunnable);
                        } else {
                            Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        }
                    }
                }, event);
            }
        });

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

        setBothTimeAndDateToDefault();
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

                if (place.getAddress() != null && !place.getAddress().equals("")){
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

    private void setBothTimeAndDateToDefault(){
        Calendar temp = Calendar.getInstance();
        updateStartDateCalendarDate(temp);
        updateStartDateCalendarTime(temp);
        updateEndDateCalendarDate(temp);
        updateEndDateCalendarTime(temp);
    }

    private void resetEndDateAndTimeToStartDateAndTime(){
        updateEndDateCalendarDate(startDateCalendar);
        updateEndDateCalendarTime(startDateCalendar);
    }

    private void setStartDate(){
        new DatePickerDialog(CreateNewEventActivity.this,
                startDate,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setStartTime(){
        new TimePickerDialog(CreateNewEventActivity.this,
                startTime,
                startDateCalendar.get(Calendar.HOUR_OF_DAY),
                startDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    private void setEndDate(){
        new DatePickerDialog(CreateNewEventActivity.this,
                endDate,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setEndTime(){
        new TimePickerDialog(CreateNewEventActivity.this,
                endTime,
                endDateCalendar.get(Calendar.HOUR_OF_DAY),
                endDateCalendar.get(Calendar.MINUTE),
                false).show();
    }


    //startDate of the event should not be before current time
    private boolean isStartDateValid(Calendar eventStartCalendar){
        boolean ret_value = eventStartCalendar.after(currentCalendar);
        if (!ret_value) {
            Toast.makeText(CreateNewEventActivity.this, "Event's Start Date cannot be before current time", LENGTH_LONG).show();
        }
        return ret_value;
    }

    //End Date of the event cannot be before Start date
    private boolean isEndDateValid(Calendar eventEndCalendar){
        boolean ret_value = (startDateCalendar.before(eventEndCalendar) ||
                startDateCalendar.equals(eventEndCalendar));
        if (!ret_value){
            Toast.makeText(CreateNewEventActivity.this, "Event's End Date cannot be before Event's Start date", LENGTH_LONG).show();
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
}
