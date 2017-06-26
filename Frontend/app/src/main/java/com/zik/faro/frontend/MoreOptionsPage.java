package com.zik.faro.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;

import java.io.IOException;

public class MoreOptionsPage extends Fragment{

    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();
    private String TAG = "MoreOptions";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Intent LoginActivity = new Intent(getActivity(), com.zik.faro.frontend.LoginActivity.class);
        View v = inflater.inflate(R.layout.activity_more_options_page, container, false);
        Button logout = (Button)v.findViewById(R.id.logout);
        TextView tv = (TextView) v.findViewById(R.id.text);
        tv.setText("Logged in as " + myUserId);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Clear all App related info here
                eventListHandler.clearListAndMapOnLogout();
                userFriendListHandler.clearFriendListAndMap();

                //Delete Firebase token.
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                // Log out of facebook
                LoginManager.getInstance().logOut();

                TokenCache.getTokenCache().deleteToken();

                startActivity(LoginActivity);
                getActivity().finish();

            }
        });
        return v;
    }
}
