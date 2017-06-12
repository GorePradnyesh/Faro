package com.zik.faro.frontend;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zik.faro.data.InviteeList;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class EventFriendAdapter extends ArrayAdapter {
    public List<InviteeList.Invitees> list = new LinkedList<>();

    public EventFriendAdapter(Context context, int resource) {
        super(context, resource);
    }

    public List<InviteeList.Invitees> getList() {
        return list;
    }

    public int getCount() {
        return this.list.size();
    }

    public void insert(InviteeList.Invitees invitees, int index) {
        list.add(index, invitees);
        Collections.sort(list, new Comparator<InviteeList.Invitees>() {
            @Override
            public int compare(InviteeList.Invitees lhs, InviteeList.Invitees rhs) {
                if (lhs.getFirstName() != null && rhs.getFirstName() != null) {
                    return lhs.getFirstName().compareTo(rhs.getFirstName());
                } else {
                    return lhs.getEmail().compareTo(lhs.getEmail());
                }
            }
        });
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    static class ImgHolder{
        ImageView userPicture;
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
        EventFriendAdapter.ImgHolder holder = new EventFriendAdapter.ImgHolder();
        InviteeList.Invitees invitees = null;

        switch ((String)parent.getTag()){
            case "EditAssignment":
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = inflater.inflate(R.layout.friend_row_style, parent, false);
                    holder.friendName = (TextView)row.findViewById(R.id.friendName);
                    holder.userPicture = (ImageView) row.findViewById(R.id.userPicture);
                    row.setTag(holder);
                }else{
                    holder = (EventFriendAdapter.ImgHolder) row.getTag();
                }
                invitees = (InviteeList.Invitees) getItem(position);
                if (invitees != null) {
                    if (invitees.getFirstName() != null) {
                        holder.friendName.setText(invitees.getFirstName());
                    }else{
                        holder.friendName.setText(invitees.getEmail());
                    }
                    holder.friendName.setTextColor(Color.BLACK);
                    holder.userPicture.setVisibility(View.GONE);
                }
                break;
            default:
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = inflater.inflate(R.layout.friend_row_style, parent, false);
                    holder.userPicture = (ImageView) row.findViewById(R.id.userPicture);

                    holder.friendName = (TextView)row.findViewById(R.id.friendName);
                    row.setTag(holder);
                }else{
                    holder = (EventFriendAdapter.ImgHolder) row.getTag();
                }
                invitees = (InviteeList.Invitees) getItem(position);
                if (invitees != null) {
                    if (invitees.getFirstName() != null) {
                        holder.friendName.setText(invitees.getFirstName());
                    }else{
                        holder.friendName.setText(invitees.getEmail());
                    }
                }
                holder.userPicture.setImageResource(R.drawable.user_pic);
        }
        return row;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        EventFriendAdapter.ImgHolder holder = new EventFriendAdapter.ImgHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.friend_row_style, parent, false);
            holder.friendName = (TextView)row.findViewById(R.id.friendName);
            holder.userPicture = (ImageView) row.findViewById(R.id.userPicture);
            row.setTag(holder);
        }else{
            holder = (EventFriendAdapter.ImgHolder) row.getTag();
        }
        InviteeList.Invitees invitees = (InviteeList.Invitees) getItem(position);
        if (invitees != null) {
            if (invitees.getFirstName() != null) {
                holder.friendName.setText(invitees.getFirstName());
            }else{
                holder.friendName.setText(invitees.getEmail());
            }
        }
        holder.friendName.setTextColor(Color.BLACK);
        holder.userPicture.setVisibility(View.GONE);
        return row;
    }
}
