package com.zik.faro.frontend;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.widget.Toast.LENGTH_LONG;

public class EditActivity extends ActionBarActivity {

    private String eventID = null;
    private String activityID = null;
    private static Event event;
    private static Activity cloneActivity;
    private  static EventListHandler eventListHandler = EventListHandler.getInstance();
    private  static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;

    Button startDateButton = null;
    Button startTimeButton = null;
    Button endTimeButton = null;
    Button endDateButton = null;

    DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    DateFormat stf = new SimpleDateFormat("hh:mm a");

    private RelativeLayout popUpRelativeLayout;

    private static String TAG = "EditActivity";

    Intent ActivityLandingPage;
    Intent ActivityListPage;

    final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activity);

        startDateButton = (Button) findViewById(R.id.startDateButton);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);
        endDateButton = (Button) findViewById(R.id.endDateButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);

        Button editActivityOK = (Button) findViewById(R.id.editActivityOK);
        Button deleteActivity = (Button) findViewById(R.id.deleteActivity);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.editActivity);

        ActivityLandingPage = new Intent(EditActivity.this, ActivityLandingPage.class);
        ActivityListPage = new Intent(EditActivity.this, ActivityListPage.class);

        final EditText activityDescription = (EditText) findViewById(R.id.activityDescriptionEditText);;

        /*
        * Do not remove the cloneactivity from the list and the map in the activityListHandler below.
        * Make a clone of the cloneactivity and make changes to that.
        * When the OK button is clicked, only then remove the cloneactivity and insert the clone.
        * This way if the user makes some changes but then clicks back without clicking OK button,
        * the original cloneactivity is not affected.
        */
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            activityID = extras.getString("activityID");
            event = eventListHandler.getEventCloneFromMap(eventID);
            cloneActivity = activityListHandler.getActivityCloneFromMap(activityID);

            if (cloneActivity != null) {

                TextView activityName = (TextView) findViewById(R.id.activityName);
                activityName.setText(cloneActivity.getName());
                
                activityDescription.setText(cloneActivity.getDescription());
                setBothTimeAndDateToActivityDates();
            }
        }



        activityDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                cloneActivity.setDescription(activityDescription.getText().toString());
            }
        });

        /*
        * Since OK button is pressed we will remove the cloneactivity from the list and insert the clone.
        */
        editActivityOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                serviceHandler.getActivityHandler().updateActivity(new BaseFaroRequestCallback<String>() {
                    @Override
                    public void onFailure(Request request, IOException ex) {
                        Log.e(TAG, "failed to update cloneactivity");
                    }

                    @Override
                    public void onResponse(String s, HttpError error) {
                        if (error == null ) {
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    activityListHandler.addActivityToListAndMap(cloneActivity);
                                    ActivityLandingPage.putExtra("eventID", event.getEventId());
                                    ActivityLandingPage.putExtra("activityID", cloneActivity.getId());
                                    startActivity(ActivityLandingPage);
                                    finish();
                                }
                            };
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(myRunnable);
                        }else {
                            Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        }
                    }
                }, eventID, activityID, cloneActivity);
            }
        });

        deleteActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmActivityDeletePopUP(v);
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
    }

    public void confirmActivityDeletePopUP(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.delete_popup, null);
        final PopupWindow popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        TextView message = (TextView)container.findViewById(R.id.questionTextView);
        message.setText("Are you sure you want to delete the Activity?");
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

                serviceHandler.getActivityHandler().deleteActivity(new BaseFaroRequestCallback<String>() {
                    @Override
                    public void onFailure(Request request, IOException ex) {
                        Log.e(TAG, "failed to delete cloneactivity");
                    }

                    @Override
                    public void onResponse(String s, HttpError error) {
                        if (error == null ) {
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    activityListHandler.removeActivityFromListAndMap(activityID);
                                    popupWindow.dismiss();
                                    Toast.makeText(EditActivity.this, cloneActivity.getName() + "is Deleted", LENGTH_LONG).show();
                                    finish();
                                }
                            };
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(myRunnable);
                        }else {
                            Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        }
                    }
                }, eventID, activityID);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void setBothTimeAndDateToActivityDates(){
        startDateButton.setText(sdf.format(cloneActivity.getStartDate().getTime()));
        endDateButton.setText(sdf.format(cloneActivity.getEndDate().getTime()));
        startTimeButton.setText(stf.format(cloneActivity.getStartDate().getTime()));
        endTimeButton.setText(stf.format(cloneActivity.getEndDate().getTime()));
    }

    public void updateStartDateButton(){
        startDateButton.setText(sdf.format(cloneActivity.getStartDate().getTime()));
    }

    public void updateEndDateButton(){
        endDateButton.setText(sdf.format(cloneActivity.getEndDate().getTime()));
    }

    public void updateStartTimeButton(){
        startTimeButton.setText(stf.format(cloneActivity.getStartDate().getTime()));
    }

    public void updateEndTimeButton(){
        endTimeButton.setText(stf.format(cloneActivity.getEndDate().getTime()));
    }

    private void resetEndDateAndTimeToStartDateAndTime(){
        cloneActivity.getEndDate().set(Calendar.YEAR, cloneActivity.getStartDate().get(Calendar.YEAR));
        cloneActivity.getEndDate().set(Calendar.MONTH, cloneActivity.getStartDate().get(Calendar.MONTH));
        cloneActivity.getEndDate().set(Calendar.DAY_OF_MONTH, cloneActivity.getStartDate().get(Calendar.DAY_OF_MONTH));
        cloneActivity.getEndDate().set(Calendar.HOUR_OF_DAY, cloneActivity.getStartDate().get(Calendar.HOUR_OF_DAY));
        cloneActivity.getEndDate().set(Calendar.MINUTE, cloneActivity.getStartDate().get(Calendar.MINUTE));
        updateEndDateButton();
        updateEndTimeButton();
    }

    /*
    * When setting the startDate, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endDate same as the startDate
    */
    DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            cloneActivity.getStartDate().set(Calendar.YEAR, year);
            cloneActivity.getStartDate().set(Calendar.MONTH, monthOfYear);
            cloneActivity.getStartDate().set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateStartDateButton();

            if(cloneActivity.getStartDate().after(cloneActivity.getEndDate())){
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

            cloneActivity.getStartDate().set(Calendar.HOUR_OF_DAY, hourOfDay);
            cloneActivity.getStartDate().set(Calendar.MINUTE, minute);
            updateStartTimeButton();

            if(cloneActivity.getStartDate().after(cloneActivity.getEndDate())) {
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

            cloneActivity.getEndDate().set(Calendar.YEAR, year);
            cloneActivity.getEndDate().set(Calendar.MONTH, monthOfYear);
            cloneActivity.getEndDate().set(Calendar.DAY_OF_MONTH, dayOfMonth);

            if(cloneActivity.getStartDate().after(cloneActivity.getEndDate())){
                resetEndDateAndTimeToStartDateAndTime();
            }else {
                updateEndDateButton();
            }


        }
    };

    /* When setting the endTime, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endTime same as the startTime
    */
    TimePickerDialog.OnTimeSetListener endTime = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            cloneActivity.getEndDate().set(Calendar.HOUR_OF_DAY, hourOfDay);
            cloneActivity.getEndDate().set(Calendar.MINUTE, minute);

            if(cloneActivity.getStartDate().after(cloneActivity.getEndDate())){
                resetEndDateAndTimeToStartDateAndTime();
            }else {
                updateEndTimeButton();
            }
        }
    };

    public void setStartDate(){
        new DatePickerDialog(EditActivity.this,
                startDate,
                cloneActivity.getStartDate().get(Calendar.YEAR),
                cloneActivity.getStartDate().get(Calendar.MONTH),
                cloneActivity.getStartDate().get(Calendar.DAY_OF_MONTH)).show();

    }

    public void setEndDate(){
        new DatePickerDialog(EditActivity.this,
                endDate,
                cloneActivity.getEndDate().get(Calendar.YEAR),
                cloneActivity.getEndDate().get(Calendar.MONTH),
                cloneActivity.getEndDate().get(Calendar.DAY_OF_MONTH)).show();

    }

    public void setStartTime(){
        new TimePickerDialog(EditActivity.this,
                startTime,
                cloneActivity.getStartDate().get(Calendar.HOUR_OF_DAY),
                cloneActivity.getStartDate().get(Calendar.MINUTE),
                false).show();
    }

    public void setEndTime(){
        new TimePickerDialog(EditActivity.this,
                endTime,
                cloneActivity.getStartDate().get(Calendar.HOUR_OF_DAY),
                cloneActivity.getStartDate().get(Calendar.MINUTE),
                false).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityLandingPage.putExtra("eventID", eventID);
        ActivityLandingPage.putExtra("activityID", activityID);
        startActivity(ActivityLandingPage);
        finish();
    }
}
