package com.zik.faro.frontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

public class UserProfilePage extends AppCompatActivity {
    private String userName;
    private String userEmailID;
    private MinUser cloneMinUser;

    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();

    FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_page);

        ImageView userProfilePicture = (ImageView) findViewById(R.id.userPicture);
        TextView userNameTextView = (TextView) findViewById(R.id.userName);
        TextView userEmailIDTextView = (TextView) findViewById(R.id.userEmailID);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userEmailID = extras.getString("userEmailID");
            if (userEmailID.equals(myUserId)) {
                //TOdo: Store my User's Fullname in faroUser context
                userName = userEmailID;
            } else {
                cloneMinUser = userFriendListHandler.getMinUserCloneFromMap(userEmailID);
                userName = userFriendListHandler.getFriendFullNameFromID(cloneMinUser.getEmail());
            }

            // TODO : Load the user's profile picture
            /*Glide.with(getApplicationContext())
                    .load((cloneMinUser.getPictureUrl() != null) ? cloneMinUser.getPictureUrl() : R.drawable.user_pic)
                    .into((userProfilePicture));*/
            userProfilePicture.setImageResource(R.drawable.user_pic);

            userNameTextView.setText(userName);
            userEmailIDTextView.setText("Email: " + userEmailID);
        }
    }
}
