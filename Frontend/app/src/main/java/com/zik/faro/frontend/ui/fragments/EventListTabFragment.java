package com.zik.faro.frontend.ui.fragments;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.ui.EventTabType;
import com.zik.faro.frontend.ui.adapters.EventRecyclerViewAdapter;
import com.zik.faro.frontend.ui.adapters.RecyclerRowItemOnClickListener;
import com.zik.faro.frontend.util.FaroExceptionHandler;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.handlers.UserFriendListHandler;
import com.zik.faro.frontend.ui.activities.EventLandingPage;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

public class EventListTabFragment extends Fragment {

    private static String TAG = "EventListTabFragment";
    private Context mContext;
    private PlansFragment parentFrag;
    private int mLastFirstVisibleItem;
    private EventTabType eventTabType = null;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private EventListHandler eventListHandler = EventListHandler.getInstance(getActivity());
    private EventRecyclerViewAdapter eventRecyclerViewAdapter;


    public EventListTabFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this.getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle extras = getArguments();
        eventTabType = EventTabType.valueOf(extras.getString("EVENT_TAB_TYPE"));

        View fragmentView = inflater.inflate(R.layout.fragment_event_list_tab, container, false);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));

        mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);

        parentFrag = ((PlansFragment)EventListTabFragment.this.getParentFragment());
        parentFrag.showFloatingButton();

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        eventRecyclerViewAdapter = eventListHandler.getEventAdapter(eventTabType);
        eventRecyclerViewAdapter.setRowItemOnClickListener(new RecyclerRowItemOnClickListener() {
            @Override
            public void onItemClickListener(int position) {
                final Intent eventLandingIntent = new Intent(mContext, EventLandingPage.class);
                Event event = eventRecyclerViewAdapter.getEventAtPostition(position);
                FaroIntentInfoBuilder.eventIntent(eventLandingIntent, event.getId());
                //TODO: check if startActivityForResult is a better way
                mContext.startActivity(eventLandingIntent);
            }
        });
        mRecyclerView.setAdapter(eventRecyclerViewAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int currentFirstVisible = mLayoutManager.findFirstVisibleItemPosition();
                if(dy > 0) {
                    parentFrag.hideFloatingButton();
                }
                else if (dy <= 0) {
                    parentFrag.showFloatingButton();
                }

                mLastFirstVisibleItem = currentFirstVisible;


            }

            /*TODO Make call to server to get events after the last event. Send the date
            of the last event and also send the eventId to resolve sorting conflicts.
            */

        });

   /*
        //ImageButton calendar_view = (ImageButton)view.findViewById(R.id.calendarViewButton);
        //calendar_view.setImageResource(R.drawable.calendar);

        //TODO Implement function to refresh event list. Watch Vipul Shah's video on youtube. The
        //link is https://www.youtube.com/watch?v=Phc9tVSG6Aw

        //Listener to change the view from List View to Calendar View
        /*calendar_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(eventCalendarView);
                //finish();
            }
        });*/

        return fragmentView;
    }

    @Override
    public void onResume() {
        mRecyclerView.getAdapter().notifyDataSetChanged();
        super.onResume();
    }
}
