package com.zik.faro.frontend;

/**
 * Created by gaurav on 8/4/17.
 */

public class FbGraphApiConstants {
    public static final String REQUEST_FIELDS = "fields";
    public static final String RESPONSE_DATA = "data";

    // User node - https://developers.facebook.com/docs/graph-api/reference/user/
    public static final String USER_EMAIL = "email";
    public static final String USER_ID = "id";
    public static final String USER_FIRST_NAME = "first_name";
    public static final String USER_LAST_NAME = "last_name";
    public static final String USER_PICTURE = "picture";
    public static final String USER_PICTURE_URL = "url";

    // Album node - https://developers.facebook.com/docs/graph-api/reference/v2.10/album/
    public static final String ALBUM_NAME = "name";
    public static final String ALBUM_ID = "id";
    public static final String ALBUM_PRIVACY = "privacy";
    public static final String ALBUM_PRIVACY_VALUE = "value";
    public static final String ALBUM_PRIVACY_SELF = "SELF";

    // Album photos edge - https://developers.facebook.com/docs/graph-api/reference/v2.10/album/photos
    public static final String ALBUM_PHOTOS_SOURCE = "source";
    public static final String ALBUM_PHOTOS_NO_STORY = "no_story";

    // Photo node - https://developers.facebook.com/docs/graph-api/reference/photo/
    public static final String PHOTO_IMAGES = "images";
    public static final String PHOTO_CREATED_TIME = "created_time";
    public static final String PHOTO_HEIGHT = "height";
    public static final String PHOTO_WIDTH = "width";
    public static final String PHOTO_ALBUM = "album";

    // images field of the Photo node - https://developers.facebook.com/docs/graph-api/reference/photo/
    // Platform Image Source - https://developers.facebook.com/docs/graph-api/reference/platform-image-source/
    public static final String PLATFORM_IMAGE_SOURCE_HEIGHT = "height";
    public static final String PLATFORM_IMAGE_SOURCE_WIDTH = "width";
    public static final String PLATFORM_IMAGE_SOURCE_SOURCE = "source";
}
