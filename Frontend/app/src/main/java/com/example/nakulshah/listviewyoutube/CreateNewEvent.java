package com.example.nakulshah.listviewyoutube;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.text.DateFormat;
import java.util.Locale;

/*
 * On this page an Event can be created with certain basic requirements like the Event Name, Start
 * Date and Time and End Date and Time as mandatory requirements.
 * Other fields like Invitees, Location, Description of the event are optional which can be edited
 * later from the Event Landing Page.
 * The Start Date/Time and the End Date/Time will be set to the current Date/Time by default.
 * Checks are made to make sure that the Start Date/Time is not set after the End Date/Time.
 *
 */
public class CreateNewEvent extends ActionBarActivity {

    public static final int NO_CHANGES = 0;
    EventGenerator evntGen = EventGenerator.getInstance();
    Intent AppLanding = null;
    DateFormat dateFormat = DateFormat.getDateInstance();

    Calendar startDateCalendar = Calendar.getInstance();
    Calendar endDateCalendar = Calendar.getInstance();
    Button startDateButton = null;
    Button startTimeButton = null;
    Button endTimeButton = null;
    Button endDateButton = null;
    DateFormat stf = new SimpleDateFormat("hh:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        final EditText eventName = (EditText) findViewById(R.id.eventNameTextEdit);
        final EditText eventStatus = (EditText) findViewById(R.id.eventStatusEditText);

        startDateButton = (Button) findViewById(R.id.startDateButton);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);

        endDateButton = (Button) findViewById(R.id.endDateButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);

        final Button createNewEventOK = (Button) findViewById(R.id.createNewEventOK);
        final Button createNewEventCancel = (Button) findViewById(R.id.createNewEventCancel);

        final Intent eventLanding = new Intent(CreateNewEvent.this, EventLandingPage.class);
        AppLanding = new Intent(CreateNewEvent.this, AppLandingPage.class);

        eventName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                eventStatus.setEnabled(!(eventName.getText().toString().trim().isEmpty()));

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        eventStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                createNewEventOK.setEnabled(!(eventStatus.getText().toString().trim().isEmpty()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        createNewEventOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int status = Integer.parseInt(eventStatus.getText().toString());
                String event_Name = eventName.getText().toString();
                final Event E = new Event(event_Name, status, startDateCalendar, endDateCalendar);
                evntGen.addNewEvent(E);
                eventLanding.putExtra("Event", E);
                startActivity(eventLanding);
                finish();

            }
        });

        createNewEventCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setResult(NO_CHANGES);
                startActivity(AppLanding);
                finish();
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
        startDateButton.setText(dateFormat.format(startDateCalendar.getTime()));
        endDateButton.setText(dateFormat.format(startDateCalendar.getTime()));
        startTimeButton.setText(stf.format(startDateCalendar.getTime()));
        endTimeButton.setText(stf.format(startDateCalendar.getTime()));
    }

    private void resetEndDateAndTimeToStartDateAndTime(){
        endDateCalendar.set(endDateCalendar.YEAR, startDateCalendar.get(Calendar.YEAR));
        endDateCalendar.set(endDateCalendar.MONTH, startDateCalendar.get(Calendar.MONTH));
        endDateCalendar.set(endDateCalendar.DAY_OF_MONTH, startDateCalendar.get(Calendar.DAY_OF_MONTH));
        endDateCalendar.set(endDateCalendar.HOUR, startDateCalendar.get(Calendar.HOUR));
        endDateCalendar.set(endDateCalendar.MINUTE, startDateCalendar.get(Calendar.MINUTE));
        updateEndDate();
        updateEndTime();
    }

    public void updateStartDate(){
        startDateButton.setText(dateFormat.format(startDateCalendar.getTime()));
    }

    public void updateEndDate(){
        endDateButton.setText(dateFormat.format(endDateCalendar.getTime()));
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
                startDateCalendar.get(Calendar.HOUR),
                startDateCalendar.get(Calendar.MINUTE),
                false).show();
    }

    public void setEndTime(){
        new TimePickerDialog(CreateNewEvent.this,
                endTime,
                endDateCalendar.get(Calendar.HOUR),
                endDateCalendar.get(Calendar.MINUTE),
                false).show();
    }


    /* When setting the startDate, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endDate same as the startDate
    */
    DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            startDateCalendar.set(startDateCalendar.YEAR, year);
            startDateCalendar.set(startDateCalendar.MONTH, monthOfYear);
            startDateCalendar.set(startDateCalendar.DAY_OF_MONTH, dayOfMonth);
            updateStartDate();

            if(startDateCalendar.compareTo(endDateCalendar) == 1){
                resetEndDateAndTimeToStartDateAndTime();
            }
        }
    };

    /* When setting the startTime, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endTime same as the startTime
    */
    //TODO: AM not displaying when selecting AM time on app.
    TimePickerDialog.OnTimeSetListener startTime = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            startDateCalendar.set(startDateCalendar.HOUR, hourOfDay);
            startDateCalendar.set(startDateCalendar.MINUTE, minute);
            updateStartTime();

            if(startDateCalendar.compareTo(endDateCalendar) == 1) {
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

            endDateCalendar.set(endDateCalendar.YEAR, year);
            endDateCalendar.set(endDateCalendar.MONTH, monthOfYear);
            endDateCalendar.set(endDateCalendar.DAY_OF_MONTH, dayOfMonth);

            if(startDateCalendar.compareTo(endDateCalendar) == 1){
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
            endDateCalendar.set(endDateCalendar.HOUR, hourOfDay);
            endDateCalendar.set(endDateCalendar.MINUTE, minute);

            if(startDateCalendar.compareTo(endDateCalendar) == 1){
                resetEndDateAndTimeToStartDateAndTime();
            }else {
                updateEndTime();
            }
        }
    };

    @Override
    public void onBackPressed() {
        //setResult(NO_CHANGES);
        startActivity(AppLanding);
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
