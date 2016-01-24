package com.zik.faro.frontend;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/*
* In this class we update the existing event object with the updated information that the user
* inputs.
* Special care should be taken that the updates should not reflect in the Event object until the
* user presses the OK button. Until then simply collect all the updated information in local
* variables.*/
public class EditEvent extends Activity {

    Button startDateButton = null;
    Button startTimeButton = null;
    Button endTimeButton = null;
    Button endDateButton = null;

    DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    DateFormat stf = new SimpleDateFormat("hh:mm a");

    private String eventID = null;


    private  static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static Event cloneEvent;
    private static Event event;

    Intent EventLanding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        startDateButton = (Button) findViewById(R.id.startDateButton);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);
        endDateButton = (Button) findViewById(R.id.endDateButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);

        TextView eventName = (TextView) findViewById(R.id.eventName);
        final EditText eventDescription = (EditText) findViewById(R.id.eventDescriptionEditText);

        Button editEventOK = (Button) findViewById(R.id.editEventOK);
        Button editEventCancel = (Button) findViewById(R.id.editEventCancel);

        EventLanding = new Intent(EditEvent.this, EventLandingPage.class);

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
            event = eventListHandler.getEventFromMap(eventID);
            if (event != null) {
                Gson gson = new Gson();
                String json = gson.toJson(event);

                cloneEvent = gson.fromJson(json, Event.class);
                String ev_name = cloneEvent.getEventName();
                eventName.setText(ev_name);
                eventDescription.setText(cloneEvent.getEventDescription());
                setBothTimeAndDateToEventDates();
            }
        }

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

        /*
        * Since OK button is pressed we will remove the event from the list and insert the clone.
        */
        editEventOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventListHandler.removeEventForEditing(event);
                ErrorCodes eventStatus;
                eventStatus = eventListHandler.addNewEvent(cloneEvent);
                //TODO What to do in Failure case?
                if (eventStatus == ErrorCodes.SUCCESS) {
                    EventLanding.putExtra("eventID", cloneEvent.getEventId());
                    startActivity(EventLanding);
                    finish();
                }
            }
        });

        editEventCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventLanding.putExtra("eventID", event.getEventId());
                startActivity(EventLanding);
                finish();
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

    private void setBothTimeAndDateToEventDates(){
        startDateButton.setText(sdf.format(cloneEvent.getStartDateCalendar().getTime()));
        endDateButton.setText(sdf.format(cloneEvent.getEndDateCalendar().getTime()));
        startTimeButton.setText(stf.format(cloneEvent.getStartDateCalendar().getTime()));
        endTimeButton.setText(stf.format(cloneEvent.getEndDateCalendar().getTime()));
    }

    private void resetEndDateAndTimeToStartDateAndTime(){
        cloneEvent.getEndDateCalendar().set(cloneEvent.getEndDateCalendar().YEAR, cloneEvent.getStartDateCalendar().get(Calendar.YEAR));
        cloneEvent.getEndDateCalendar().set(cloneEvent.getEndDateCalendar().MONTH, cloneEvent.getStartDateCalendar().get(Calendar.MONTH));
        cloneEvent.getEndDateCalendar().set(cloneEvent.getEndDateCalendar().DAY_OF_MONTH, cloneEvent.getStartDateCalendar().get(Calendar.DAY_OF_MONTH));
        cloneEvent.getEndDateCalendar().set(cloneEvent.getEndDateCalendar().HOUR_OF_DAY, cloneEvent.getStartDateCalendar().get(Calendar.HOUR_OF_DAY));
        cloneEvent.getEndDateCalendar().set(cloneEvent.getEndDateCalendar().MINUTE, cloneEvent.getStartDateCalendar().get(Calendar.MINUTE));
        updateEndDateButton();
        updateEndTimeButton();
    }

    public void updateStartDateButton(){
        startDateButton.setText(sdf.format(cloneEvent.getStartDateCalendar().getTime()));
    }

    public void updateEndDateButton(){
        endDateButton.setText(sdf.format(cloneEvent.getEndDateCalendar().getTime()));
    }

    public void updateStartTimeButton(){
        startTimeButton.setText(stf.format(cloneEvent.getStartDateCalendar().getTime()));
    }

    public void updateEndTimeButton(){
        endTimeButton.setText(stf.format(cloneEvent.getEndDateCalendar().getTime()));
    }


    public void setStartDate(){
        new DatePickerDialog(EditEvent.this,
                startDate,
                cloneEvent.getStartDateCalendar().get(Calendar.YEAR),
                cloneEvent.getStartDateCalendar().get(Calendar.MONTH),
                cloneEvent.getStartDateCalendar().get(Calendar.DAY_OF_MONTH)).show();

    }

    public void setEndDate(){
        new DatePickerDialog(EditEvent.this,
                endDate,
                cloneEvent.getEndDateCalendar().get(Calendar.YEAR),
                cloneEvent.getEndDateCalendar().get(Calendar.MONTH),
                cloneEvent.getEndDateCalendar().get(Calendar.DAY_OF_MONTH)).show();

    }

    public void setStartTime(){
        new TimePickerDialog(EditEvent.this,
                startTime,
                cloneEvent.getStartDateCalendar().get(Calendar.HOUR_OF_DAY),
                cloneEvent.getStartDateCalendar().get(Calendar.MINUTE),
                false).show();
    }

    public void setEndTime(){
        new TimePickerDialog(EditEvent.this,
                endTime,
                cloneEvent.getStartDateCalendar().get(Calendar.HOUR_OF_DAY),
                cloneEvent.getStartDateCalendar().get(Calendar.MINUTE),
                false).show();
    }


    /* When setting the startDate, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endDate same as the startDate
    */
    DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            cloneEvent.getStartDateCalendar().set(cloneEvent.getStartDateCalendar().YEAR, year);
            cloneEvent.getStartDateCalendar().set(cloneEvent.getStartDateCalendar().MONTH, monthOfYear);
            cloneEvent.getStartDateCalendar().set(cloneEvent.getStartDateCalendar().DAY_OF_MONTH, dayOfMonth);
            updateStartDateButton();

            if(cloneEvent.getStartDateCalendar().after(cloneEvent.getEndDateCalendar())){
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

            cloneEvent.getStartDateCalendar().set(cloneEvent.getStartDateCalendar().HOUR_OF_DAY, hourOfDay);
            cloneEvent.getStartDateCalendar().set(cloneEvent.getStartDateCalendar().MINUTE, minute);
            updateStartTimeButton();

            if(cloneEvent.getStartDateCalendar().after(cloneEvent.getEndDateCalendar())) {
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

            cloneEvent.getEndDateCalendar().set(cloneEvent.getEndDateCalendar().YEAR, year);
            cloneEvent.getEndDateCalendar().set(cloneEvent.getEndDateCalendar().MONTH, monthOfYear);
            cloneEvent.getEndDateCalendar().set(cloneEvent.getEndDateCalendar().DAY_OF_MONTH, dayOfMonth);

            if(cloneEvent.getStartDateCalendar().after(cloneEvent.getEndDateCalendar())){
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
            cloneEvent.getEndDateCalendar().set(cloneEvent.getEndDateCalendar().HOUR_OF_DAY, hourOfDay);
            cloneEvent.getEndDateCalendar().set(cloneEvent.getEndDateCalendar().MINUTE, minute);

            if(cloneEvent.getStartDateCalendar().after(cloneEvent.getEndDateCalendar())){
                resetEndDateAndTimeToStartDateAndTime();
            }else {
                updateEndTimeButton();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_event, menu);
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

    @Override
    public void onBackPressed() {
        EventLanding.putExtra("eventID", event.getEventId());
        startActivity(EventLanding);
        finish();
        super.onBackPressed();
    }
}
