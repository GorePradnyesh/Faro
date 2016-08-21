package com.zik.faro.frontend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import com.zik.faro.data.PollOption;


public class PollOptionsAdapter extends ArrayAdapter {
    public List<PollOption> list = new LinkedList<>();

    public List<PollOption> getList() {
        return list;
    }

    public PollOptionsAdapter(Context context, int resource) {
        super(context, resource);
    }

    public int getCount() {
        return this.list.size();
    }

    public void insert(PollOption pollOption, int index) {
        list.add(index, pollOption);
        super.insert(pollOption, index);
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    static class ImgHolder{
        TextView OPTION_DESCRPTN;
        ImageButton DELETE_OPTION;
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

        PollOption pollOption = (PollOption) getItem(position);
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (pollOption.getId() != null){
            row = inflater.inflate(R.layout.poll_option_cant_edit_row_style, parent, false);
            holder.OPTION_DESCRPTN = (TextView)row.findViewById(R.id.optionDescription);
            row.setTag(holder);
        }else {
            row = inflater.inflate(R.layout.poll_option_can_edit_row_style, parent, false);
            holder.OPTION_DESCRPTN = (TextView)row.findViewById(R.id.optionDescription);
            holder.DELETE_OPTION = (ImageButton) row.findViewById(R.id.deleteOption);
            row.setTag(holder);
            holder.DELETE_OPTION.setImageResource(R.drawable.delete);
            holder.DELETE_OPTION.setId(position);
            holder.DELETE_OPTION.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton imageButton = (ImageButton) v;
                    int position = imageButton.getId();
                    PollOption removepollOption = (PollOption) getItem(position);
                    list.remove(removepollOption);
                    notifyDataSetChanged();
                }
            });
        }
        holder.OPTION_DESCRPTN.setText(pollOption.getOption());

        return row;
    }
}
