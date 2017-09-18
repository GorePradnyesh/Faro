package com.zik.faro.frontend.handlers;

import android.content.Context;

import com.google.common.collect.Lists;
import com.zik.faro.data.FaroImageBase;
import com.zik.faro.frontend.ui.adapters.ImageAdapter;

import java.util.List;

/**
 * Created by granganathan on 2/25/17.
 */

public class ImagesListHandler {
    private List<FaroImageBase> faroImages = Lists.newArrayList();

    private static ImagesListHandler imagesListHandler = null;
    private static ImageAdapter imageAdapter;

    public static ImagesListHandler initializeInstance(Context context) {
        synchronized (ImagesListHandler.class) {
            if (imagesListHandler == null) {
                imagesListHandler = new ImagesListHandler();
            }
        }

        imageAdapter = new ImageAdapter(context);

        return imagesListHandler;
    }

    public static ImagesListHandler getInstance() {
        return imagesListHandler;
    }

    private ImagesListHandler() {}

    public List<FaroImageBase> getFaroImages() {
        return faroImages;
    }

    public void setFaroImages(List<FaroImageBase> faroImages) {
        this.faroImages = faroImages;
    }

    public void addImages(List<String> imageUrls) {
        imageAdapter.addAll(imageUrls);
        imageAdapter.notifyDataSetChanged();
    }

    public ImageAdapter getImageAdapter() {
        return imageAdapter;
    }


}
