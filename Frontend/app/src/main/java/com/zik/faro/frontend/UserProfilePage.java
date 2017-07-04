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
    private String userEmailId;
    private MinUser cloneMinUser;
    private InviteeList.Invitees cloneInvitee;
    private String eventId = null;

    private UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    private FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_page);

        ImageView userProfilePicture = (ImageView) findViewById(R.id.userPicture);
        TextView userNameTextView = (TextView) findViewById(R.id.userName);
        TextView userEmailIdTextView = (TextView) findViewById(R.id.userEmailId);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userEmailId = extras.getString(FaroIntentConstants.EMAIL_ID);
            eventId = extras.getString(FaroIntentConstants.EVENT_ID);

            if (userEmailId.equals(myUserId)){
                //TOdo: Store my User's Fullname in faroUser context
                userName = userEmailId;
            }else if (eventId != null) {
                cloneInvitee = eventFriendListHandler.getInviteesCloneFromMap(userEmailId);
                userName = eventFriendListHandler.getFriendFullNameFromID(userEmailId);
                userProfilePicture.setImageResource(R.drawable.user_pic);
            }else {
                cloneMinUser = userFriendListHandler.getMinUserCloneFromMap(userEmailId);
                userName = userFriendListHandler.getFriendFullNameFromID(userEmailId);
                userProfilePicture.setImageResource(R.drawable.user_pic);
            }

            userNameTextView.setText(userName);
            userEmailIdTextView.setText("Email: " + userEmailId);
        }
    }
}
