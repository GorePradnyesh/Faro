package com.zik.faro.frontend;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.squareup.okhttp.Request;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

/**
 * Created by gaurav on 7/5/17.
 */

public class AddPhoneContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private ListView contactsFriendsList;
    private Button addContactsFriendsDoneButton;
    private Map<String, MinUser> selectedFriends = Maps.newHashMap();
    private Activity addFriendsActivity;

    // An adapter that binds the result Cursor to the ListView
    private CursorAdapter cursorAdapter;

    // Define a projection
    private static final String[] PROJECTION = {Contacts._ID, Contacts.DISPLAY_NAME_PRIMARY, Contacts.PHOTO_THUMBNAIL_URI,
            CommonDataKinds.Email.DATA};

    // Defines the text expression
    private static final String SELECTION = Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" ;
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = new String[1];

    private String TAG = "PhoneContactsFragment";
    private final int PERMISSIONS_REQ_CONTACTS = 1;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         // Make search string into pattern by inserting "%" characters to represent a sequence of zero or more chars
         // and store it in the selection array
        mSelectionArgs[0] = "%";
        // Starts the query
        return new CursorLoader(
                getActivity(),
                CommonDataKinds.Email.CONTENT_URI,
                PROJECTION,
                SELECTION,
                mSelectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Loader framework calls onLoadFinished() when the Contacts Provider returns the results of the query.
        // In this method, put the result Cursor in the SimpleCursorAdapter. This automatically updates the ListView with the search results:
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // onLoaderReset() is invoked when the loader framework detects that the result Cursor contains stale data.
        // Delete the SimpleCursorAdapter reference to the existing Cursor. If you don't, the loader framework will not recycle the Cursor, which causes a memory leak.
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        removeAllSelectedFriends();

        // Inflate the fragment layout
        View fragmentView = inflater.inflate(R.layout.fragment_contacts_list, container, false);

        contactsFriendsList = (ListView) fragmentView.findViewById(R.id.contactsFriendsList);
        contactsFriendsList.setBackgroundColor(Color.BLUE);

        addContactsFriendsDoneButton = (Button) fragmentView.findViewById(R.id.addContactFriendsDoneButton);
        addContactsFriendsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectedFriends.isEmpty()){
                    addFriends();
                }

                // Complete this Activity and go back to the previous page
                getActivity().finish();
            }
        });
        addContactsFriendsDoneButton.setVisibility(View.GONE);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addFriendsActivity = getActivity();

        // Get a CursorAdapter
        cursorAdapter =  new PhoneContactsAdapter(getActivity(), null, 0);

        // Set the adapter for the ListView
        contactsFriendsList.setAdapter(cursorAdapter);

        if (checkContactsPermission()) {
            // Initialize the loader
            getLoaderManager().initLoader(0, null, this);
        }
    }

    private void addFriends() {
        for (MinUser minUser : selectedFriends.values()) {
            final String friendEmailId = minUser.getEmail();
            String myUserId = FaroUserContext.getInstance().getEmail();

            if (friendEmailId.equals(myUserId)) {
                // TODO: Add error popUp with "Cant invite self message"
                return;
            }

            FaroServiceHandler.getFaroServiceHandler().getFriendsHandler().inviteFriend(new BaseFaroRequestCallback<String>() {
                @Override
                public void onFailure(Request request, IOException ex) {
                    Log.e(TAG, MessageFormat.format("failed to send friend invite request for friend {0}", friendEmailId));
                }

                @Override
                public void onResponse(String email, HttpError httpError) {
                    if (httpError == null ) {
                        Handler mainHandler = new Handler(addFriendsActivity.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, MessageFormat.format("Friend invite for {0} succeeded", friendEmailId));
                                // TODO it would be better if we return the MinUser object instead of a String
                                MinUser minUser = new MinUser().withEmail(friendEmailId);
                                UserFriendListHandler.getInstance().addFriendToListAndMap(minUser);
                            }
                        });
                    } else {
                        Log.e(TAG, MessageFormat.format("Failed to invite friend {0}. code = {1}, message = {2}", friendEmailId, httpError.getCode(), httpError.getMessage()));
                    }
                }
            }, friendEmailId);
        }
    }

    private void showAddFriendsDoneButton(boolean visible) {
        if (visible) {
            // Hide the addByEmail button and then make the add friends button visible
            ((AddFriendsActivity) getActivity()).showAddByEmailButton(false);
            addContactsFriendsDoneButton.setVisibility(View.VISIBLE);
        } else {
            // Hide the add friends button and then make the addByEmail button visible
            addContactsFriendsDoneButton.setVisibility(View.GONE);
            ((AddFriendsActivity) getActivity()).showAddByEmailButton(true);
        }
    }

    private void addSelectedFriend(MinUser minUser) {
        selectedFriends.put(minUser.getEmail(), minUser);
        // If first friend is selected, show the add friends button and hide the add by email button
        if (selectedFriends.size() == 1 ) {
            showAddFriendsDoneButton(true);
        }
    }

    private void removeSelectedFriend(String email) {
        selectedFriends.remove(email);

        // If this was the last friend selected, show the add by email button and hide the add friends button
        if (selectedFriends.isEmpty()) {
            showAddFriendsDoneButton(false);
        }
    }

    private void removeAllSelectedFriends() {
        selectedFriends.clear();
    }

    private boolean checkContactsPermission() {
        // Beginning in Android 6.0 (API level 23), users grant permissions to
        // apps while the app is running, not when they install the app.

        // Check Permission to access to READ and WRITE Phone contacts for access if not already granted
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Show asynchronously, an explanation of why the permission is required
                /*if (ActivityCompat.shouldShowRequestPermissionRationale((android.app.Activity) mContext,
                        Manifest.permission.)) {
                }*/

                Log.i(TAG, "Requesting permission to access contacts");
                ActivityCompat.requestPermissions(getActivity(),
                        new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQ_CONTACTS);
                return false;
            }
        }

        return true;
    }

    private class PhoneContactsAdapter extends CursorAdapter {
        private LayoutInflater layoutInflater;

        public PhoneContactsAdapter(Context context, Cursor cursor, int flags) {
            super(context,cursor, flags);
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return layoutInflater.inflate(R.layout.add_friends_row_style, parent, false);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            TextView contactNameTextView = (TextView) view.findViewById(R.id.contactFriendName);
            String friendName = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY));
            Log.i(TAG, MessageFormat.format("friendName = {0}", friendName));

            if (!Strings.isNullOrEmpty(friendName)) {
                contactNameTextView.setText(friendName);
            }

            String photoUri = cursor.getString(cursor.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI));
            Log.i(TAG, MessageFormat.format("photoUri = {0}", photoUri));

            ImageView contactPhotoImageView = (ImageView) view.findViewById(R.id.contactFriendPicture);

            // Load the user profile picture if available, otherwise load the default pic
            Glide.with(getContext())
                    .load((photoUri != null) ? photoUri : R.drawable.user_pic)
                    .placeholder(R.drawable.user_pic)
                    .into(contactPhotoImageView);

            CheckBox contactFriendSelectionCheckBox = (CheckBox) view.findViewById(R.id.contactFriendSelection);
            contactFriendSelectionCheckBox.setTag(cursor.getPosition());
            contactFriendSelectionCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onItemClick");

                    CheckBox selectedCheckbox = (CheckBox) view;
                    cursor.moveToPosition((Integer) selectedCheckbox.getTag());
                    String contactEmail = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));

                    if (selectedCheckbox.isChecked()) {
                        String firstName = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY));

                        Log.i(TAG, MessageFormat.format("email = {0}, firstName = {1}", contactEmail, firstName));

                        addSelectedFriend(new MinUser().withEmail(contactEmail)
                                .withFirstName(firstName));
                    } else {
                        removeSelectedFriend(contactEmail);
                    }
                }
            });
        }
    }
}
