package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by granganathan on 3/26/17.
 */

public class ImagePickerActivity extends AppCompatActivity {
    private static final String TAG = "ImagePickerActivity";
    private final Context context = this;
    private GridView gridView;
    private Cursor cursor;
    private final String[] projection = {MediaStore.Images.ImageColumns.DATA};
    private HashMap<Integer, String> imageNames = Maps.newHashMap();
    private Set<String> selectedImages = Sets.newHashSet();
    private int maxNumImages;

    private static final String ACTIVITY_TITLE = "Pick Images";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker_grid_view);

        setTitle(ACTIVITY_TITLE);

        // Get all gallery images from SD
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        CursorLoader loader = new CursorLoader(context, uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        cursor.moveToFirst();
        int row = 0;
        do {
            imageNames.put(row, cursor.getString(cursor.getColumnIndex(projection[0])));
            row++;
        } while (cursor.moveToNext());

        cursor.close();

        // Setup the GridView for the images
        gridView = (GridView) findViewById(R.id.imagePickerGridview);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView imageView = (ImageView) view;

                String imageName = imageNames.get(position);

                if (!selectedImages.contains(imageName)) {
                    if (selectedImages.size() < maxNumImages) {
                        imageView.setBackground(getResources().getDrawable(R.drawable.image_view_highlight));
                        selectedImages.add(imageName);
                    }
                } else {
                    imageView.setBackground(null);
                    selectedImages.remove(imageName);
                }
            }
        });

        gridView.setAdapter(new ImagePickerAdapter());

        FaroApplication faroApplication = (FaroApplication) getApplication();
        maxNumImages = faroApplication.getMaxImagesUploaded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(Menu.NONE, 1000, Menu.NONE, "Done");
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "DONE!!");

        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("images", Lists.newArrayList(selectedImages));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        return true;
    }

    private class ImagePickerAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return imageNames.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(gridView.getLayoutParams());
                imageView.setLayoutParams(new GridView.LayoutParams(255, 255));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(10, 10, 10, 10);
            } else {
                imageView = (ImageView) convertView;
            }

            String pictureName = imageNames.get(position);

            if (selectedImages.contains(pictureName)) {
                ViewParent viewParent = imageView.getParent();
                imageView.setBackground(getResources().getDrawable(R.drawable.image_view_highlight));
            } else {
                imageView.setBackground(null);
            }

            // Load the image into the ImageView
            Glide.with(context)
                    .load(pictureName)
                    .into(imageView);

            Log.d(TAG, "Loaded image " + pictureName + " to ImageView " + position);

            return imageView;
        }
    }
}
