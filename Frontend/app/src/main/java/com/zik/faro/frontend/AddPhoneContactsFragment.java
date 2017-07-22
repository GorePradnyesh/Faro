package com.zik.faro.frontend;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.provider.ContactsContract.Contacts;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Created by gaurav on 7/5/17.
 */

public class AddPhoneContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private ListView contactsFriendsList;

    private final static String[] FROM_COLUMNS = {
            Contacts.DISPLAY_NAME_PRIMARY
    };

    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    private final static int[] TO_IDS = {
            R.id.friendName
    };

    // Define variables for the contact the user selects
    // The contact's _ID value
    private long contactId;

    // The contact's LOOKUP_KEY
    private String contactKey;

    // A content URI for the selected contact
    private Uri contactUri;

    // An adapter that binds the result Cursor to the ListView
    private SimpleCursorAdapter cursorAdapter;

    // Define a projection
    private static final String[] PROJECTION = {Contacts._ID, Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME_PRIMARY};

    // Column indexes in the cursor. Indexes are the same as the order of the column names in the projection
    private static final int CONTACT_ID_INDEX = 0;
    private static final int LOOKUP_KEY_INDEX = 1;

    // Defines the text expression
    private static final String SELECTION = Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" ;
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = new String[1];

    private String TAG = "PhoneContactsFragment";
    private final int PERMISSIONS_REQ_CONTACTS = 1;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        /*
         * Make search string into pattern by inserting "%" characters to represent a sequence of zero or more chars
         * and store it in the selection array
         */
        mSelectionArgs[0] = "%";
        // Starts the query
        return new CursorLoader(
                getActivity(),
                Contacts.CONTENT_URI,
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_contacts_list, container, false);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        contactsFriendsList = (ListView) getActivity().findViewById(R.id.contactsFriendsList);
        contactsFriendsList.setBackgroundColor(Color.BLACK);

        // Get a CursorAdapter
        cursorAdapter = new SimpleCursorAdapter(
                getActivity(),  // context
                R.layout.friend_row_style, // layout
                null,  // cursor
                FROM_COLUMNS, // from
                TO_IDS, // to
                0); // flags

        // Set the adapter for the ListView
        contactsFriendsList.setAdapter(cursorAdapter);

        // Set the item click listener to be the current fragment.
        // contactsFriendsList.setOnItemClickListener(this);

        if (checkContactsPermission()) {
            // Initialize the loader
            getLoaderManager().initLoader(0, null, this);
        }
    }
}
