package com.zik.faro.frontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.zik.faro.data.InviteeList;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

public class UserProfilePage extends AppCompatActivity {

    private String userName;
    private String userEmailID;
    private MinUser cloneMinUser;
    private InviteeList.Invitees cloneInvitee;
    private String eventID = null;
    private String inviteStatus = null;




    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

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
            eventID = extras.getString("eventID");
            inviteStatus = extras.getString("inviteStatus");
            if (userEmailID.equals(myUserId)){
                //TOdo: Store my User's Fullname in faroUser context
                userName = userEmailID;
            }else if (eventID != null && inviteStatus != null) {
                cloneInvitee = eventFriendListHandler.getInviteesCloneFromMap(userEmailID);
                userName = eventFriendListHandler.getFriendFullNameFromID(userEmailID);
                userProfilePicture.setImageResource(R.drawable.user_pic);
            }else {
                cloneMinUser = userFriendListHandler.getMinUserCloneFromMap(userEmailID);
                userName = userFriendListHandler.getFriendFullNameFromID(cloneMinUser.getEmail());
                userProfilePicture.setImageResource(R.drawable.user_pic);
            }

            userNameTextView.setText(userName);
            userEmailIDTextView.setText("Email: " + userEmailID);
        }
    }
}
