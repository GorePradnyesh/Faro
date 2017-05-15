package com.zik.faro.frontend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zik.faro.data.Activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class ActivityAdapter extends ArrayAdapter{

    //TODO (Code Review) Implement sorted list instead of Linkedlist
    public List<Activity> list = new LinkedList<>();

    DateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy");
    DateFormat stf = new SimpleDateFormat("hh:mm a");

    public ActivityAdapter(Context context, int resource) {
        super(context, resource);
    }

    public int getCount() {
        return this.list.size();
    }

    public void insert(Activity activity, int index) {
        list.add(index, activity);
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    static class ImgHolder {
        TextView ACTIVITY_NAME;
        TextView ACTIVITY_START_DATE;
        TextView ACTIVITY_START_TIME;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ImgHolder holder = new ImgHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.activity_row_style, parent, false);
            holder.ACTIVITY_NAME = (TextView) row.findViewById(R.id.activityName);
            holder.ACTIVITY_START_DATE = (TextView)row.findViewById(R.id.startDate);
            holder.ACTIVITY_START_TIME = (TextView)row.findViewById(R.id.startTime);
            row.setTag(holder);
        } else {
            holder = (ImgHolder) row.getTag();
        }
        Activity activity = (Activity) getItem(position);
        holder.ACTIVITY_NAME.setText(activity.getName());
        holder.ACTIVITY_START_DATE.setText(sdf.format(activity.getStartDate().getTime()));
        holder.ACTIVITY_START_TIME.setText(stf.format(activity.getStartDate().getTime()));
        return row;
    }

}
