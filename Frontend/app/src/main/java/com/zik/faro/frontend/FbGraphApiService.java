package com.zik.faro.frontend;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.GridView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenSource;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by granganathan on 7/23/16.
 */
public class FbGraphApiService {
    private String TAG = "FbGraphApiService";
    private AccessToken accessToken;
    private String userId;
    private static final String FB_APPLICATION_ID = "145634995501895";

    public FbGraphApiService(String accessTokenStr, String userId) {
        this.userId = userId;
        accessToken = new AccessToken(accessTokenStr, FB_APPLICATION_ID, userId, null,
                null, AccessTokenSource.FACEBOOK_APPLICATION_WEB, new Date(1467673200), null);
    }

    public void obtainUserData() {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code

                        Log.i(TAG, "response = " + response);
                        Log.i(TAG, "object = " + object);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private String findAlbum(final String albumName) {
        GraphRequest request = new GraphRequest(
                accessToken,
                "/me/albums");

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
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Create album with specified name
     * if not album already exists with the same name
     * Synchronous method
     * Must be executed by background thread
     * @param albumName
     */
    private String createAlbum(final String albumName) {
        String albumId = findAlbum(albumName);
        if (albumId == null) {
            Log.i(TAG, "Album does not exist.Creating one");
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
                Log.i(TAG, "could not create album. error  = " + response.getError());
            }
        }

        return albumId;
    }

    public void downloadImagesIntoGridView(final String albumName, final GridView gridView, final Context context) {
        final List<String> imageUrls = Lists.newArrayList();
        String albumId = createAlbum(albumName);

        if (albumId != null) {
            // Get all images from the album
            String photosPath = MessageFormat.format("/{0}/photos", albumId);
            GraphRequest imagesRequest = GraphRequest.newGraphPathRequest(
                    accessToken,
                    photosPath,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            if (response.getError() == null) {
                                JSONObject responseObject = response.getJSONObject();

                                try {
                                    JSONArray photos = responseObject
                                            .getJSONArray("data");

                                    for (int i = 0; i < photos.length(); i++) {
                                        JSONArray imagesArray = photos
                                                .getJSONObject(i)
                                                .getJSONArray("images");
                                        if (imagesArray != null) {
                                            JSONObject imageObject = imagesArray
                                                    .getJSONObject(0);

                                            String url = imageObject.getString("source");
                                            Log.i(TAG, MessageFormat
                                                    .format("Found image {0} URL : {1}", i, url));
                                            imageUrls.add(url);
                                        } else {
                                            Log.e(TAG, "images not present for the photo");
                                        }
                                    }

                                    String[] urlsArray = new String[imageUrls.size()];
                                    urlsArray = imageUrls.toArray(urlsArray);
                                    gridView.setAdapter(new ImageAdapter(context,
                                            urlsArray));
                                    Log.i(TAG, "imageUrls : " + imageUrls);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e(TAG, "error " + response.getError());
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "images");
            imagesRequest.setParameters(parameters);
            imagesRequest.executeAsync();
        }
    }

    public void uploadPhoto(final String imageFilePath, final String albumName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Check if album exists
                String albumId = createAlbum(albumName);

                if (albumId == null) {
                    Log.e(TAG, "Album does not exist and could not be created");
                    return;
                }

                // Upload photo
                Log.i(TAG, "Converting image " + imageFilePath + " to byte array before uploading ...");

                if (imageFilePath != null) {
                    byte[] data = null;
                    Bitmap bi = BitmapFactory.decodeFile(imageFilePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    data = baos.toByteArray();

                    Bundle params = new Bundle();
                    params.putByteArray("source", data);
                    params.putBoolean("no_story", true);

                    Log.i(TAG, "Uploading image " + imageFilePath);

                    // make the API call
                    String uploadPhotoGraphPath = MessageFormat.format("{0}/photos", albumId);
                    GraphResponse response = new GraphRequest(
                            accessToken,
                            uploadPhotoGraphPath,
                            params,
                            HttpMethod.POST
                    ).executeAndWait();

                    // handle the result
                    if (response.getError() == null) {
                        Log.i(TAG, "Uploaded the photo successfully");
                    } else {
                        Log.e(TAG, "Failed to upload the photo. error = " + response.getError());
                    }

                } else {
                    Log.e(TAG, "Could not find file");
                }
            }
        }).start();
    }

    private void getImages() {
        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/10151503638155006/photos",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        // Insert your code here
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "images");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public static List<String> getCameraImages(Context context) {
        final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString()
                + "/DCIM/Camera";
        final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
        final String[] projection = { MediaStore.Images.Media.DATA };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        ArrayList<String> result = new ArrayList<>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    /**
     * Download photo from specified URL, using the
     * android DownloadManager service
     *
     */
    /*private void downloadPhoto(String imageUrl) {
        if (!isExternalStorageWritable()) {
            Log.i(TAG, "External storage unavailable for read and write");
            return;
        }

        Uri uri = Uri.parse(imageUrl);

        // Create download manager request
        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setDescription("Faro download")
                .setTitle("FB image1");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                "11129914_10155366688470006_9052762267197352743_n.jpg");

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }*/

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}
