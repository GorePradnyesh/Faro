package com.zik.faro.frontend.ui.fragments;

/**
 * Created by granganathan on 2/25/17.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zik.faro.data.FaroImageBase;
import com.zik.faro.frontend.handlers.ImagesListHandler;
import com.zik.faro.frontend.R;

import java.text.MessageFormat;

/**
 * Created by granganathan on 2/25/17.
 */

public class ScreenSlidePageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_page, container, false);

        if(getArguments() != null) {
            int imagePosition = getArguments().getInt("imagePosition");

            FaroImageBase faroImage = ImagesListHandler.getInstance().getFaroImages().get(imagePosition);
            View screenSlideImageView = rootView.findViewById(R.id.screenSlideImageView);

            Glide.with(this)
                    .load(faroImage.getPublicUrl().toString())
                    .into((ImageView) screenSlideImageView);

            TextView screenSlideImageTextView = (TextView) rootView.findViewById(R.id.screenSlideImageText);
            screenSlideImageTextView.setText(MessageFormat.format("Uploaded on {0} \n By {1}",
                    faroImage.getCreatedTime(), faroImage.getFaroUserId()));

        } else {
            // TODO
        }

        return rootView;
    }


}


