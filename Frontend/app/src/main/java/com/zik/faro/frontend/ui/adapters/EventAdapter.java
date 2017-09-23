package com.zik.faro.frontend.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.frontend.NotAcceptedEventListPage;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.SetDisplayProperties;
import com.zik.faro.data.Location;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.util.GetLocationAddressString;

public class EventAdapter extends ArrayAdapter <Event>{

    private  Comparator <Event> comparator = new Comparator<Event>() {
        @Override
        public int compare(Event event1, Event event2) {
            return event1.getStartDate().compareTo(event2.getStartDate());
        }
    };


    private DateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private Context mContext =  this.getContext();


    public EventAdapter(Context context, int resource) {
        super(context, resource);
    }

    private EventListHandler eventListHandler = EventListHandler.getInstance(mContext);
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private String TAG = "EventAdapter";

    public void addEvent(Event event){
        insert(event, 0);
        sort(comparator);
    }

    public void clearList () {
        clear();
    }

    public void addDownloadedEvents (List <Event> events) {
        List<Event> completedEvents = new LinkedList<>();
        List<Event> notCompletedEvents = new LinkedList<>();
        Calendar currentCalendar = Calendar.getInstance();

        for (Event event : events) {
            if (event.getEndDate().before(currentCalendar)) {
                completedEvents.add(event);
            } else {
                notCompletedEvents.add(event);
            }
        }

        Collections.sort(completedEvents, comparator);

        Collections.sort(notCompletedEvents, comparator);

        //Deleting existing events from the adapter list
        clear();

        addAll(completedEvents);
        addAll(notCompletedEvents);
    }

    private static class NotAcceptedButtonRowHolder {
        private Button notAcceptedButton;

        public Button getNotAcceptedButton() {
            return notAcceptedButton;
        }

        public void setNotAcceptedButton(Button notAcceptedButton) {
            this.notAcceptedButton = notAcceptedButton;
        }
    }

    private static class CoolThingsAroundYouCarouselHolder {
        private ImageView carouselImageView;

        public ImageView getCarouselImageView() {
            return carouselImageView;
        }

        public void setCarouselImageView(ImageView carouselImageView) {
            this.carouselImageView = carouselImageView;
        }
    }

    private static class EventRowHolder {
        private TextView eventNameTextView;
        private TextView eventStartDateTextView;
        private TextView eventStartTimeTextView;
        private ImageButton mapImageButton;
        //private TextView eventAddressTextView;
        private TextView attendeesCountTextView;
        private ImageView eventCoverPhotoImageView;
        private CardView eventRowCardView;
        private LinearLayout inviteResponseLinearLayout;
        private Button eventInvitationAccept;
        private Button eventInvitationMaybe;
        private Button eventInvitationDecline;
        private RelativeLayout eventLocationRelativeLayout;


        public CardView getEventRowCardView() {
            return eventRowCardView;
        }

        public void setEventRowCardView(CardView eventRowCardView) {
            this.eventRowCardView = eventRowCardView;
        }

        public TextView getEventNameTextView() {
            return eventNameTextView;
        }

        public void setEventNameTextView(TextView eventNameTextView) {
            this.eventNameTextView = eventNameTextView;
        }

        public TextView getEventStartDateTextView() {
            return eventStartDateTextView;
        }

        public void setEventStartDateTextView(TextView eventStartDateTextView) {
            this.eventStartDateTextView = eventStartDateTextView;
        }

        public TextView getEventStartTimeTextView() {
            return eventStartTimeTextView;
        }

        public void setEventStartTimeTextView(TextView eventStartTimeTextView) {
            this.eventStartTimeTextView = eventStartTimeTextView;
        }

        public ImageButton getMapImageButton() {
            return mapImageButton;
        }

        public void setMapImageButton(ImageButton mapImageButton) {
            this.mapImageButton = mapImageButton;
        }

        /*public TextView getEventAddressTextView() {
            return eventAddressTextView;
        }

        public void setEventAddressTextView(TextView eventAddressTextView) {
            this.eventAddressTextView = eventAddressTextView;
        }*/

        public TextView getAttendeesCountTextView() {
            return attendeesCountTextView;
        }

        public void setAttendeesCountTextView(TextView attendeesCountTextView) {
            this.attendeesCountTextView = attendeesCountTextView;
        }

        public ImageView getEventCoverPhotoImageView() {
            return eventCoverPhotoImageView;
        }

        public void setEventCoverPhotoImageView(ImageView eventCoverPhotoImageView) {
            this.eventCoverPhotoImageView = eventCoverPhotoImageView;
        }

        public RelativeLayout getEventLocationRelativeLayout() {
            return eventLocationRelativeLayout;
        }

        public void setEventLocationRelativeLayout(RelativeLayout eventLocationRelativeLayout) {
            this.eventLocationRelativeLayout = eventLocationRelativeLayout;
        }

        public LinearLayout getInviteResponseLinearLayout() {
            return inviteResponseLinearLayout;
        }

        public void setInviteResponseLinearLayout(LinearLayout inviteResponseLinearLayout) {
            this.inviteResponseLinearLayout = inviteResponseLinearLayout;
        }

        public Button getEventInvitationAccept() {
            return eventInvitationAccept;
        }

        public void setEventInvitationAccept(Button eventInvitationAccept) {
            this.eventInvitationAccept = eventInvitationAccept;
        }

        public Button getEventInvitationMaybe() {
            return eventInvitationMaybe;
        }

        public void setEventInvitationMaybe(Button eventInvitationMaybe) {
            this.eventInvitationMaybe = eventInvitationMaybe;
        }

        public Button getEventInvitationDecline() {
            return eventInvitationDecline;
        }

        public void setEventInvitationDecline(Button eventInvitationDecline) {
            this.eventInvitationDecline = eventInvitationDecline;
        }

    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }

    private Uri getEventLocationUri (Location eventLocation) {
        double latitude = eventLocation.getPosition().getLatitude();
        double longitude = eventLocation.getPosition().getLongitude();
        String label = GetLocationAddressString.getLocationAddressString(eventLocation);
        String uriBegin = "geo:" + latitude + "," + longitude;
        String query = latitude + "," + longitude + "(" + label + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        return Uri.parse(uriString);
    }

    private void updateUserEventInviteStatus(final EventInviteStatus eventInviteStatus, final Event event) {
        serviceHandler.getEventHandler().updateEventUserInviteStatus(new BaseFaroRequestCallback<String>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to send cloneEvent Invite Status");
            }

            @Override
            public void onResponse(String s, HttpError error) {
                if (error == null ) {
                    //Since update to server successful
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            switch (eventInviteStatus){
                                case ACCEPTED:
                                    eventListHandler.addEventToListAndMap(event, EventInviteStatus.ACCEPTED);
                                    break;
                                case MAYBE:
                                    eventListHandler.addEventToListAndMap(event, EventInviteStatus.MAYBE);
                                    break;
                                case DECLINED:
                                    //TODO: change this to not do the below but just change the state and keep it in the list and Map
                                    eventListHandler.removeEventFromListAndMap(event.getId());
                                    break;
                            }
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                } else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, event.getId(), eventInviteStatus);
    }

    //TODO: what is the warning given for this function?
    @Override
    public View getView(int position, final View convertView, final ViewGroup parent) {
        View row = convertView;


        final Event event = (Event) getItem(position);
        if (event == null)
            return row;

        EventInviteStatus eventInviteStatus = eventListHandler.getUserEventStatus(event.getId());

        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (("Faro dummy Not Accepted Button Row Element").equals(event.getEventName())) {
            row = inflater.inflate(R.layout.not_accepted_button_row, parent, false);
            NotAcceptedButtonRowHolder holder = new NotAcceptedButtonRowHolder();
            holder.setNotAcceptedButton((Button) row.findViewById(R.id.notAcceptedEventsButton));

            holder.getNotAcceptedButton().setText(MessageFormat.format("{0} Not Accepted Events",
                    eventListHandler.notAcceptedEventAdapter.getCount()));

            holder.getNotAcceptedButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent notAcceptedEventListPage = new Intent(mContext, NotAcceptedEventListPage.class);
                    mContext.startActivity(notAcceptedEventListPage);
                }
            });
            row.setTag(holder);
        } else if (("Faro dummy Carousel Row Element").equals(event.getEventName())) {
            row = inflater.inflate(R.layout.cool_things_around_you_carousel, parent, false);
            CoolThingsAroundYouCarouselHolder holder = new CoolThingsAroundYouCarouselHolder();
            holder.setCarouselImageView((ImageView) row.findViewById(R.id.coolThingsAroundYouCarouselImageView));
            row.setTag(holder);
        }
        else {
            row = inflater.inflate(R.layout.event_row_style, parent, false);
            EventRowHolder holder = new EventRowHolder();
            holder.setEventRowCardView((CardView) row.findViewById(R.id.eventRowCardView));
            holder.setEventNameTextView((TextView) row.findViewById(R.id.eventNameTextView));
            holder.setEventStartDateTextView((TextView)row.findViewById(R.id.startDateTextView));
            holder.setEventStartTimeTextView((TextView)row.findViewById(R.id.startTimeTextView));
            holder.setMapImageButton((ImageButton)row.findViewById(R.id.mapImageButton));
            //holder.setEventAddressTextView((TextView)row.findViewById(R.id.eventAddressTextView));
            holder.setAttendeesCountTextView((TextView)row.findViewById(R.id.attendeesCountTextView));
            holder.setEventCoverPhotoImageView((ImageView)row.findViewById(R.id.eventCoverPhotoImageView));
            holder.setEventLocationRelativeLayout((RelativeLayout) row.findViewById(R.id.eventLocationRelativeLayout));
            holder.setInviteResponseLinearLayout((LinearLayout) row.findViewById(R.id.inviteResponseLinearLayout));
            holder.setEventInvitationAccept((Button) row.findViewById(R.id.eventInvitationAccept));
            holder.setEventInvitationMaybe((Button) row.findViewById(R.id.eventInvitationMaybe));
            holder.setEventInvitationDecline((Button) row.findViewById(R.id.eventInvitationDecline));
            row.setTag(holder);



            if (eventInviteStatus.equals(EventInviteStatus.ACCEPTED)) {
                holder.getInviteResponseLinearLayout().setVisibility(View.GONE);
            } else {
                holder.getInviteResponseLinearLayout().setVisibility(View.VISIBLE);
                holder.getEventInvitationAccept().setTextColor(Color.GRAY);
                holder.getEventInvitationMaybe().setTextColor(Color.GRAY);
                holder.getEventInvitationDecline().setTextColor(Color.GRAY);

                if (eventInviteStatus.equals(EventInviteStatus.MAYBE))
                    holder.getEventInvitationMaybe().setTextColor(mContext.getResources().getColor(R.color.faro_button_color, null));
                else if (eventInviteStatus.equals(EventInviteStatus.DECLINED))
                    holder.getEventInvitationDecline().setTextColor(mContext.getResources().getColor(R.color.faro_button_color, null));
            }

            holder.getEventInvitationAccept().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateUserEventInviteStatus(EventInviteStatus.ACCEPTED, event);
                }
            });

            holder.getEventInvitationDecline().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateUserEventInviteStatus(EventInviteStatus.DECLINED, event);
                }
            });

            holder.getEventInvitationMaybe().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateUserEventInviteStatus(EventInviteStatus.MAYBE, event);
                }
            });

            //Set the colors for the layout
            holder.getEventRowCardView().setBackgroundColor(Color.WHITE);
            holder.getEventNameTextView().setTextColor(Color.BLACK);
            holder.getEventStartDateTextView().setTextColor(Color.GRAY);
            holder.getEventStartTimeTextView().setTextColor(Color.GRAY);
            holder.getAttendeesCountTextView().setTextColor(Color.GRAY);

            /*int imageHeight = 70;
            int imageWidth = 3*imageHeight/2;
            holder.getEventCoverPhotoImageView().setMaxHeight(imageHeight);
            holder.getEventCoverPhotoImageView().setMinimumHeight(imageHeight);
            holder.getEventCoverPhotoImageView().setMaxWidth(imageWidth);
            holder.getEventCoverPhotoImageView().setMinimumWidth(imageWidth);*/
            //holder.getEventAddressTextView().setTextColor(Color.GRAY);

            holder.getEventNameTextView().setText(event.getEventName());
            holder.getEventStartDateTextView().setText(sdf.format(event.getStartDate().getTime()));
            holder.getEventStartTimeTextView().setText(stf.format(event.getStartDate().getTime()));

            final Location eventLocation = event.getLocation();
            if (eventLocation == null) {
                //holder.getEventLocationRelativeLayout().setVisibility(View.INVISIBLE);
                //holder.getEventAddressTextView().setVisibility(View.GONE);
                holder.getMapImageButton().setVisibility(View.INVISIBLE);
            } else {
                String str = GetLocationAddressString.getLocationAddressString(eventLocation);
                //holder.getEventLocationRelativeLayout().setVisibility(View.VISIBLE);
                //holder.getEventAddressTextView().setVisibility(View.VISIBLE);
                holder.getMapImageButton().setVisibility(View.VISIBLE);
                holder.getMapImageButton().setFocusable(false);
                //holder.getEventAddressTextView().setText(str);
                holder.getMapImageButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = getEventLocationUri(eventLocation);
                        Intent googleMapsAppIntent = new Intent(Intent.ACTION_VIEW, uri);
                        parent.getContext().startActivity(googleMapsAppIntent);
                    }
                });
            }
        }


        return row;
    }
}
