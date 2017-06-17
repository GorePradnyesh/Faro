package com.zik.faro.frontend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.zik.faro.data.MinUser;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class UserFriendAdapter extends ArrayAdapter<MinUser> {
    private List<MinUser> minUsers = new LinkedList<>();

    public UserFriendAdapter(Context context, int resource) {
        super(context, resource);
    }

    public List<MinUser> getMinUsers() {
        return minUsers;
    }

    @Override
    public int getCount() {
        return minUsers.size();
    }

    @Override
    public void insert(MinUser minUser, int index) {
        minUsers.add(index, minUser);
        Collections.sort(minUsers, new Comparator<MinUser>() {
            @Override
            public int compare(MinUser lhs, MinUser rhs) {
                String name1 = null;
                String name2 = null;

                if (lhs.getFirstName() != null) {
                    name1 = lhs.getFirstName();
                } else {
                    name1 = lhs.getEmail();
                }

                if (rhs.getFirstName() != null){
                    name2 = rhs.getFirstName();
                } else {
                    name2 = rhs.getEmail();
                }

                return name1.compareTo(name2);
            }
        });
    }

    @Override
    public MinUser getItem(int position) {
        return minUsers.get(position);
    }

    static class ImgHolder {
        private ImageView userPictureImageView;
        private TextView friendNameTextView;

        public ImgHolder(View rowInListView) {
            this.userPictureImageView = (ImageView)rowInListView.findViewById(R.id.userPicture);
            this.friendNameTextView = (TextView)rowInListView.findViewById(R.id.friendName);
        }

        public void setFriendName(String friendName) {
            friendNameTextView.setText(friendName);
        }

        public ImageView getImageView() {
            return userPictureImageView;
        }
    }


    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        UserFriendAdapter.ImgHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.friend_row_style, parent, false);
            holder = new ImgHolder(row);
            row.setTag(holder);
        } else {
            holder = (ImgHolder) row.getTag();
        }

        MinUser minUser = (MinUser)getItem(position);
        if (minUser != null) {
            if (minUser.getFirstName() != null) {
                holder.setFriendName(minUser.getFirstName());
            } else {
                holder.setFriendName(minUser.getEmail());
            }
        }

        // Load the user profile picture if available otherwise load the default pic
        Glide.with(getContext())
                .load((minUser.getThumbProfileImageUrl() != null) ? minUser.getThumbProfileImageUrl() : R.drawable.user_pic)
                .placeholder(R.drawable.user_pic)
                .into(holder.getImageView());

        return row;
    }
}
