package com.zik.faro.frontend.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zik.faro.frontend.R;
import com.zik.faro.frontend.ui.activities.MoreOptionsPage;
import com.zik.faro.frontend.ui.adapters.ViewPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlansFragment extends Fragment {

    private FragmentTabHost mTabHost;
    private FragmentActivity myContext;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    public PlansFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plans, container, false);

        tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);

        FragmentManager fragManager = myContext.getSupportFragmentManager();

        viewPagerAdapter = new ViewPagerAdapter(fragManager);
        setupViewPager();

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setTabTextColors(ContextCompat.getColorStateList(myContext, R.color.tab_selector));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(myContext, R.color.colorAccent));

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

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

        return view;
    }

    void setupViewPager () {
        viewPagerAdapter.addFragment(new MoreOptionsPage(), "Upcoming");
        viewPagerAdapter.addFragment(new MoreOptionsPage(), "Past");
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            myContext = (FragmentActivity) context;
        }
    }
}