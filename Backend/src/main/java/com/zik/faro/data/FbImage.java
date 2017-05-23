package com.zik.faro.data;


import java.net.URL;
import java.util.Date;

/**
 * Created by granganathan on 12/24/16.
 */
public class FbImage extends FaroImageBase {
    public FbImage() {
        setImageProvider(ImageProvider.FACEBOOK);
    }

    public FbImage withImageName(String imageName) {
        setImageName(imageName);
        return this;
    }

    public FbImage withAlbumName(String albumName) {
        setAlbumName(albumName);
        return this;
    }

    public FbImage withCreatedTime(String createdTime) {
        setCreatedTime(createdTime);
        return this;
    }

    public FbImage withFaroUserId(String faroUserId) {
        setFaroUserId(faroUserId);
        return this;
    }

    public FbImage withEventId(String eventId) {
        setEventId(eventId);
        return this;
    }

    public FbImage withHeight(Integer height) {
        setHeight(height);
        return this;
    }

    public FbImage withWidth(Integer width) {
        setWidth(width);
        return this;
    }

    public FbImage withPublicUrl(URL url) {
        setPublicUrl(url);
        return this;
    }

}
