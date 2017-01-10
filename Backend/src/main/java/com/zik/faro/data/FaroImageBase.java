package com.zik.faro.data;


import java.net.URL;
import java.util.Date;


/**
 * Created by granganathan on 12/31/16.
 */
public class FaroImageBase implements FaroImage {
    private String imageName;
    private String albumName;
    private Date createdTime;
    private String faroUserId;
    private String eventId;
    private Integer height;
    private Integer width;
    private URL publicUrl;
    private ImageProvider imageProvider;

    public FaroImageBase () {
    }

    public FaroImageBase withImageName(String imageName) {
        setImageName(imageName);
        return this;
    }

    public FaroImageBase withAlbumName(String albumName) {
        setAlbumName(albumName);
        return this;
    }

    public FaroImageBase withCreatedTime(Date createdTime) {
        setCreatedTime(createdTime);
        return this;
    }

    public FaroImageBase withFaroUserId(String faroUserId) {
        setFaroUserId(faroUserId);
        return this;
    }

    public FaroImageBase withEventId(String eventId) {
        setEventId(eventId);
        return this;
    }

    public FaroImageBase withHeight(Integer height) {
        setHeight(height);
        return this;
    }

    public FaroImageBase withWidth(Integer width) {
        setWidth(width);
        return this;
    }

    public FaroImageBase withPublicUrl(URL url) {
        setPublicUrl(url);
        return this;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public void setFaroUserId(String faroUserId) {
        this.faroUserId = faroUserId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setPublicUrl(URL publicUrl) {
        this.publicUrl = publicUrl;
    }

    public void setImageProvider(ImageProvider imageProvider) { this.imageProvider = imageProvider; }

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public String getAlbumName() {
        return albumName;
    }

    @Override
    public Date getCreatedTime() {
        return createdTime;
    }

    @Override
    public String getFaroUserId() {
        return faroUserId;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public Integer getHeight() {
        return height;
    }

    @Override
    public Integer getWidth() {
        return width;
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
