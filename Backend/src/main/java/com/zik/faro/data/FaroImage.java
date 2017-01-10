package com.zik.faro.data;

import java.net.URL;
import java.util.Date;

/**
 * Created by granganathan on 12/24/16.
 */
public interface FaroImage {
    String getImageName();
    String getAlbumName();
    Date getCreatedTime();
    String getFaroUserId();
    String getEventId();
    Integer getHeight();
    Integer getWidth();
    URL getPublicUrl();
    ImageProvider getImageProvider();
}
