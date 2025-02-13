package com.zik.faro.frontend.ui.activities;

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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.Activity;
import com.zik.faro.frontend.handlers.ActivityListHandler;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.util.FaroExceptionHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import static android.widget.Toast.LENGTH_LONG;

public class CreateNewActivity extends android.app.Activity {

    private String eventId = null;
    private EventListHandler eventListHandler = EventListHandler.getInstance(this);
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private Event event = null;

    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private Button startDateButton = null;
    private Button startTimeButton = null;
    private Button endTimeButton = null;
    private Button endDateButton = null;

    private DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");

    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private static String TAG = "CreateNewActivity";

    private Context mContext;

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


        final Intent activityLanding  = new Intent(CreateNewActivity.this, ActivityLandingPage.class);

        mContext = this;

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventId = extras.getString(FaroIntentConstants.EVENT_ID);
            try {
                event = eventListHandler.getCloneObject(eventId);
            } catch (FaroObjectNotFoundException e) {
                Toast.makeText(this, "Event has been deleted", LENGTH_LONG).show();
                Log.i(TAG, MessageFormat.format("Event {0} has been deleted", eventId));
                finish();
                return;
            }
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

                final Activity eventActivity = new Activity(eventId,
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
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //Since update to server successful, adding activity to List and Map below
                                    Log.i(TAG, "Activity Create Response received Successfully");
                                    activityListHandler.addActivityToListAndMap(eventId, receivedActivity, mContext);
                                    FaroIntentInfoBuilder.activityIntent(activityLanding, eventId, receivedActivity.getId());
                                    startActivity(activityLanding);
                                    finish();
                                }
                            });
                        } else {
                            Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                        }
                    }
                }, eventId, eventActivity);
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

        setBothTimeAndDateToEventStartTimeAndDate();

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

    private void setBothTimeAndDateToEventStartTimeAndDate(){
        updateStartDateCalendarDate(event.getStartDate());
        updateStartDateCalendarTime(event.getStartDate());
        updateEndDateCalendarDate(event.getStartDate());
        updateEndDateCalendarTime(event.getStartDate());
    }

    private void resetEndDateAndTimeToStartDateAndTime(){
        updateEndDateCalendarDate(startDateCalendar);
        updateEndDateCalendarTime(startDateCalendar);
    }


    private void setStartDate(){
        new DatePickerDialog(CreateNewActivity.this,
                startDate,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setStartTime(){
        new TimePickerDialog(CreateNewActivity.this,
                startTime,
                startDateCalendar.get(Calendar.HOUR_OF_DAY),
                startDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    private void setEndDate(){
        new DatePickerDialog(CreateNewActivity.this,
                endDate,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setEndTime(){
        new TimePickerDialog(CreateNewActivity.this,
                endTime,
                endDateCalendar.get(Calendar.HOUR_OF_DAY),
                endDateCalendar.get(Calendar.MINUTE),
                false).show();
    }


    /*
     * Activity Start Date should lie within the range of the event dates
     */
    private boolean isStartDateValid(Calendar activityStartCalendar){
        boolean ret_val1 = (activityStartCalendar.after(event.getStartDate()) ||
                activityStartCalendar.equals(event.getStartDate()));
        boolean ret_val2 = activityStartCalendar.before(event.getEndDate());
        if (!ret_val1){
            Toast.makeText(CreateNewActivity.this, "Activity's Start Date cannot be before Event's Start Date", LENGTH_LONG).show();
        }else if (!ret_val2){
            Toast.makeText(CreateNewActivity.this, "Activity's Start Date cannot be on/after Event's End Date", LENGTH_LONG).show();
        }
        return (ret_val1 && ret_val2);
    }

    /*
     * Activity End Date should lie within the range of the event dates and after the activity Start
     * Date
     */
    private boolean isEndDateValid(Calendar activityEndCalendar){
        boolean ret_val1 = (startDateCalendar.before(activityEndCalendar) ||
                startDateCalendar.equals(activityEndCalendar));
        boolean ret_val2 = (activityEndCalendar.before(event.getEndDate()) ||
                activityEndCalendar.equals(event.getEndDate()));

        if (!ret_val1){
            Toast.makeText(CreateNewActivity.this, "Activity's End Date cannot be before Activity's Start Date", LENGTH_LONG).show();
        }else if (!ret_val2){
            Toast.makeText(CreateNewActivity.this, "Activity's End Date cannot be after Event's End Date", LENGTH_LONG).show();
        }
        return  (ret_val2 && ret_val1);
    }

    /* When setting the startDate, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endDate same as the startDate
    */
    private DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar temp = Calendar.getInstance();
            temp.set(year, monthOfYear, dayOfMonth,
                    startDateCalendar.get(Calendar.HOUR_OF_DAY),
                    startDateCalendar.get(Calendar.MINUTE));

            //If the selected startDate is not valid then reset it to the event's startDate.
            if (isStartDateValid(temp)){
                updateStartDateCalendarDate(temp);
            }else{
                updateStartDateCalendarDate(event.getStartDate());
            }

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
            Calendar temp = Calendar.getInstance();
            temp.set(startDateCalendar.get(Calendar.YEAR),
                    startDateCalendar.get(Calendar.MONTH),
                    startDateCalendar.get(Calendar.DAY_OF_MONTH),
                    hourOfDay, minute);

            //If the selected startDate is not valid then reset it to the event's startDate.
            if (isStartDateValid(temp)){
                updateStartDateCalendarTime(temp);
            }else{
                updateStartDateCalendarTime(event.getStartDate());
            }

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
            Calendar temp = Calendar.getInstance();
            temp.set(year, monthOfYear, dayOfMonth,
                    endDateCalendar.get(Calendar.HOUR_OF_DAY),
                    endDateCalendar.get(Calendar.MINUTE));

            //If the selected endDate is not valid then reset it to the activity's startDate.
            if(isEndDateValid(temp)){
                updateEndDateCalendarDate(temp);
            }else{
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };

    /* When setting the endTime, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endTime same as the startTime
    */
    private TimePickerDialog.OnTimeSetListener endTime = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar temp = Calendar.getInstance();
            temp.set(endDateCalendar.get(Calendar.YEAR),
                    endDateCalendar.get(Calendar.MONTH),
                    endDateCalendar.get(Calendar.DAY_OF_MONTH),
                    hourOfDay, minute);

            //If the selected endDate is not valid then reset it to the activity's startDate.
            if(isEndDateValid(temp)){
                updateEndDateCalendarTime(temp);
            }else{
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };
}
