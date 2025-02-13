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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.common.collect.Sets;
import com.squareup.okhttp.Request;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.data.UpdateRequest;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import static android.widget.Toast.LENGTH_LONG;

public class EditActivityPage extends android.app.Activity {
    private String eventId = null;
    private String activityId = null;
    private Event event;
    private Activity cloneActivity;
    private EventListHandler eventListHandler = EventListHandler.getInstance(this);
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

    private Button startDateButton = null;
    private Button startTimeButton = null;
    private Button endTimeButton = null;
    private Button endDateButton = null;

    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();

    private DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");

    private RelativeLayout popUpRelativeLayout;

    private static String TAG = "EditActivityPage";

    private Intent activityLandingPageIntent;

    private Context mContext;

    private PopupWindow popupWindow;

    private LinearLayout linlaHeaderProgress = null;
    private RelativeLayout editActivityRelativeLayout = null;

    private Bundle extras = null;

    private Set<String> updatedFields = Sets.newHashSet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activity);

        mContext = this;

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        editActivityRelativeLayout = (RelativeLayout) findViewById(R.id.editActivityRelativeLayout);
        editActivityRelativeLayout.setVisibility(View.GONE);

        extras = getIntent().getExtras();
        if (extras == null)return; //TODO: How to handle such conditions

        try {
            setupPageDetails();
        } catch (FaroObjectNotFoundException e) {
            Log.e(TAG, MessageFormat.format("Activity {0} has been deleted", activityId));
            Toast.makeText(this, "Activity has been deleted", LENGTH_LONG).show();
            finish();
        }
    }

    private void setupPageDetails () throws FaroObjectNotFoundException{

        linlaHeaderProgress.setVisibility(View.GONE);
        editActivityRelativeLayout.setVisibility(View.VISIBLE);

        eventId = extras.getString(FaroIntentConstants.EVENT_ID);
        activityId = extras.getString(FaroIntentConstants.ACTIVITY_ID);

        event = eventListHandler.getCloneObject(eventId);
        cloneActivity = activityListHandler.getCloneObject(activityId);

        startDateButton = (Button) findViewById(R.id.startDateButton);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);
        endDateButton = (Button) findViewById(R.id.endDateButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);

        Button editActivityOK = (Button) findViewById(R.id.editActivityOK);
        Button deleteActivity = (Button) findViewById(R.id.deleteActivity);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.editActivity);

        activityLandingPageIntent = new Intent(EditActivityPage.this, ActivityLandingPage.class);

        final EditText activityDescription = (EditText) findViewById(R.id.activityDescriptionEditText);;

        /*
        * Do not remove the activity from the list and the map in the activityListHandler below.
        * Make a clone of the activity and make changes to that.
        * When the OK button is clicked, only then remove the activity and insert the clone.
        * This way if the user makes some changes but then clicks back without clicking OK button,
        * the original activity is not affected.
        */

        if (cloneActivity != null) {
            TextView activityName = (TextView) findViewById(R.id.activityName);
            activityName.setText(cloneActivity.getName());

            activityDescription.setText(cloneActivity.getDescription());
            setBothTimeAndDateToActivityDates();
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
                updatedFields.add(Activity.DESCRIPTION);
            }
        });

        /*
        * Since OK button is pressed we will remove the cloneactivity from the list and insert the clone.
        */
        editActivityOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cloneActivity.setStartDate(startDateCalendar);
                cloneActivity.setEndDate(endDateCalendar);
                updatedFields.add(Activity.START_DATE);
                updatedFields.add(Activity.END_DATE);

                updateActivityToServer();
            }
        });

        deleteActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmActivityDeletePopUP(v);
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
    }

    private void confirmActivityDeletePopUP(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.delete_popup, null);
        popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT,
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
                deleteActivityFromServer();
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

    private void setBothTimeAndDateToActivityDates(){
        updateStartDateCalendarDate(cloneActivity.getStartDate());
        updateStartDateCalendarTime(cloneActivity.getStartDate());
        updateEndDateCalendarDate(cloneActivity.getEndDate());
        updateEndDateCalendarTime(cloneActivity.getEndDate());
    }

    private void resetEndDateAndTimeToStartDateAndTime(){
        updateEndDateCalendarDate(startDateCalendar);
        updateEndDateCalendarTime(startDateCalendar);
    }

    private void setStartDate(){
        new DatePickerDialog(EditActivityPage.this,
                startDate,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setStartTime(){
        new TimePickerDialog(EditActivityPage.this,
                startTime,
                startDateCalendar.get(Calendar.HOUR_OF_DAY),
                startDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    private void setEndDate(){
        new DatePickerDialog(EditActivityPage.this,
                endDate,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setEndTime(){
        new TimePickerDialog(EditActivityPage.this,
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
            Toast.makeText(EditActivityPage.this, "Activity's Start Date cannot be before Event's Start Date", LENGTH_LONG).show();
        }else if (!ret_val2){
            Toast.makeText(EditActivityPage.this, "Activity's Start Date cannot be on/after Event's End Date", LENGTH_LONG).show();
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
            Toast.makeText(EditActivityPage.this, "Activity's End Date cannot be before Activity's Start Date", LENGTH_LONG).show();
        }else if (!ret_val2){
            Toast.makeText(EditActivityPage.this, "Activity's End Date cannot be after Event's End Date", LENGTH_LONG).show();
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

    private void updateActivityToServer() {
        UpdateRequest<Activity> updateRequest = new UpdateRequest<>(updatedFields, cloneActivity);
        serviceHandler.getActivityHandler().updateActivity(new BaseFaroRequestCallback<String>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to update cloneactivity");

                // TODO:  Handle errors and update version conflict
            }

            @Override
            public void onResponse(String s, HttpError error) {
                if (error == null ) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            activityListHandler.addActivityToListAndMap(eventId, cloneActivity, mContext);
                            FaroIntentInfoBuilder.activityIntent(activityLandingPageIntent, eventId, cloneActivity.getId());
                            startActivity(activityLandingPageIntent);
                            finish();
                        }
                    });
                } else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId, activityId, updateRequest);
    }

    private void deleteActivityFromServer() {
        serviceHandler.getActivityHandler().deleteActivity(new BaseFaroRequestCallback<String>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to delete cloneactivity");
            }

            @Override
            public void onResponse(String s, HttpError error) {
                if (error == null ) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            activityListHandler.removeActivityFromListAndMap(eventId, activityId, mContext);
                            popupWindow.dismiss();
                            Toast.makeText(EditActivityPage.this, cloneActivity.getName() + " is Deleted", LENGTH_LONG).show();
                            finish();
                        }
                    });
                }else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId, activityId);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FaroIntentInfoBuilder.activityIntent(activityLandingPageIntent, eventId, activityId);
        startActivity(activityLandingPageIntent);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Check if the version is same. It can be different if this page is loaded and a notification
        // is received for this later which updates the cache but clonedata on this page remains
        // stale.
        try {
            if (!activityListHandler.checkObjectVersionIfLatest(activityId, cloneActivity.getVersion())) {
                setupPageDetails();
            }
        } catch (FaroObjectNotFoundException e) {
            //Activity has been deleted.
            Log.e(TAG, MessageFormat.format("Activity {0} has been deleted", activityId));
            Toast.makeText(this, "Activity has been deleted", LENGTH_LONG).show();
            finish();
        }
    }
}
