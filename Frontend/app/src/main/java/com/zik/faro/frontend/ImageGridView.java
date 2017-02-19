package com.zik.faro.frontend;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.squareup.okhttp.Request;
import com.zik.faro.data.FaroImageBase;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;


/**
 * Created by granganathan on 7/9/16.
 */
public class ImageGridView extends AppCompatActivity {
    private static final String TAG = "ImageGridView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        String eventName = getIntent().getStringExtra("eventName");
        String eventId = getIntent().getStringExtra("eventId");
        Log.i(TAG, "eventName = " + eventName);

        final GridView gridView = (GridView) findViewById(R.id.gridview);
        final List<String> imageUrls = Lists.newArrayList();

        FaroServiceHandler.getFaroServiceHandler().getImagesHandler().getImages(new BaseFaroRequestCallback<List<FaroImageBase>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get images");
            }

            @Override
            public void onResponse(List<FaroImageBase> faroImages, HttpError error) {
                if (error == null) {
                    if (!faroImages.isEmpty()) {
                        imageUrls.addAll(Lists.transform(faroImages, new Function<FaroImageBase, String>() {
                            @Override
                            public String apply(FaroImageBase faroImageBase) {
                                return faroImageBase.getPublicUrl().toString();
                            }
                        }));
                    }
                } else {
                    Log.e(TAG, MessageFormat.format("error = {0}", error.getCode()));
                }
            }
        }, eventId);

        gridView.setAdapter(new ImageAdapter(this, imageUrls));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, MessageFormat.format("Item view position = {0}, item row id = {1}", i, l));

                
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_grid_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
