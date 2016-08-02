package com.zik.faro.frontend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.zik.faro.data.Event;
import com.zik.faro.data.MinUser;

import java.util.LinkedList;
import java.util.List;

public class FriendAdapter extends ArrayAdapter {
    public List<MinUser> list = new LinkedList<>();


    public FriendAdapter(Context context, int resource) {
        super(context, resource);
    }

    public int getCount() {
        return this.list.size();
    }

    public void insert(MinUser minUser, int index) {
        list.add(index, minUser);
        super.insert(minUser, index);
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    static class ImgHolder{
        TextView friendName;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        FriendAdapter.ImgHolder holder = new FriendAdapter.ImgHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.friend_row_style, parent, false);
            holder.friendName = (TextView)row.findViewById(R.id.friendName);
            row.setTag(holder);
        }else{
            holder = (ImgHolder) row.getTag();
        }
        MinUser minUser = (MinUser)getItem(position);
        if (minUser != null) {
            if (minUser.getFirstName() != null) {
                holder.friendName.setText(minUser.getFirstName());
            }else{
                holder.friendName.setText(minUser.getEmail());
            }
        }
        return row;
    }
}
