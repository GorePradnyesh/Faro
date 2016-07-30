package com.zik.faro.frontend;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.user.InviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;


/*
 * On this page an Event can be created with certain basic requirements like the Event Name, Start
 * Date and Time and End Date and Time as mandatory requirements.
 * Other fields like Invitees, Location, Description of the event are optional which can be edited
 * later from the Event Landing Page.
 * The Start Date/Time and the End Date/Time will be set to the current Date/Time by default.
 * Checks are made to make sure that the Start Date/Time is not set after the End Date/Time.
 *
 */
public class CreateNewEvent extends Activity {

    //public static final int NO_CHANGES = 0;
    static EventListHandler eventListHandler = EventListHandler.getInstance();

    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private Button startDateButton = null;
    private Button startTimeButton = null;
    private Button endTimeButton = null;
    private Button endDateButton = null;

    private DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");

    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;
    private static String TAG = "CreateNewEvent";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        final EditText eventName = (EditText) findViewById(R.id.eventNameTextEdit);
        final EditText eventDescription = (EditText) findViewById(R.id.eventDescriptionEditText);

        startDateButton = (Button) findViewById(R.id.startDateButton);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);

        endDateButton = (Button) findViewById(R.id.endDateButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);

        final CheckBox controlFlagCheckBox = (CheckBox)findViewById(R.id.controlFlag);

        final Button createNewEventOK = (Button) findViewById(R.id.createNewEventOK);

        final Intent EventLanding = new Intent(CreateNewEvent.this, EventLandingPage.class);

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
                        null,
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
                                    eventListHandler.addEventToListAndMap(receivedEvent, InviteStatus.ACCEPTED);
                                    EventLanding.putExtra("eventID", receivedEvent.getEventId());
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

    private void setBothTimeAndDateToDefault(){
        startDateButton.setText(sdf.format(startDateCalendar.getTime()));
        endDateButton.setText(sdf.format(startDateCalendar.getTime()));
        startTimeButton.setText(stf.format(startDateCalendar.getTime()));
        endTimeButton.setText(stf.format(startDateCalendar.getTime()));
    }

    private void resetEndDateAndTimeToStartDateAndTime(){
        endDateCalendar.set(Calendar.YEAR, startDateCalendar.get(Calendar.YEAR));
        endDateCalendar.set(Calendar.MONTH, startDateCalendar.get(Calendar.MONTH));
        endDateCalendar.set(Calendar.DAY_OF_MONTH, startDateCalendar.get(Calendar.DAY_OF_MONTH));
        endDateCalendar.set(Calendar.HOUR_OF_DAY, startDateCalendar.get(Calendar.HOUR_OF_DAY));
        endDateCalendar.set(Calendar.MINUTE, startDateCalendar.get(Calendar.MINUTE));
        updateEndDate();
        updateEndTime();
    }

    private void updateStartDate(){
        startDateButton.setText(sdf.format(startDateCalendar.getTime()));
    }

    private void updateEndDate(){
        endDateButton.setText(sdf.format(endDateCalendar.getTime()));
    }

    private void updateStartTime(){
        startTimeButton.setText(stf.format(startDateCalendar.getTime()));
    }

    private void updateEndTime(){
        endTimeButton.setText(stf.format(endDateCalendar.getTime()));
    }


    private void setStartDate(){
        new DatePickerDialog(CreateNewEvent.this,
                startDate,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setEndDate(){
        new DatePickerDialog(CreateNewEvent.this,
                endDate,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setStartTime(){
        new TimePickerDialog(CreateNewEvent.this,
                startTime,
                startDateCalendar.get(Calendar.HOUR_OF_DAY),
                startDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    private void setEndTime(){
        new TimePickerDialog(CreateNewEvent.this,
                endTime,
                endDateCalendar.get(Calendar.HOUR_OF_DAY),
                endDateCalendar.get(Calendar.MINUTE),
                false).show();
    }


    /* When setting the startDate, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endDate same as the startDate
    */
    private DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            startDateCalendar.set(Calendar.YEAR, year);
            startDateCalendar.set(Calendar.MONTH, monthOfYear);
            startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateStartDate();

            if(startDateCalendar.after(endDateCalendar)){
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };

    /* When setting the startTime, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endTime same as the startTime
    */
    private TimePickerDialog.OnTimeSetListener startTime = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            startDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            startDateCalendar.set(Calendar.MINUTE, minute);
            updateStartTime();

            if(startDateCalendar.after(endDateCalendar)) {
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };

    /* When setting the endDate, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endDate same as the startDate
    */

    private DatePickerDialog.OnDateSetListener endDate = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            endDateCalendar.set(Calendar.YEAR, year);
            endDateCalendar.set(Calendar.MONTH, monthOfYear);
            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            if(startDateCalendar.after(endDateCalendar)){
                resetEndDateAndTimeToStartDateAndTime();
            }else {
                updateEndDate();
            }


        }
    };

    /* When setting the endTime, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endTime same as the startTime
    */
    private TimePickerDialog.OnTimeSetListener endTime = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            endDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            endDateCalendar.set(Calendar.MINUTE, minute);

            if(startDateCalendar.after(endDateCalendar)){
                resetEndDateAndTimeToStartDateAndTime();
            }else {
                updateEndTime();
            }
        }
    };

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
