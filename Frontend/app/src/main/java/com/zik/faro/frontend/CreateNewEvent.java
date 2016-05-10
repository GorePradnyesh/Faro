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
import android.view.Menu;
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
    Intent AppLanding = null;
    Intent EventListPage = null;


    Calendar startDateCalendar = Calendar.getInstance();
    Calendar endDateCalendar = Calendar.getInstance();
    Button startDateButton = null;
    Button startTimeButton = null;
    Button endTimeButton = null;
    Button endDateButton = null;

    private DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private RadioGroup inviteStatusRadioGroup = null;

    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;
    private static String TAG = "CreateNewEvent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        final EditText eventName = (EditText) findViewById(R.id.eventNameTextEdit);
        final EditText eventDescription = (EditText) findViewById(R.id.eventDescriptionEditText);

        //TODO Remove the below added just to test creator EventLanding Page vs Friend EventLandingPage
        final CheckBox eventCreator = (CheckBox) findViewById(R.id.eventCreatorCheckBox);

        inviteStatusRadioGroup = (RadioGroup) findViewById(R.id.inviteStatusRadioGroup);
        final RadioButton acceptedRadio = new RadioButton(this);
        acceptedRadio.setText("Accepted");
        final RadioButton noResponseRadio = new RadioButton(this);
        noResponseRadio.setText("No Response");
        final RadioButton maybeRadio = new RadioButton(this);
        maybeRadio.setText("Maybe");
        inviteStatusRadioGroup.addView(acceptedRadio);
        inviteStatusRadioGroup.addView(noResponseRadio);
        inviteStatusRadioGroup.addView(maybeRadio);
        inviteStatusRadioGroup.check(inviteStatusRadioGroup.getChildAt(1).getId());

        startDateButton = (Button) findViewById(R.id.startDateButton);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);

        endDateButton = (Button) findViewById(R.id.endDateButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);

        final CheckBox controlFlagCheckBox = (CheckBox)findViewById(R.id.controlFlag);

        final Button createNewEventOK = (Button) findViewById(R.id.createNewEventOK);

        final Intent eventLanding = new Intent(CreateNewEvent.this, EventLandingPage.class);
        AppLanding = new Intent(CreateNewEvent.this, EventListPage.class);
        EventListPage = new Intent(CreateNewEvent.this, EventListPage.class);

        final Context mContext = this.getApplicationContext();


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

        eventCreator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventCreator.isChecked()) {
                    inviteStatusRadioGroup.setClickable(false);
                } else {
                    inviteStatusRadioGroup.setClickable(true);
                }
            }
        });

        eventCreator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (eventCreator.isChecked()) {
                    for (int i = 0; i < inviteStatusRadioGroup.getChildCount(); i++) {
                        inviteStatusRadioGroup.getChildAt(i).setEnabled(false);
                    }
                } else {
                    for (int i = 0; i < inviteStatusRadioGroup.getChildCount(); i++) {
                        inviteStatusRadioGroup.getChildAt(i).setEnabled(true);
                    }
                    inviteStatusRadioGroup.check(inviteStatusRadioGroup.getChildAt(1).getId());
                }
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
                                    eventListHandler.addEventToListAndMap(receivedEvent);
                                    eventLanding.putExtra("eventID", receivedEvent.getEventId());
                                    startActivity(eventLanding);
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

    public void updateStartDate(){
        startDateButton.setText(sdf.format(startDateCalendar.getTime()));
    }

    public void updateEndDate(){
        endDateButton.setText(sdf.format(endDateCalendar.getTime()));
    }

    public void updateStartTime(){
        startTimeButton.setText(stf.format(startDateCalendar.getTime()));
    }

    public void updateEndTime(){
        endTimeButton.setText(stf.format(endDateCalendar.getTime()));
    }


    public void setStartDate(){
        new DatePickerDialog(CreateNewEvent.this,
                startDate,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    public void setEndDate(){
        new DatePickerDialog(CreateNewEvent.this,
                endDate,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    public void setStartTime(){
        new TimePickerDialog(CreateNewEvent.this,
                startTime,
                startDateCalendar.get(Calendar.HOUR_OF_DAY),
                startDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    public void setEndTime(){
        new TimePickerDialog(CreateNewEvent.this,
                endTime,
                endDateCalendar.get(Calendar.HOUR_OF_DAY),
                endDateCalendar.get(Calendar.MINUTE),
                false).show();
    }


    /* When setting the startDate, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endDate same as the startDate
    */
    DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener(){

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
    TimePickerDialog.OnTimeSetListener startTime = new TimePickerDialog.OnTimeSetListener(){
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

    DatePickerDialog.OnDateSetListener endDate = new DatePickerDialog.OnDateSetListener(){

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
    TimePickerDialog.OnTimeSetListener endTime = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            endDateCalendar.set(endDateCalendar.HOUR_OF_DAY, hourOfDay);
            endDateCalendar.set(endDateCalendar.MINUTE, minute);

            if(startDateCalendar.after(endDateCalendar)){
                resetEndDateAndTimeToStartDateAndTime();
            }else {
                updateEndTime();
            }
        }
    };

    @Override
    public void onBackPressed() {
        startActivity(EventListPage);
        finish();
        super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_new_event, menu);
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
