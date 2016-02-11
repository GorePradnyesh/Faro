package com.zik.faro.frontend;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventUser;


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

    private static int popupWidth;
    private static int popupHeight;
    private RelativeLayout popUpRelativeLayout;

    Intent EventLanding = null;
    Intent EventListPage = null;

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
        Button deleteEventButton = (Button) findViewById(R.id.deleteEvent);

        popUpRelativeLayout = (RelativeLayout) findViewById(R.id.editEventPage);

        EventLanding = new Intent(EditEvent.this, EventLandingPage.class);
        EventListPage = new Intent(EditEvent.this, EventListPage.class);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        popupWidth = (int) (dm.widthPixels * 0.8);
        popupHeight = (int) (dm.heightPixels * 0.8);

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
                eventListHandler.removeEventFromListAndMap(event);
                ErrorCodes eventStatus;
                EventUser eventUser = eventListHandler.getEventUser(cloneEvent.getEventId(),
                        eventListHandler.getMyUserId());

                eventStatus = eventListHandler.addNewEvent(cloneEvent, eventUser.getInviteStatus());
                //TODO What to do in Failure case?
                if (eventStatus == ErrorCodes.SUCCESS) {
                    EventLanding.putExtra("eventID", cloneEvent.getEventId());
                    startActivity(EventLanding);
                    finish();
                }
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

    public void confirmEventDeletePopUP(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.delete_event_pop_up, null);
        final PopupWindow popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        Button delete = (Button)container.findViewById(R.id.deleteEventPopUp);
        Button cancel = (Button)container.findViewById(R.id.cancelEventPopUp);

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

                //TODO: Make API call and update server
                eventListHandler.removeEventFromListAndMap(event);
                startActivity(EventListPage);
                popupWindow.dismiss();
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void setBothTimeAndDateToEventDates(){
        startDateButton.setText(sdf.format(cloneEvent.getStartDate().getTime()));
        endDateButton.setText(sdf.format(cloneEvent.getEndDate().getTime()));
        startTimeButton.setText(stf.format(cloneEvent.getStartDate().getTime()));
        endTimeButton.setText(stf.format(cloneEvent.getEndDate().getTime()));
    }

    private void resetEndDateAndTimeToStartDateAndTime(){
        cloneEvent.getEndDate().set(cloneEvent.getEndDate().YEAR, cloneEvent.getStartDate().get(Calendar.YEAR));
        cloneEvent.getEndDate().set(cloneEvent.getEndDate().MONTH, cloneEvent.getStartDate().get(Calendar.MONTH));
        cloneEvent.getEndDate().set(cloneEvent.getEndDate().DAY_OF_MONTH, cloneEvent.getStartDate().get(Calendar.DAY_OF_MONTH));
        cloneEvent.getEndDate().set(cloneEvent.getEndDate().HOUR_OF_DAY, cloneEvent.getStartDate().get(Calendar.HOUR_OF_DAY));
        cloneEvent.getEndDate().set(cloneEvent.getEndDate().MINUTE, cloneEvent.getStartDate().get(Calendar.MINUTE));
        updateEndDateButton();
        updateEndTimeButton();
    }

    public void updateStartDateButton(){
        startDateButton.setText(sdf.format(cloneEvent.getStartDate().getTime()));
    }

    public void updateEndDateButton(){
        endDateButton.setText(sdf.format(cloneEvent.getEndDate().getTime()));
    }

    public void updateStartTimeButton(){
        startTimeButton.setText(stf.format(cloneEvent.getStartDate().getTime()));
    }

    public void updateEndTimeButton(){
        endTimeButton.setText(stf.format(cloneEvent.getEndDate().getTime()));
    }


    public void setStartDate(){
        new DatePickerDialog(EditEvent.this,
                startDate,
                cloneEvent.getStartDate().get(Calendar.YEAR),
                cloneEvent.getStartDate().get(Calendar.MONTH),
                cloneEvent.getStartDate().get(Calendar.DAY_OF_MONTH)).show();

    }

    public void setEndDate(){
        new DatePickerDialog(EditEvent.this,
                endDate,
                cloneEvent.getEndDate().get(Calendar.YEAR),
                cloneEvent.getEndDate().get(Calendar.MONTH),
                cloneEvent.getEndDate().get(Calendar.DAY_OF_MONTH)).show();

    }

    public void setStartTime(){
        new TimePickerDialog(EditEvent.this,
                startTime,
                cloneEvent.getStartDate().get(Calendar.HOUR_OF_DAY),
                cloneEvent.getStartDate().get(Calendar.MINUTE),
                false).show();
    }

    public void setEndTime(){
        new TimePickerDialog(EditEvent.this,
                endTime,
                cloneEvent.getStartDate().get(Calendar.HOUR_OF_DAY),
                cloneEvent.getStartDate().get(Calendar.MINUTE),
                false).show();
    }


    /* When setting the startDate, we compare the startCalendar to the endCalendar and if the
    * startCalendar is after the endCalendar then we set the endDate same as the startDate
    */
    DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            cloneEvent.getStartDate().set(cloneEvent.getStartDate().YEAR, year);
            cloneEvent.getStartDate().set(cloneEvent.getStartDate().MONTH, monthOfYear);
            cloneEvent.getStartDate().set(cloneEvent.getStartDate().DAY_OF_MONTH, dayOfMonth);
            updateStartDateButton();

            if(cloneEvent.getStartDate().after(cloneEvent.getEndDate())){
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

            cloneEvent.getStartDate().set(cloneEvent.getStartDate().HOUR_OF_DAY, hourOfDay);
            cloneEvent.getStartDate().set(cloneEvent.getStartDate().MINUTE, minute);
            updateStartTimeButton();

            if(cloneEvent.getStartDate().after(cloneEvent.getEndDate())) {
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

            cloneEvent.getEndDate().set(cloneEvent.getEndDate().YEAR, year);
            cloneEvent.getEndDate().set(cloneEvent.getEndDate().MONTH, monthOfYear);
            cloneEvent.getEndDate().set(cloneEvent.getEndDate().DAY_OF_MONTH, dayOfMonth);

            if(cloneEvent.getStartDate().after(cloneEvent.getEndDate())){
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
            cloneEvent.getEndDate().set(cloneEvent.getEndDate().HOUR_OF_DAY, hourOfDay);
            cloneEvent.getEndDate().set(cloneEvent.getEndDate().MINUTE, minute);

            if(cloneEvent.getStartDate().after(cloneEvent.getEndDate())){
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
