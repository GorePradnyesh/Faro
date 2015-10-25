package com.zik.faro.frontend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by nakulshah on 7/24/15.
 */
public class PollOptionsAdapter extends ArrayAdapter {
    public List<Poll.PollOption> list = new LinkedList<>();

    public PollOptionsAdapter(Context context, int resource) {
        super(context, resource);
    }

    public int getCount() {
        return this.list.size();
    }

    public void insert(Poll.PollOption pollOption, int index) {
        list.add(index, pollOption);
        super.insert(pollOption, index);
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    static class ImgHolder{
        TextView OPTION_DESCRPTN;
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
            row = inflater.inflate(R.layout.poll_create_new_page_row_style, parent, false);
            holder.OPTION_DESCRPTN = (TextView)row.findViewById(R.id.optionDescription);
            row.setTag(holder);
        }else{
            holder = (ImgHolder) row.getTag();
        }
        Poll.PollOption pollOption = (Poll.PollOption) getItem(position);
        holder.OPTION_DESCRPTN.setText(pollOption.getOptionDescription());
        return row;
    }
}
