package com.zik.faro.frontend;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.squareup.okhttp.Request;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;

public class FriendListFragment extends Fragment {

    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();

    static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static FaroServiceHandler serviceHandler;
    private static String TAG = "FriendListFragment";

    private RelativeLayout popUpRelativeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);
        ImageButton inviteFriend = (ImageButton)view.findViewById(R.id.inviteFriend);
        inviteFriend.setImageResource(R.drawable.plus);

        serviceHandler = eventListHandler.serviceHandler;

        ListView friendListView  = (ListView)view.findViewById(R.id.friendList);
        friendListView.setBackgroundColor(Color.BLACK);
        friendListView.setAdapter(userFriendListHandler.userFriendAdapter);

        final Context mContext = this.getActivity();

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));

       /* friendListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Display Friend's info.
            }
        });*/

        popUpRelativeLayout = (RelativeLayout) view.findViewById(R.id.friendListFragment);

        inviteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.invite_friend_popup, null);
                final PopupWindow popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT, true);
                final EditText emailIDEditText = (EditText) container.findViewById(R.id.friend_email_id);
                final Button sendInvite = (Button) container.findViewById(R.id.send_invite_button);

                popupWindow.showAtLocation(popUpRelativeLayout, Gravity.CENTER, 0, 0);

                container.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return false;
                    }
                });

                emailIDEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        sendInvite.setEnabled(!(emailIDEditText.getText().toString().trim().isEmpty()));
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                sendInvite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO:Check if not inviting self. Check if entered emailID is not same as myUser ID
                        serviceHandler.getFriendsHandler().inviteFriend(new BaseFaroRequestCallback<String>() {
                            @Override
                            public void onFailure(Request request, IOException ex) {
                                Log.e(TAG, "failed to send friend invite request");
                            }

                            @Override
                            public void onResponse(String s, HttpError error) {
                                if (error == null ) {
                                    Runnable myRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.i(TAG, "Friend invite sent Successfully");
                                            //TODO it would be better if we return the MinUser object instead of a String
                                            MinUser minUser = new MinUser("", "", emailIDEditText.getText().toString());
                                            userFriendListHandler.addFriendToListAndMap(minUser);
                                            popupWindow.dismiss();
                                        }
                                    };
                                    Handler mainHandler = new Handler(mContext.getMainLooper());
                                    mainHandler.post(myRunnable);
                                }else {
                                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                                }
                            }
                        }, emailIDEditText.getText().toString());
                    }
                });
            }
        });
        return view;
    }
}
