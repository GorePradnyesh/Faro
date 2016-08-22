package com.zik.faro.frontend;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;

import java.util.LinkedList;
import java.util.List;

public class AssignmentAdapter extends ArrayAdapter{
    //TODO (Code Review) Implement sorted list instead of Linkedlist
    public List<Assignment> list = new LinkedList<>();

    public AssignmentAdapter(Context context, int resource) {
        super(context, resource);
    }

    public int getCount() {
        return this.list.size();
    }

    public void insert(Assignment assignment, int index) {
        list.add(index, assignment);
        super.insert(assignment, index);
    }

    static class ImgHolder{
        ImageView STATUS_IMG;
        TextView EVNT_NAME;
        TextView EVNT_START_DATE;
        TextView EVNT_START_TIME;
    }
    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }

}
