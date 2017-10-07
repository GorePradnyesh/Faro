package com.zik.faro.frontend.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.squareup.okhttp.Request;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.handlers.UserFriendListHandler;
import com.zik.faro.frontend.ui.EventTabType;
import com.zik.faro.frontend.ui.activities.CreateNewEventActivity;
import com.zik.faro.frontend.ui.adapters.ViewPagerAdapter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlansFragment extends Fragment {

    private FragmentActivity mContext;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    //private Toolbar toolbar;
    private FloatingActionButton createNewEventFloatingActionButton = null;
    private Intent createNewEventIntent = null;
    private LinearLayout linlaHeaderProgress = null;
    private CoordinatorLayout fragmentPlansCoordinatorLayout = null;
    private EventListHandler eventListHandler = EventListHandler.getInstance(getActivity());
    private UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private static String TAG = "PlansFragment";

    public PlansFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context mContext = this.getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plans, container, false);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);

        fragmentPlansCoordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.fragmentPlansCoordinatorLayout);
        fragmentPlansCoordinatorLayout.setVisibility(View.GONE);
        linlaHeaderProgress = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        /*toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        */

        createNewEventFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.createNewEventFloatingActionButton);
        createNewEventIntent = new Intent(getActivity(), CreateNewEventActivity.class);
        createNewEventFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(createNewEventIntent);
            }
        });

        FragmentManager fragManager = getChildFragmentManager();

        viewPagerAdapter = new ViewPagerAdapter(fragManager);

        tabLayout.setupWithViewPager(viewPager);

        //tabLayout.setTabTextColors(ContextCompat.getColorStateList(mContext, R.color.tab_selector));
        //tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(mContext, R.color.colorAccent));

        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        getEventsFromServer();

        //Let this api call be on EventListTabFragment and not on FriendListFragment since this is where the
        // cache for friendlist is populated which is later used when inviting friends for
        // events. If the user never goes to the FriendListFragment then this cache would not be
        // populated.
        getFriendsFromServer();

        return view;
    }

    public void hideFloatingButton () {
        createNewEventFloatingActionButton.hide();
    }

    public void showFloatingButton () {
        createNewEventFloatingActionButton.show();
    }

    void setupViewPager () {
        //Setup the page only after response for both eventlist and friendlist is received.
        if (!(eventListHandler.isReceivedEvents() && userFriendListHandler.isReceivedFriends())) return;

        viewPagerAdapter.addFragment(new EventListTabFragment(), "UPCOMING");
        viewPagerAdapter.addFragment(new EventListTabFragment(), "PAST");
        viewPagerAdapter.addFragment(new EventListTabFragment(), "NOT RESPONDED");
        viewPager.setAdapter(viewPagerAdapter);

        linlaHeaderProgress.setVisibility(View.GONE);
        fragmentPlansCoordinatorLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mContext = (FragmentActivity) context;
        }
    }

    private void getEventsFromServer() {
        eventListHandler.setReceivedEvents(false);
        //Make API calls to get events and also friend List for the user when he first logs in to set up the user completely
        serviceHandler.getEventHandler().getEvents(new BaseFaroRequestCallback<List<EventInviteStatusWrapper>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get event list");
            }
            @Override
            public void onResponse(final List<EventInviteStatusWrapper> eventInviteStatusWrappers, HttpError error) {
                if (error == null ) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received events from the server!!");
                            eventListHandler.addDownloadedEventsToListAndMap(eventInviteStatusWrappers);
                            eventListHandler.setReceivedEvents(true);
                            setupViewPager();
                        }
                    });
                }else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        });
    }

    public void getFriendsFromServer() {
        userFriendListHandler.setReceivedFriends(false);
        serviceHandler.getFriendsHandler().getFriends(new BaseFaroRequestCallback<List<MinUser>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get friend list");
            }

            @Override
            public void onResponse(final List<MinUser> minUsers, HttpError error) {
                if (error == null ) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received friends from the server!!");
                            userFriendListHandler.addDownloadedFriendsToListAndMap(minUsers);
                            userFriendListHandler.setReceivedFriends(true);
                            setupViewPager();
                        }
                    });
                }else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        });
    }
}