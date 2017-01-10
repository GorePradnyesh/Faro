package com.zik.faro.frontend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by granganathan on 7/23/16.
 */
public class FbGraphApiService {
    private String TAG = "FbGraphApiService";
    private AccessToken accessToken;
    private static final long FB_IMAGES_REQUEST_TIMEOUT_SECS = 60;
    private static final int NUM_THREADS = 10;

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
                JSONArray albums = responseObject.getJSONArray("data");
                for (int i = 0; i < albums.length(); i++) {
                    JSONObject album = albums.getJSONObject(i);
                    if (albumName.equals(album.getString("name"))) {
                        // Album already exists. Return the album id
                        Log.d(TAG, MessageFormat.format("Album {0} exists. Return album id : {1}", albumName, album.getString("id")));
                        return album.getString("id");
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

            }

            // Create new album
            Bundle parameters = new Bundle();
            parameters.putString("name", albumName);

            // Set privacy of the album to SELF i.e. only the user can view the album and its photos
            JSONObject privacyObject = new JSONObject();
            try {
                privacyObject.put("value", "SELF");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            parameters.putString("privacy", privacyObject.toString());
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
                        albumId = createAlbumResponsObject.getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.e(TAG, MessageFormat.format("could not create album. error  = {0}", response.getError()));
            }
        }

        return albumId;
    }

    /**
     * Obtain the public URL of all the images in the album specified
     *
     * @param fbPhotoIds
     * @return list of image urls
     */
    public List<String> obtainImageDownloadLinks(final List<String> fbPhotoIds) {
        List<String> imageUrls = Lists.newArrayList();

        Bundle params = new Bundle();
        params.putString("fields", "images");

        for (String fbPhotoId : fbPhotoIds) {
            GraphResponse response = new GraphRequest(
                    accessToken,
                    MessageFormat.format("/{0}",fbPhotoId),
                    params,
                    HttpMethod.GET
            ).executeAndWait();

            if (response.getError() == null) {
                try {
                    JSONArray imagesArray = response.getJSONObject().getJSONArray("images");
                    if (imagesArray != null) {
                        JSONObject imageObject = imagesArray.getJSONObject(0);

                        String url = imageObject.getString("source");
                        Log.d(TAG, MessageFormat.format("Found image URL : {0}", url));
                        imageUrls.add(url);
                    } else {
                        Log.e(TAG, "images not present for the photo");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "incorrect JSON processing or invalid response");
                }
            } else {
                Log.e(TAG, "error " + response.getError());
            }
        }

        Log.d(TAG, "imageUrls : " + imageUrls);
        return imageUrls;
    }

    public List<String> uploadPhotos(final List<String> photoPaths, final String albumName) {
        Future<List<String>> future = threadPool.submit(new Callable<List<String>>() {
            @Override
            public List<String> call() throws JSONException {
                // Check if album exists or create it
                String albumId = getOrCreateAlbum(albumName);

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
                        params.putByteArray("source", data);
                        params.putBoolean("no_story", true);

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

                    // Execute the batch request and handle the result
                    List<GraphResponse> responseList = batchRequest.executeAndWait();
                    List<String> photoIds = Lists.newArrayList();

                    for (GraphResponse response : responseList) {
                        String filePath = requestMap.get(response.getRequest());
                        if (response.getError() == null) {
                            Log.d(TAG, MessageFormat.format("Uploaded the photo {0} successfully", filePath));
                            photoIds.add(response.getJSONObject().getString("id"));
                        } else {
                            Log.e(TAG, MessageFormat.format("Failed to upload the photo {0}. error {1}",
                                    filePath, response.getError()));
                        }
                    }

                    Log.i(TAG, MessageFormat.format("Get image URLs of the uploaded photos {0}", photoIds));
                    List<String> imageUrls = obtainImageDownloadLinks(photoIds);

                    // Store the image urls and image metadata on app server
                    FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
                }

                return Lists.newArrayList();
            }
        });

        List<String> photos = Lists.newArrayList();
        try {
            photos = future.get(FB_IMAGES_REQUEST_TIMEOUT_SECS, TimeUnit.SECONDS);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Log.e(TAG, MessageFormat.format("Failed to download images from album {0}", albumName));
        }

        return photos;
    }
}
