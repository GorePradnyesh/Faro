package com.zik.faro.frontend;

import com.google.common.collect.Lists;
import com.zik.faro.data.FaroImageBase;

import java.util.List;

/**
 * Created by granganathan on 2/25/17.
 */

public class ImagesListHandler {
    private List<FaroImageBase> faroImages = Lists.newArrayList();

    private static ImagesListHandler imagesListHandler = null;

    public static ImagesListHandler getInstance() {

        synchronized (ImagesListHandler.class) {
            if (imagesListHandler == null) {
                imagesListHandler = new ImagesListHandler();
            }
        }

        return imagesListHandler;
    }

    private ImagesListHandler() {}

    public List<FaroImageBase> getFaroImages() {
        return faroImages;
    }

    public void setFaroImages(List<FaroImageBase> faroImages) {
        this.faroImages = faroImages;
    }
}
