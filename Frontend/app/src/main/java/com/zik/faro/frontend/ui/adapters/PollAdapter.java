package com.zik.faro.frontend.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;
import com.zik.faro.data.Poll;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.SetDisplayProperties;

public class PollAdapter extends ArrayAdapter {
    public List<Poll> list = new LinkedList<>();

    public PollAdapter(Context context, int resource) {
        super(context, resource);
    }

    public int getCount() {
        return this.list.size();
    }

    public void insert(Poll poll, int index) {
        list.add(index, poll);
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    static class ImgHolder{
        TextView POLL_DESCRPTN;
        ImageView STATUS_IMG;
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
            row = inflater.inflate(R.layout.poll_list_page_row_style, parent, false);
            holder.POLL_DESCRPTN = (TextView)row.findViewById(R.id.pollDescription);
            holder.STATUS_IMG = (ImageView)row.findViewById(R.id.poll_status_img);
            row.setTag(holder);
        }else{
            holder = (ImgHolder) row.getTag();
        }
        Poll poll = (Poll) getItem(position);
        holder.POLL_DESCRPTN.setText(poll.getDescription());
        holder.STATUS_IMG.setImageResource(SetDisplayProperties.getPollStatusImage(poll));
        return row;
    }
}
