package com.zik.faro.frontend;

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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import static android.widget.Toast.LENGTH_LONG;


/*
* In this class we update the existing event object with the updated information that the user
* inputs.
* Special care should be taken that the updates should not reflect in the Event object until the
* user presses the OK button. Until then simply collect all the updated information in local
* variables.*/
public class EditEvent extends Activity {

    private Button startDateButton = null;
    private Button startTimeButton = null;
    private Button endTimeButton = null;
    private Button endDateButton = null;
    private ImageButton deleteLocation = null;

    private TextView eventAddress = null;

    private DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");


    private  static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static FaroServiceHandler serviceHandler;
    private static Event cloneEvent;

    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private Calendar currentCalendar = Calendar.getInstance();

    private RelativeLayout popUpRelativeLayout;

    private String eventID;
    private static String TAG = "EditEvent";

    Intent EventLanding = null;

    private int PLACE_PICKER_REQUEST = 1;

    final Context mContext = this;
    private Activity mActivity = this;

    private Location location = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        serviceHandler = eventListHandler.serviceHandler;

        startDateButton = (Button) findViewById(R.id.startDateButton);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);
        endDateButton = (Button) findViewById(R.id.endDateButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);
        deleteLocation = (ImageButton) findViewById(R.id.deleteLocation);
        deleteLocation.setImageResource(R.drawable.cancel);
        deleteLocation.setVisibility(View.GONE);

        TextView eventName = (TextView) findViewById(R.id.eventName);
        final EditText eventDescription = (EditText) findViewById(R.id.eventDescriptionEditText);
        eventAddress = (TextView) findViewById(R.id.locationAddressTextView);

        Button editEventOK = (Button) findViewById(R.id.editEventOK);
        Button deleteEventButton = (Button) findViewById(R.id.deleteEvent);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.editEventPage);

        EventLanding = new Intent(EditEvent.this, EventLandingPage.class);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        /*
        * Do not remove the event from the list and the map in the eventListHandler below.
        * Make a clone of the event and make changes to that.
        * When the OK button is clicked, only then remove the event and insert the clone.
        * This way if the user makes some changes but then clicks back without clicking OK button,
        * the original event is not affected.
        */
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            cloneEvent = eventListHandler.getEventCloneFromMap(eventID);
            if (cloneEvent != null) {
                String ev_name = cloneEvent.getEventName();
                eventName.setText(ev_name);
                eventDescription.setText(cloneEvent.getEventDescription());
                setBothTimeAndDateToEventDates();
            }
        }

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

                serviceHandler.getEventHandler().updateEvent(new BaseFaroRequestCallback<Event>() {
                    @Override
                    public void onFailure(Request request, IOException ex) {
                        Log.e(TAG, "failed to send event create request");
                    }

                    @Override
                    public void onResponse(final Event receivedEvent, HttpError error) {
                        if (error == null ) {
                            //Since update to server successful, adding event to List and Map below
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    //Since update to server successful, adding event to List and Map below
                                    Log.i(TAG, "Event Create Response received Successfully");
                                    eventListHandler.addEventToListAndMap(receivedEvent, EventInviteStatus.ACCEPTED);
                                    EventLanding.putExtra("eventID", eventID);
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
                }, eventID, cloneEvent);
            }
        });

        deleteEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmEventDeletePopUP(v);
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
                cloneEvent.setEventDescription(eventDescription.getText().toString());
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
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    eventListHandler.removeEventFromListAndMap(eventID);
                                    popupWindow.dismiss();
                                    Toast.makeText(EditEvent.this, cloneEvent.getEventName() + "is Deleted", LENGTH_LONG).show();
                                    finish();
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
        new DatePickerDialog(EditEvent.this,
                startDate,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setStartTime(){
        new TimePickerDialog(EditEvent.this,
                startTime,
                startDateCalendar.get(Calendar.HOUR_OF_DAY),
                startDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    private void setEndDate(){
        new DatePickerDialog(EditEvent.this,
                endDate,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setEndTime(){
        new TimePickerDialog(EditEvent.this,
                endTime,
                endDateCalendar.get(Calendar.HOUR_OF_DAY),
                endDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    //startDate of the event should not be before current time
    private boolean isStartDateValid(Calendar eventStartCalendar){
        boolean ret_value = eventStartCalendar.after(currentCalendar);
        if (!ret_value) {
            Toast.makeText(EditEvent.this, "Event's Start Date cannot be before current time", LENGTH_LONG).show();
        }
        return ret_value;
    }

    //End Date of the event cannot be before Start date
    private boolean isEndDateValid(Calendar eventEndCalendar){
        boolean ret_value = (startDateCalendar.before(eventEndCalendar) ||
                startDateCalendar.equals(eventEndCalendar));
        if (!ret_value){
            Toast.makeText(EditEvent.this, "Event's End Date cannot be before Event's Start date", LENGTH_LONG).show();
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
        EventLanding.putExtra("eventID", eventID);
        startActivity(EventLanding);
        finish();
        super.onBackPressed();
    }
}
