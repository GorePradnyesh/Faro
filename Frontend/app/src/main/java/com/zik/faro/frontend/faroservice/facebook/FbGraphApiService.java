package com.zik.faro.frontend.faroservice.facebook;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.FaroImageBase;
import com.zik.faro.data.FbImage;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.ui.activities.ImageGridViewActivity;
import com.zik.faro.frontend.data.FacebookMinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by granganathan on 7/23/16.
 */

/**
 *  This class is provides the functionality for Facebook graph api workflows
 *  Refer the Facebook graph api at
 *
 *  https://developers.facebook.com/docs/graph-api/reference
 *  https://developers.facebook.com/docs/graph-api/using-graph-api
 *
 */
public class FbGraphApiService {
    private String TAG = "FbGraphApiService";
    private AccessToken accessToken;
    private static final int NUM_THREADS = 10;
    private final static AtomicInteger notificationIdGenerator = new AtomicInteger(0);

    // TODO : need an optimal configuration of the threadpool
    private static ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

    public FbGraphApiService() {
        this.accessToken = AccessToken.getCurrentAccessToken();

        if (this.accessToken == null) {
            throw new IllegalStateException("Access token is null. Not logged into FB account");
        }
    }

    /**
     * Find the FB album and return the album id
     *
     * This method makes a synchronous FB api call
     * Must be executed by background thread
     *
     * @param albumName
     * @return  id of the album on FB
     */
    private String findAlbum(final String albumName) {
        GraphRequest request = new GraphRequest(accessToken, "/me/albums");
        GraphResponse response = request.executeAndWait();

        if (response.getError() == null) {
            JSONObject responseObject = response.getJSONObject();
            try {
                JSONArray albums = responseObject.getJSONArray(FbGraphApiConstants.RESPONSE_DATA);
                for (int i = 0; i < albums.length(); i++) {
                    JSONObject album = albums.getJSONObject(i);
                    if (albumName.equals(album.getString(FbGraphApiConstants.ALBUM_NAME))) {
                        // Album already exists. Return the album id
                        Log.d(TAG, MessageFormat.format("Album {0} exists. Return album id : {1}", albumName, album.getString("id")));
                        return album.getString(FbGraphApiConstants.ALBUM_ID);
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "incorrect JSON processing or invalid response");
            }
        }

        return null;
    }

    /**
     * Create album with specified name
     * if not album already exists with the same name, just return the name
     *
     * This method makes a synchronous FB api call
     * Must be executed by background thread
     * @param albumName
     */
    private String getOrCreateAlbum(final String albumName) {
        String albumId = findAlbum(albumName);
        if (albumId == null) {
            Log.i(TAG, "Album does not exist. Must create one. First check permissions to create");
            if (!accessToken.getPermissions().contains("user_photos")) {
                // TODO : Must initiate permission request
            }

            // Create new album
            Bundle parameters = new Bundle();
            parameters.putString(FbGraphApiConstants.ALBUM_NAME, albumName);

            // Set privacy of the album to SELF i.e. only the user can view the album and its photos
            JSONObject privacyObject = new JSONObject();
            try {
                privacyObject.put(FbGraphApiConstants.ALBUM_PRIVACY_VALUE, FbGraphApiConstants.ALBUM_PRIVACY_SELF);

                parameters.putString(FbGraphApiConstants.ALBUM_PRIVACY, privacyObject.toString());
                GraphRequest createRequest = new GraphRequest(
                        accessToken,
                        "/me/albums",
                        parameters,
                        HttpMethod.POST);
                GraphResponse response = createRequest.executeAndWait();
                if (response.getError() == null) {
                    JSONObject createAlbumResponsObject = response.getJSONObject();
                    if (createAlbumResponsObject != null) {
                        try {
                            albumId = createAlbumResponsObject.getString(FbGraphApiConstants.ALBUM_ID);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error processing response from album creation response");
                        }
                    }
                } else {
                    Log.e(TAG, MessageFormat.format("could not create album. error  = {0}", response.getError()));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error creating request to create album", e);
            }

        }

        return albumId;
    }

    /**
     * Obtain the public URL of all the images in the album specified
     *
     * @param fbPhotos
     * @return list of image urls
     */
    private List<FaroImageBase> obtainImageDownloadLinks(final Map<String, String> fbPhotos, Event event) {
        List<FaroImageBase> faroImages = Lists.newArrayList();

        Bundle params = new Bundle();
        params.putString(FbGraphApiConstants.REQUEST_FIELDS, Joiner.on(", ").join(FbGraphApiConstants.PHOTO_IMAGES, FbGraphApiConstants.PHOTO_CREATED_TIME, FbGraphApiConstants.PHOTO_HEIGHT, FbGraphApiConstants.PHOTO_WIDTH, FbGraphApiConstants.PHOTO_ALBUM));

        for (String fbPhoto : fbPhotos.keySet()) {
            GraphResponse response = new GraphRequest(
                    accessToken,
                    MessageFormat.format("/{0}", fbPhotos.get(fbPhoto)),
                    params,
                    HttpMethod.GET
            ).executeAndWait();

            if (response.getError() == null) {
                try {
                    JSONObject jsonResponse = response.getJSONObject();
                    String createdTime = jsonResponse.getString(FbGraphApiConstants.PHOTO_CREATED_TIME);
                    Integer height = jsonResponse.getInt(FbGraphApiConstants.PHOTO_HEIGHT);
                    Integer width = jsonResponse.getInt(FbGraphApiConstants.PHOTO_WIDTH);
                    String albumName = jsonResponse.getJSONObject(FbGraphApiConstants.PHOTO_ALBUM).getString(FbGraphApiConstants.ALBUM_NAME);

                    String url = null;

                    Log.i(TAG, MessageFormat.format("createdTime: {0} height: {1} width: {2}", createdTime, height, width));
                    JSONArray imagesArray = jsonResponse.getJSONArray(FbGraphApiConstants.PHOTO_IMAGES);
                    if (imagesArray != null) {
                        JSONObject imageObject = imagesArray.getJSONObject(0);

                        url = imageObject.getString(FbGraphApiConstants.PLATFORM_IMAGE_SOURCE_SOURCE);
                        Log.d(TAG, MessageFormat.format("Found image URL : {0}", url));
                    } else {
                        Log.e(TAG, "images not present for the photo");
                    }

                    if (url != null && createdTime != null && height != null && width != null && albumName != null) {
                        faroImages.add(new FbImage()
                                .withImageName(fbPhoto)
                                .withPublicUrl(new URL(url))
                                .withHeight(height)
                                .withWidth(width)
                                .withAlbumName(albumName)
                                .withFaroUserId(FaroUserContext.getInstance().getEmail())
                                .withCreatedTime(createdTime)
                                .withEventId(event.getId()));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "incorrect JSON processing or invalid response");
                } catch (MalformedURLException e) {
                    Log.e(TAG, "incorrect URL processing or invalid response");
                }
            } else {
                Log.e(TAG, "error " + response.getError());
            }
        }

        Log.d(TAG, "imageUrlsMap : " + faroImages);
        return faroImages;
    }

    private NotificationCompat.Builder postNotification(Context context, int notificationId, Event event) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("Faro Images Upload")
                .setContentText("Upload in progress")
                .setProgress(0, 0, true);

        // Create an explicit intent for the Image GridView Activity in your app
        Intent imagesViewIntent = new Intent(context, ImageGridViewActivity.class);
        FaroIntentInfoBuilder.eventIntent(imagesViewIntent, event.getId());

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ImageGridViewActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(imagesViewIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later
        mNotificationManager.notify(notificationId, notificationBuilder.build());

        return notificationBuilder;
    }

    private void updateNotification(NotificationCompat.Builder notificationBuilder, Context context, int notificationId) {
        notificationBuilder.setContentText("Upload Complete")
                .setProgress(0, 0, false);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later
        mNotificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Upload the given photos to the facebook album of the specified event
     * and then save the image metadata (including image public url) on the Faro app server
     * Context here is used for creating a Notification showing the upload progress on the UI
     *
     * @param photoPaths
     * @param event
     * @param context
     */
    public void uploadPhotos(final List<String> photoPaths, final Event event, final Context context) {
        threadPool.submit(new Callable<List<String>>() {
            @Override
            public List<String> call() throws JSONException {
                // Check if album exists or create it
                String albumId = getOrCreateAlbum(event.getEventName());

                if (albumId == null) {
                    Log.e(TAG, "Album does not exist and could not be created");
                    // TODO : throw exception
                    return Lists.newArrayList();
                }

                String uploadPhotoGraphPath = MessageFormat.format("{0}/photos", albumId);

                if (photoPaths != null) {
                    GraphRequestBatch batchRequest = new GraphRequestBatch();

                    // Map of request to file path in order to associate the response of a request to the file path
                    Map<GraphRequest, String> requestMap = Maps.newHashMap();

                    // Convert the photos to byte arrays and then upload them all using a single batch request
                    // TODO : Restrict the number of images in a single batch request
                    for (String photoPath : photoPaths) {
                        byte[] data = null;
                        Bitmap bi = BitmapFactory.decodeFile(photoPath);

                        if (bi == null) {
                            Log.e(TAG, "could not decode file " + photoPath);
                            // TODO : throw exception
                            return Lists.newArrayList();
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        data = baos.toByteArray();

                        Bundle params = new Bundle();
                        params.putByteArray(FbGraphApiConstants.ALBUM_PHOTOS_SOURCE, data);
                        params.putBoolean(FbGraphApiConstants.ALBUM_PHOTOS_NO_STORY, true);

                        Log.i(TAG, MessageFormat.format("Uploading image {0}", photoPath));

                        // Create a GraphRequest for this image
                        GraphRequest graphRequest = new GraphRequest(
                                accessToken,
                                uploadPhotoGraphPath,
                                params,
                                HttpMethod.POST);
                        // Add the request to the batch
                        batchRequest.add(graphRequest);

                        requestMap.put(graphRequest, photoPath);
                    }

                    final int notificationId = notificationIdGenerator.getAndIncrement();
                    final NotificationCompat.Builder notificationBuilder = postNotification(context, notificationId, event);

                    // Execute the batch request and handle the result
                    List<GraphResponse> responseList = batchRequest.executeAndWait();

                    Map<String, String> photoIdsMap = Maps.newHashMap();
                    for (GraphResponse response : responseList) {
                        String filePath = requestMap.get(response.getRequest());
                        if (response.getError() == null) {
                            Log.d(TAG, MessageFormat.format("Uploaded the photo {0} successfully", filePath));
                            photoIdsMap.put(filePath, response.getJSONObject().getString("id"));
                        } else {
                            Log.e(TAG, MessageFormat.format("Failed to upload the photo {0}. error {1}",
                                    filePath, response.getError()));
                        }
                    }

                    Log.i(TAG, "Get image URLs of the uploaded photos ");
                    List<FaroImageBase> fbImages = obtainImageDownloadLinks(photoIdsMap, event);

                    // Store the image urls and image metadata on app server
                    FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

                    serviceHandler.getImagesHandler().createImages(new BaseFaroRequestCallback<List<FaroImageBase>>() {
                        @Override
                        public void onFailure(Request request, IOException ex) {
                            Log.e(TAG, "unable to send images info to app server");
                        }

                        @Override
                        public void onResponse(List<FaroImageBase> faroImageBases, HttpError httpError) {
                            if (httpError == null) {
                                Log.i(TAG, "saved images successfully on app serever");
                                updateNotification(notificationBuilder, context, notificationId);
                            } else {
                                Log.e(TAG, MessageFormat.format("Failed to save images on the app server. code={0} , message={1}", httpError.getCode(), httpError.getMessage()));
                            }
                        }
                    }, event.getId(), fbImages);
                }

                return Lists.newArrayList();
            }
        });
    }

    /**
     * Find all Facebook friends of the facebook user.
     * These friends are all of those who are also Faro users
     *
     * @param callback
     */
    public void findFacebookFriends(GraphRequest.Callback callback) {
        GraphRequest graphRequest = createFindFriendsRequest();
        graphRequest.setCallback(callback);
        graphRequest.executeAsync();
    }

    private GraphRequest createFindFriendsRequest() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Log.i(TAG, MessageFormat.format("accessToken = {0}", accessToken));

        if (accessToken != null) {
            Bundle params = new Bundle();
            params.putString(FbGraphApiConstants.REQUEST_FIELDS, Joiner.on(", ").join(FbGraphApiConstants.USER_EMAIL, FbGraphApiConstants.USER_ID, FbGraphApiConstants.USER_FIRST_NAME, FbGraphApiConstants.USER_LAST_NAME, FbGraphApiConstants.USER_PICTURE));

            return new GraphRequest(AccessToken.getCurrentAccessToken(),
                    "/me/friends",
                    params,
                    HttpMethod.GET);

        }

        throw new IllegalStateException("Access token is null. Not logged into FB account");
    }

    /**
     * Parse the Graph API response object to obtain a list of friends with their info
     * @param response
     * @return
     */
    public List<FacebookMinUser> getFriendsFromGraphResponse(GraphResponse response) {
        List<FacebookMinUser> fbFriendsList = Lists.newArrayList();

        if (response.getError() == null) {
            JSONObject jsonObject = response.getJSONObject();
            Log.i(TAG, MessageFormat.format("jsonObject = {0}", jsonObject));

            try {
                JSONArray friendsArray = jsonObject.getJSONArray(FbGraphApiConstants.RESPONSE_DATA);
                if (friendsArray != null) {
                    for (int i = 0; i < friendsArray.length(); i++) {
                        JSONObject friend = friendsArray.getJSONObject(i);
                        String firstName = friend.getString(FbGraphApiConstants.USER_FIRST_NAME);
                        String lastName = friend.getString(FbGraphApiConstants.USER_LAST_NAME);
                        String id = friend.getString(FbGraphApiConstants.USER_ID);
                        JSONObject pictureData = friend.getJSONObject(FbGraphApiConstants.USER_PICTURE).getJSONObject(FbGraphApiConstants.RESPONSE_DATA);
                        String pictureUrl = pictureData.getString(FbGraphApiConstants.USER_PICTURE_URL);

                        Log.i(TAG, MessageFormat.format("friendName = {0}, last_name = {1}, id = {2}",
                                firstName, lastName, id));

                        fbFriendsList.add((FacebookMinUser) new FacebookMinUser(id)
                                .withFirstName(firstName)
                                .withLastName(lastName)
                                .withThumbProfileImageUrl(pictureUrl));
                    }
                } else {
                    throw new JSONException("data not present in the JSON response");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error processing response for friends list", e);
            }
        }

        return fbFriendsList;
    }

    /**
     * Parse the Graph API response object to obtain a list of friend userids
     * @param response
     * @return
     */
    public List<String> getFriendIdsFromGraphResponse(GraphResponse response) throws JSONException {
        List<String> fbFriendsList = Lists.newArrayList();

        if (response.getError() == null) {
            JSONObject jsonObject = response.getJSONObject();
            Log.i(TAG, MessageFormat.format("jsonObject = {0}", jsonObject));

            JSONArray friendsArray = jsonObject.getJSONArray(FbGraphApiConstants.RESPONSE_DATA);
            if (friendsArray != null) {
                for (int i = 0; i < friendsArray.length(); i++) {
                    JSONObject friend = friendsArray.getJSONObject(i);
                    String fbUserId = friend.getString(FbGraphApiConstants.USER_ID);

                    // TODO : Exchange the fb user id with email
                    Log.i(TAG, MessageFormat.format("facebook  friend id = {0}", fbUserId));
                    if (!Strings.isNullOrEmpty(fbUserId)) {
                        fbFriendsList.add(fbUserId);
                    } else {
                        throw new JSONException("user id not present in the JSON response");
                    }
                }
            } else {
                throw new JSONException("data not present in the JSON response");
            }
        }

        return fbFriendsList;
    }

}
