package com.zik.faro.frontend.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.zik.faro.frontend.handlers.ImagesListHandler;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.ui.fragments.ScreenSlidePageFragment;

/**
 * Created by granganathan on 2/25/17.
 */

public class ScreenSlidePagerActivity extends FragmentActivity {

    /**
     * The pager widget, which handles animation and allows swiping horizontally
     */
    private ViewPager viewPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);

        // Instantiate a ViewPager and a PagerAdapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        // Set the current item of the view pager to the position of the image clicked
        // Get the image position passed in
        int imagePosition = getIntent().getIntExtra("imageIndex", 0);
        viewPager.setCurrentItem(imagePosition);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * A  pager adapter that represents ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ScreenSlidePageFragment screenSlidePageFragment = new ScreenSlidePageFragment();
            Bundle params = new Bundle();
            params.putInt("imagePosition", position);
            screenSlidePageFragment.setArguments(params);
            return screenSlidePageFragment;
        }

        @Override
        public int getCount() {
            return ImagesListHandler.getInstance().getFaroImages().size();
        }
    }
}
