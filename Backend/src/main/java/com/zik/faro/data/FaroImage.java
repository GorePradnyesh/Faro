package com.zik.faro.data;

import java.net.URL;

/**
 * Created by granganathan on 12/24/16.
 */
public interface FaroImage {
    String getImageName();
    String getAlbumName();
    String getCreatedTime();
    String getFaroUserId();
    String getEventId();
    Integer getHeight();
    Integer getWidth();
    URL getPublicUrl();
    ImageProvider getImageProvider();
}
