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
import com.zik.faro.data.Poll;
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

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.poll_create_new_page_row_style, parent, false);
            holder.OPTION_DESCRPTN = (TextView)row.findViewById(R.id.optionDescription);
            holder.DELETE_OPTION = (ImageButton) row.findViewById(R.id.deleteOption);
            row.setTag(holder);
        }else{
            holder = (ImgHolder) row.getTag();
        }
        PollOption pollOption = (PollOption) getItem(position);
        holder.OPTION_DESCRPTN.setText(pollOption.getOption());
        holder.DELETE_OPTION.setImageResource(R.drawable.delete);
        holder.DELETE_OPTION.setId(position);
        holder.DELETE_OPTION.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton imageButton = (ImageButton)v;
                int position = imageButton.getId();
                list.remove(getItem(position));
                notifyDataSetChanged();
            }
        });
        return row;
    }
}
