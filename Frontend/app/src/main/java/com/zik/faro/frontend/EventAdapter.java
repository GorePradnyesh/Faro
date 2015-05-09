package com.zik.faro.frontend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by nakulshah on 1/8/15.
 */
public class EventAdapter extends ArrayAdapter {
    //TODO (Code Review) Implement sorted list instead of Linkedlist
    public List<Event> list = new LinkedList<>();

    DateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
    DateFormat stf = new SimpleDateFormat("hh:mm a");

    public EventAdapter(Context context, int resource) {
        super(context, resource);
    }


    public int getCount() {
        return this.list.size();
    }

    public void insert(Event event, int index) {
        list.add(index, event);
        super.insert(event, index);
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    static class ImgHolder{
        ImageView STATUS_IMG;
        TextView EVNT_NAME;
        TextView EVNT_START_TV;
        TextView EVNT_START_DATE;
        TextView EVNT_START_TIME;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ImgHolder holder = new ImgHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.event_row_style, parent, false);
            holder.EVNT_NAME = (TextView) row.findViewById(R.id.event_name);
            holder.STATUS_IMG = (ImageView) row.findViewById(R.id.status_img);
            holder.EVNT_START_TV = (TextView)row.findViewById(R.id.startDateTextView);
            holder.EVNT_START_DATE = (TextView)row.findViewById(R.id.startDate);
            holder.EVNT_START_TIME = (TextView)row.findViewById(R.id.startTime);
            row.setTag(holder);
        }else{
            holder = (ImgHolder) row.getTag();
        }
        Event EVENT = (Event) getItem(position);
        holder.EVNT_NAME.setText(EVENT.getEventName());
        holder.STATUS_IMG.setImageResource(EVENT.getImgResource());
        holder.EVNT_START_DATE.setText(sdf.format(EVENT.getStartDateCalendar().getTime()));
        holder.EVNT_START_TIME.setText(stf.format(EVENT.getStartDateCalendar().getTime()));
        return row;
    }
}
