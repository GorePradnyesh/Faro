package com.zik.faro.frontend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.zik.faro.data.MinUser;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class UserFriendAdapter extends ArrayAdapter {
    public List<MinUser> list = new LinkedList<>();

    public UserFriendAdapter(Context context, int resource) {
        super(context, resource);
    }

    public List<MinUser> getList() {
        return list;
    }

    public int getCount() {
        return this.list.size();
    }

    public void insert(MinUser minUser, int index) {
        list.add(index, minUser);
        Collections.sort(list, new Comparator<MinUser>() {
            @Override
            public int compare(MinUser lhs, MinUser rhs) {
                String name1 = null;
                String name2 = null;
                if (lhs.getFirstName() != null) {
                    name1 = lhs.getFirstName();
                }else {
                    name1 = lhs.getEmail();
                }
                if (rhs.getFirstName() != null){
                    name2 = rhs.getFirstName();
                }else{
                    name2 = rhs.getEmail();
                }
                return name1.compareTo(name2);
            }
        });
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
        UserFriendAdapter.ImgHolder holder = new UserFriendAdapter.ImgHolder();

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
