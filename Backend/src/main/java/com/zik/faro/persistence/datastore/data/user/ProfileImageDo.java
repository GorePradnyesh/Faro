package com.zik.faro.persistence.datastore.data.user;

import com.zik.faro.data.ImageProvider;
import com.zik.faro.data.user.ProfileImage;

import java.net.URL;

/**
 * Created by gaurav on 6/14/17.
 */
public abstract class ProfileImageDo implements FaroProfileImageDo {
    protected URL publicUrl;
    protected ImageProvider imageProvider;

    protected ProfileImageDo() {}

    protected ProfileImageDo(URL publicUrl, ImageProvider imageProvider) {
        this.publicUrl = publicUrl;
        this.imageProvider = imageProvider;
    }

    @Override
    public URL getPublicUrl() {
        return publicUrl;
    }

    @Override
    public ImageProvider getImageProvider() {
        return imageProvider;
    }

}
