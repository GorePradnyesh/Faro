package com.zik.faro.persistence.datastore.data.user;

import com.zik.faro.data.ImageProvider;

import java.net.URL;

/**
 * Created by gaurav on 6/14/17.
 */
public class SmallProfileImageDo extends ProfileImageDo {

    public SmallProfileImageDo(URL publicUrl, ImageProvider imageProvider) {
        super(publicUrl, imageProvider);
    }

    public SmallProfileImageDo() {}

    @Override
    public boolean isThumbnail() {
        return true;
    }

}
