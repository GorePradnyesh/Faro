package com.zik.faro.data.user;

import com.zik.faro.data.ImageProvider;
import java.net.URL;

/**
 * Created by gaurav on 6/16/17.
 */
public abstract class ProfileImage implements FaroProfileImage {
    protected URL publicUrl;
    protected ImageProvider imageProvider;

    protected ProfileImage() {}

    protected ProfileImage(URL publicUrl, ImageProvider imageProvider) {
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
