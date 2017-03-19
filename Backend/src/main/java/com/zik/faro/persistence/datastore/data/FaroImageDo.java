package com.zik.faro.persistence.datastore.data;

/**
 * Created by granganathan on 12/30/16.
 */


import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.zik.faro.data.ImageProvider;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.net.URL;
import java.util.Date;

/**
 * Created by granganathan on 12/24/16.
 */
@Entity
public class FaroImageDo {
    @Id @Index
    private String imageName;
    private String albumName;
    private String createdTime;
    private String faroUserId;
    @Parent
    private Ref<EventDo> eventId;
    private Integer height;
    private Integer width;
    private URL publicUrl;
    private ImageProvider imageProvider;

    public FaroImageDo() {

    }

    public FaroImageDo withImageName(String imageName) {
        setImageName(imageName);
        return this;
    }

    public FaroImageDo withAlbumName(String albumName) {
        setAlbumName(albumName);
        return this;
    }

    public FaroImageDo withCreatedTime(String createdTime) {
        setCreatedTime(createdTime);
        return this;
    }

    public FaroImageDo withFaroUserId(String faroUserId) {
        setFaroUserId(faroUserId);
        return this;
    }

    public FaroImageDo withEventId(String eventId) {
        setEventId(eventId);
        return this;
    }

    public FaroImageDo withHeight(Integer height) {
        setHeight(height);
        return this;
    }

    public FaroImageDo withWidth(Integer width) {
        setWidth(width);
        return this;
    }

    public FaroImageDo withPublicUrl(URL url) {
        setPublicUrl(url);
        return this;
    }

    public FaroImageDo withImageProvider(ImageProvider imageProvider) {
        setImageProvider(imageProvider);
        return this;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getFaroUserId() {
        return faroUserId;
    }

    public void setFaroUserId(String faroUserId) {
        this.faroUserId = faroUserId;
    }

    public String getEventId() {
        return eventId.getKey().getName();
    }

    public void setEventId(String eventId) {
        this.eventId = Ref.create(Key.create(EventDo.class, eventId));
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public URL getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(URL publicUrl) {
        this.publicUrl = publicUrl;
    }

    public ImageProvider getImageProvider() {
        return imageProvider;
    }

    public void setImageProvider(ImageProvider imageProvider) {
        this.imageProvider = imageProvider;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

