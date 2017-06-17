package com.zik.faro.persistence.datastore.data.user;

import com.zik.faro.data.ImageProvider;

import java.net.URL;

/**
 * Created by gaurav on 6/14/17.
 */
public class LargeProfileImageDo extends ProfileImageDo {

    public LargeProfileImageDo(URL publicUrl, ImageProvider imageProvider) {
        super(publicUrl, imageProvider);
    }

    public LargeProfileImageDo() {}

    @Override
    public boolean isThumbnail() {
        return false;
    }
}
