package com.zik.faro.data.user;

import com.zik.faro.data.ImageProvider;

import java.net.URL;

/**
 * Created by gaurav on 6/16/17.
 */
public class LargeProfileImage extends ProfileImage {
    public LargeProfileImage(URL publicUrl, ImageProvider imageProvider) {
        super(publicUrl, imageProvider);
    }

    public LargeProfileImage() {}

    @Override
    public boolean isThumbnail() {
        return false;
    }
}
