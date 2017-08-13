package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.FaroImageBase;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;


/**
 * Created by granganathan on 7/9/16.
 */
public class ImageGridViewActivity extends AppCompatActivity {
    private static final String TAG = "ImageGridViewActivity";
    private final Context context = this;
    private Event cloneEvent = null;
    private EventListHandler eventListHandler = EventListHandler.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        String eventId = getIntent().getStringExtra(FaroIntentConstants.EVENT_ID);
        try {
            cloneEvent = eventListHandler.getCloneObject(eventId);
        } catch (FaroObjectNotFoundException e) {
            Log.i(TAG, MessageFormat.format("Event {0} has been deleted", eventId));
            finish();
        }

        final String eventName = cloneEvent.getEventName();
        Log.i(TAG, "eventName = " + eventName);

        // Set the title to the event name
        setTitle(eventName);

        final GridView gridView = (GridView) findViewById(R.id.gridview);

        ImagesListHandler imagesListHandler = ImagesListHandler.initializeInstance(context);
        gridView.setAdapter(imagesListHandler.getImageAdapter());

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, MessageFormat.format("Item view position = {0}, item row id = {1}", i, l));

                Intent screenSlideIntent = new Intent(ImageGridViewActivity.this, ScreenSlidePagerActivity.class);
                screenSlideIntent.putExtra("imageIndex", i);
                screenSlideIntent.putExtra("eventName", eventName);
                startActivity(screenSlideIntent);
            }
        });


        FaroServiceHandler.getFaroServiceHandler().getImagesHandler().getImages(new BaseFaroRequestCallback<List<FaroImageBase>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get images");
            }

            @Override
            public void onResponse(final List<FaroImageBase> faroImages, HttpError error) {
                if (error == null) {
                    if (!faroImages.isEmpty()) {
                        ImagesListHandler.getInstance().setFaroImages(faroImages);

                        final List<String> imageUrls = Lists.newArrayList();
                        imageUrls.addAll(Lists.transform(faroImages, new Function<FaroImageBase, String>() {
                            @Override
                            public String apply(FaroImageBase faroImageBase) {
                                return faroImageBase.getPublicUrl().toString();
                            }
                        }));

                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "Successfully obtained images for the event");
                                ImagesListHandler.getInstance().addImages(imageUrls);
                            }
                        };
                        Handler mainHandler = new Handler(context.getMainLooper());
                        mainHandler.post(myRunnable);
                    }
                } else {
                    Log.e(TAG, MessageFormat.format("error = {0}", error.getCode()));
                }
            }
        }, eventId);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
