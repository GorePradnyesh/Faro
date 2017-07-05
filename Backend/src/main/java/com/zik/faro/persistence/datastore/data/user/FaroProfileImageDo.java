package com.zik.faro.persistence.datastore.data.user;

import com.zik.faro.data.ImageProvider;

import java.net.URL;

/**
 * Created by gaurav on 6/14/17.
 */
public interface FaroProfileImageDo {
    URL getPublicUrl();
    ImageProvider getImageProvider();
    boolean isThumbnail();
}
