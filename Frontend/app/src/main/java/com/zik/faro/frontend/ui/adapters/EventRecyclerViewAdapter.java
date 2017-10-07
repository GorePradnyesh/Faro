package com.zik.faro.frontend.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.ui.EventTabType;
import com.zik.faro.frontend.util.GetLocationAddressString;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRowHolder> {
    private List<Event> mEventList = new LinkedList<>();
    private Context mContext = null;
    private DateFormat sdf = new SimpleDateFormat("EEE, MMM dd");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private DateFormat circleDateFormat = new SimpleDateFormat("dd");
    private DateFormat circleMonthFormat = new SimpleDateFormat("MMM");
    private RecyclerRowItemOnClickListener rowItemOnClickListener;

    private Comparator<Event> upcomingEventsComparator = new Comparator<Event>() {
        @Override
        public int compare(Event event1, Event event2) {
            return event1.getStartDate().compareTo(event2.getStartDate());
        }
    };

    private Comparator<Event> pastEventsComparator = new Comparator<Event>() {
        @Override
        public int compare(Event event1, Event event2) {
            return event2.getEndDate().compareTo(event1.getEndDate());
        }
    };


    public EventRecyclerViewAdapter (Context context) {
        this.mContext = context;
    }

    public void populateAdapterWithEventList(List<Event> eventList, EventTabType eventTabType) {
        mEventList = eventList;

        if (eventTabType.equals(EventTabType.PAST)) {
            Collections.sort(mEventList, pastEventsComparator);
        } else {
            Collections.sort(mEventList, upcomingEventsComparator);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        mEventList.clear();
    }

    public boolean removeEventIfFound(String eventId) {
        for (Event event : mEventList) {
            if (eventId.equals(event.getId())){
                mEventList.remove(event);
                notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }

    public boolean findEvent(String eventId) {
        for (Event event : mEventList) {
            if (eventId.equals(event.getId())){
                return true;
            }
        }
        return false;
    }

    public void populateAdapterWithEvent (Event event, EventTabType eventTabType) {
        mEventList.add(event);
        if (eventTabType.equals(EventTabType.PAST)) {
            Collections.sort(mEventList, pastEventsComparator);
        } else {
            Collections.sort(mEventList, upcomingEventsComparator);
        }
        notifyDataSetChanged();
    }

    public Event getEventAtPostition (int position) {
        if ((position > (getItemCount() - 1)) || position < 0) {
            return null;
        }
        return mEventList.get(position);
    }


    public void setRowItemOnClickListener(RecyclerRowItemOnClickListener rowItemOnClickListener) {
        this.rowItemOnClickListener = rowItemOnClickListener;
    }

    @Override
    public EventRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View eventRowView = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.event_final_row_style, parent, false);
        return new EventRowHolder(eventRowView, rowItemOnClickListener);
    }

    @Override
    public void onBindViewHolder(EventRowHolder holder, int position) {
        final Event event = (Event) mEventList.get(position);
        EventListHandler eventListHandler = EventListHandler.getInstance(mContext);

        if (event == null)
            return;
        EventInviteStatus eventInviteStatus = eventListHandler.getUserEventStatus(event.getId());

        switch (position % 4) {
            case 0:
                holder.getEventCoverPhotoImageView().setImageResource(R.drawable.beach);
                break;
            case 1:
                holder.getEventCoverPhotoImageView().setImageResource(R.drawable.concert);
                break;
            case 2:
                holder.getEventCoverPhotoImageView().setImageResource(R.drawable.lake_shasta);
                break;
            case 3:
                holder.getEventCoverPhotoImageView().setImageResource(R.drawable.skiing);
                break;
        }

        holder.getEventNameTextView().setText(event.getEventName());

        holder.getInviteStatusTextView().setText(eventInviteStatus.toString());

        if (event.getStartDate() == null) {
            holder.getStartDateTextView().setText("Date/Time");
            holder.getStartTimeTextView().setText("TBD");
            holder.getCircleDateTextView().setVisibility(View.GONE);
            holder.getCircleMonthTextView().setText("?");
            holder.getStartDateAndTimeDisplayLinearLayout().setClickable(true);
            //TODO: Open pollLanding page for specialzied Date/Time poll
            holder.getStartDateAndTimeDisplayLinearLayout().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } else {
            holder.getStartDateTextView().setText(sdf.format(event.getStartDate().getTime()));
            holder.getStartTimeTextView().setText(stf.format(event.getStartDate().getTime()));
            holder.getCircleDateTextView().setVisibility(View.VISIBLE);
            holder.getCircleDateTextView().setText(circleDateFormat.format(event.getStartDate().getTime()));
            holder.getCircleMonthTextView().setText(circleMonthFormat.format(event.getStartDate().getTime()));
            holder.getStartDateAndTimeDisplayLinearLayout().setClickable(false);
        }

        final Location eventLocation = event.getLocation();
        if (eventLocation == null) {
            holder.getLocationDisplayLinearLayout().setClickable(true);
            holder.getLocationDisplayLinearLayout().setFocusable(false);
            //TODO: Open pollLanding page for specialzied Location poll
            holder.getLocationDisplayLinearLayout().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            holder.getEventAddressTextView().setText("Location\nTBD");
        } else {
            holder.getLocationDisplayLinearLayout().setClickable(true);
            holder.getLocationDisplayLinearLayout().setFocusable(false);
            holder.getLocationDisplayLinearLayout().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = getEventLocationUri(eventLocation);
                    Intent googleMapsAppIntent = new Intent(Intent.ACTION_VIEW, uri);
                    googleMapsAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(googleMapsAppIntent);
                }
            });

            String eventAddress = GetLocationAddressString.getLocationAddressString(eventLocation);
            holder.getEventAddressTextView().setText(eventAddress);
        }


        //Set the colors for the layout
        holder.getEventRowCardView().setBackgroundColor(Color.WHITE);
        holder.getEventNameTextView().setTextColor(Color.WHITE);
        holder.getStartDateTextView().setTextColor(Color.BLACK);
        holder.getStartTimeTextView().setTextColor(Color.BLACK);
        holder.getInviteStatusTextView().setTextColor(Color.BLACK);
        holder.getEventAddressTextView().setTextColor(Color.BLACK);


        /*int imageHeight = 70;
        int imageWidth = 3*imageHeight/2;
        holder.getEventCoverPhotoImageView().setMaxHeight(imageHeight);
        holder.getEventCoverPhotoImageView().setMinimumHeight(imageHeight);
        holder.getEventCoverPhotoImageView().setMaxWidth(imageWidth);
        holder.getEventCoverPhotoImageView().setMinimumWidth(imageWidth);*/
        //holder.getEventAddressTextView().setTextColor(Color.GRAY);

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

    @Override
    public int getItemCount() {
        return mEventList.size();
    }
}
