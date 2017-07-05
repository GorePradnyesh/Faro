package com.zik.faro.data.user;

import com.zik.faro.data.ImageProvider;

import java.net.URL;

/**
 * Created by gaurav on 6/16/17.
 */
public class SmallProfileImage extends ProfileImage {

    public SmallProfileImage(URL publicUrl, ImageProvider imageProvider) {
        super(publicUrl, imageProvider);
    }

    public SmallProfileImage() {}

    @Override
    public boolean isThumbnail() {
        return true;
    }
}
