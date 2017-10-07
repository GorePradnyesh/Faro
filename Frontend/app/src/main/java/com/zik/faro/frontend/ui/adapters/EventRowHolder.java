package com.zik.faro.frontend.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zik.faro.frontend.R;

public class EventRowHolder extends RecyclerView.ViewHolder {
    private CardView eventRowCardView;
    private TextView eventNameTextView;
    public ImageView eventCoverPhotoImageView;
    private TextView startDateTextView;
    private TextView startTimeTextView;
    private ImageView locationPinImageView;
    private TextView circleDateTextView;
    private TextView circleMonthTextView;
    private TextView inviteStatusTextView;
    private TextView eventAddressTextView;
    private LinearLayout locationDisplayLinearLayout;
    private LinearLayout startDateAndTimeDisplayLinearLayout;

    public EventRowHolder(View row, final RecyclerRowItemOnClickListener rowItemOnClickListener) {
        super(row);
        eventRowCardView = (CardView) row.findViewById(R.id.eventRowCardView);
        eventNameTextView =(TextView) row.findViewById(R.id.eventNameTextView);
        eventCoverPhotoImageView = (ImageView) row.findViewById(R.id.eventCoverPhotoImageView);
        startDateTextView = (TextView) row.findViewById(R.id.startDateTextView);
        startTimeTextView = (TextView) row.findViewById(R.id.startTimeTextView);
        locationPinImageView = (ImageView) row.findViewById(R.id.locationPinImageView);
        circleDateTextView = (TextView) row.findViewById(R.id.circleDateTextView);
        circleMonthTextView = (TextView) row.findViewById(R.id.circleMonthTextView);
        inviteStatusTextView = (TextView) row.findViewById(R.id.inviteStatusTextView);
        eventAddressTextView = (TextView)row.findViewById(R.id.eventAddressTextView);
        locationDisplayLinearLayout = (LinearLayout) row.findViewById(R.id.locationDisplayLinearLayout);
        startDateAndTimeDisplayLinearLayout = (LinearLayout) row.findViewById(R.id.startDateAndTimeDisplayLinearLayout);

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    rowItemOnClickListener.onItemClickListener(position);
                }
            }
        });
    }

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

    public TextView getStartDateTextView() {
        return startDateTextView;
    }

    public void setStartDateTextView(TextView startDateTextView) {
        this.startDateTextView = startDateTextView;
    }

    public TextView getStartTimeTextView() {
        return startTimeTextView;
    }

    public void setStartTimeTextView(TextView startTimeTextView) {
        this.startTimeTextView = startTimeTextView;
    }

    public ImageView getLocationPinImageView() {
        return locationPinImageView;
    }

    public void setLocationPinImageView(ImageView locationPinImageView) {
        this.locationPinImageView = locationPinImageView;
    }

    public ImageView getEventCoverPhotoImageView() {
        return eventCoverPhotoImageView;
    }

    public void setEventCoverPhotoImageView(ImageView eventCoverPhotoImageView) {
        this.eventCoverPhotoImageView = eventCoverPhotoImageView;
    }

    public TextView getCircleDateTextView() {
        return circleDateTextView;
    }

    public void setCircleDateTextView(TextView circleDateTextView) {
        this.circleDateTextView = circleDateTextView;
    }

    public TextView getCircleMonthTextView() {
        return circleMonthTextView;
    }

    public void setCircleMonthTextView(TextView circleMonthTextView) {
        this.circleMonthTextView = circleMonthTextView;
    }

    public TextView getInviteStatusTextView() {
        return inviteStatusTextView;
    }

    public void setInviteStatusTextView(TextView inviteStatusTextView) {
        this.inviteStatusTextView = inviteStatusTextView;
    }

    public TextView getEventAddressTextView() {
        return eventAddressTextView;
    }

    public void setEventAddressTextView(TextView eventAddressTextView) {
        this.eventAddressTextView = eventAddressTextView;
    }

    public LinearLayout getLocationDisplayLinearLayout() {
        return locationDisplayLinearLayout;
    }

    public void setLocationDisplayLinearLayout(LinearLayout locationDisplayLinearLayout) {
        this.locationDisplayLinearLayout = locationDisplayLinearLayout;
    }

    public LinearLayout getStartDateAndTimeDisplayLinearLayout() {
        return startDateAndTimeDisplayLinearLayout;
    }

    public void setStartDateAndTimeDisplayLinearLayout(LinearLayout startDateAndTimeDisplayLinearLayout) {
        this.startDateAndTimeDisplayLinearLayout = startDateAndTimeDisplayLinearLayout;
    }
}
