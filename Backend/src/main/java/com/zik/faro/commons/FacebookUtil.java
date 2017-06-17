package com.zik.faro.commons;

import com.zik.faro.data.ImageProvider;
import com.zik.faro.data.user.LargeProfileImage;
import com.zik.faro.data.user.SmallProfileImage;
import com.zik.faro.persistence.datastore.data.user.LargeProfileImageDo;
import com.zik.faro.persistence.datastore.data.user.SmallProfileImageDo;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

/**
 * Created by gaurav on 6/16/17.
 */
public class FacebookUtil {
    private static final String FACEBOOK_USER_THUMB_PICTURE_URL = "https://graph.facebook.com/{0}/picture";
    private static final String FACEBOOK_USER_LARGE_PICTURE_URL = "https://graph.facebook.com/{0}/picture?type=large";

    public static SmallProfileImage getFacebookSmallProfileImage(String authProviderUserId) throws MalformedURLException {
        return new SmallProfileImage(new URL(MessageFormat.format(FACEBOOK_USER_THUMB_PICTURE_URL, authProviderUserId)), ImageProvider.FACEBOOK);
    }

    public static LargeProfileImage getFacebookLargeProfileImage(String authProviderUserId) throws MalformedURLException {
        return new LargeProfileImage(new URL(MessageFormat.format(FACEBOOK_USER_LARGE_PICTURE_URL, authProviderUserId)), ImageProvider.FACEBOOK);
    }

}
