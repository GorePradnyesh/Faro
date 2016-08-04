package com.zik.faro.frontend;

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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.Activity;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

public class CreateNewActivity extends android.app.Activity{

    private static String eventID = null;
    static EventListHandler eventListHandler = EventListHandler.getInstance();
    static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    Event event = null;

    Calendar startDateCalendar = Calendar.getInstance();
    Calendar endDateCalendar = Calendar.getInstance();
    Button startDateButton = null;
    Button startTimeButton = null;
    Button endTimeButton = null;
    Button endDateButton = null;

    private DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");

    Intent activityListPage = null;

    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;
    private static String TAG = "CreateNewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_activity);

        final EditText activityName = (EditText) findViewById(R.id.activityName);
        final EditText activityDescription = (EditText) findViewById(R.id.activityDescription);

        final Button createNewActivityOK = (Button) findViewById(R.id.createNewActivityOK);

        startDateButton = (Button) findViewById(R.id.startDateButton);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);

        endDateButton = (Button) findViewById(R.id.endDateButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);

        activityListPage = new Intent(CreateNewActivity.this, ActivityListPage.class);
        final Intent activityLanding  = new Intent(CreateNewActivity.this, ActivityLandingPage.class);

        final Context mContext = this;

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            event = eventListHandler.getEventCloneFromMap(eventID);
        }

        activityName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                createNewActivityOK.setEnabled(!(activityName.getText().toString().trim().isEmpty()));

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        activityDescription.addTextChangedListener(new TextWatcher() {
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

        createNewActivityOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activity_Name = activityName.getText().toString();
                String activity_Desc = activityDescription.getText().toString();

                final Activity eventActivity = new Activity(eventID,
                        activity_Name,
                        activity_Desc,
                        null,
                        startDateCalendar,
                        endDateCalendar,
                        null);

                serviceHandler.getActivityHandler().createActivity(new BaseFaroRequestCallback<Activity>() {
                    @Override
                    public void onFailure(Request request, IOException ex) {
                        Log.e(TAG, "failed to send activity create request");
                    }

                    @Override
                    public void onResponse(final Activity receivedActivity, HttpError error) {
                        if (error == null ) {
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    //Since update to server successful, adding activity to List and Map below
                                    Log.i(TAG, "Activity Create Response received Successfully");
                                    activityListHandler.addActivityToListAndMap(receivedActivity);
                                    activityLanding.putExtra("eventID", eventID);
                                    activityLanding.putExtra("activityID", receivedActivity.getId());
                                    startActivity(activityLanding);
                                    finish();
                                }
                            };
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(myRunnable);
                        } else {
                            Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        }
                    }
                }, eventID, eventActivity);
            }
        });

        //TODO: User should be allowed to set date and time only within the events date and time
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

        setBothTimeAndDateToEventStartTimeAndDate();

    }

    private void setBothTimeAndDateToEventStartTimeAndDate(){
        startDateButton.setText(sdf.format(event.getStartDate().getTime()));
        endDateButton.setText(sdf.format(event.getStartDate().getTime()));
        startTimeButton.setText(stf.format(event.getStartDate().getTime()));
        endTimeButton.setText(stf.format(event.getStartDate().getTime()));
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
        new DatePickerDialog(CreateNewActivity.this,
                startDate,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void setEndDate(){
        new DatePickerDialog(CreateNewActivity.this,
                endDate,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    public void setStartTime(){
        new TimePickerDialog(CreateNewActivity.this,
                startTime,
                startDateCalendar.get(Calendar.HOUR_OF_DAY),
                startDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    public void setEndTime(){
        new TimePickerDialog(CreateNewActivity.this,
                endTime,
                endDateCalendar.get(Calendar.HOUR_OF_DAY),
                endDateCalendar.get(Calendar.MINUTE),
                false).show();
    }


    /*
     * Activity Start Date should lie within the range of the event dates and before the activity End
     * Date
     */
    public boolean isStartDateValid(){
        return startDateCalendar.after(event.getStartDate()) &&
                startDateCalendar.before(event.getEndDate()) &&
                startDateCalendar.before(endDateCalendar);

    }

    /*
     * Activity End Date should lie within the range of the event dates and after the activity Start
     * Date
     */
    public boolean isEndDateValid(){
        return endDateCalendar.after(event.getStartDate()) &&
                endDateCalendar.before(event.getEndDate()) &&
                startDateCalendar.before(endDateCalendar);

    }

    /* When setting the startDate, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endDate same as the startDate
    */
    DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            if(isStartDateValid()) {
                startDateCalendar.set(Calendar.YEAR, year);
                startDateCalendar.set(Calendar.MONTH, monthOfYear);
                startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }else{
                startDateCalendar.set(Calendar.YEAR, event.getStartDate().get(Calendar.YEAR));
                startDateCalendar.set(Calendar.MONTH, event.getStartDate().get(Calendar.MONTH));
                startDateCalendar.set(Calendar.DAY_OF_MONTH, event.getStartDate().get(Calendar.DAY_OF_MONTH));
            }
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

            if(isStartDateValid()) {
                startDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startDateCalendar.set(Calendar.MINUTE, minute);
            }else{
                startDateCalendar.set(Calendar.HOUR_OF_DAY, event.getStartDate().get(Calendar.HOUR_OF_DAY));
                startDateCalendar.set(Calendar.MINUTE, event.getStartDate().get(Calendar.MINUTE));
            }
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

            if(isEndDateValid()){
                endDateCalendar.set(Calendar.YEAR, year);
                endDateCalendar.set(Calendar.MONTH, monthOfYear);
                endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateEndDate();
            }else{
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };

    /* When setting the endTime, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endTime same as the startTime
    */
    TimePickerDialog.OnTimeSetListener endTime = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if(isEndDateValid()) {
                endDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endDateCalendar.set(Calendar.MINUTE, minute);
                updateEndTime();
            }else{
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };
}
