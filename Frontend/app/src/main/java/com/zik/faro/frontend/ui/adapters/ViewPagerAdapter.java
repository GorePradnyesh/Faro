package com.zik.faro.frontend.ui.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.zik.faro.frontend.ui.EventTabType;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        String title = (String) getPageTitle(position);
        Bundle bundle = new Bundle();
        String eventTabString = null;
        switch (title) {
            case "UPCOMING":
                eventTabString = EventTabType.UPCOMING.toString();
                break;
            case "PAST":
                eventTabString = EventTabType.PAST.toString();
                break;
            case "NOT RESPONDED":
                eventTabString = EventTabType.NOT_RESPONDED.toString();
                break;
        }

        bundle.putString("EVENT_TAB_TYPE", eventTabString);
        Fragment fragment = mFragmentList.get(position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
