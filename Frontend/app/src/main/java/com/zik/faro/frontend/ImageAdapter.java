package com.zik.faro.frontend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by granganathan on 7/13/16.
 */
public class ImageAdapter extends ArrayAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<String> imageUrls;

    public ImageAdapter(Context context, List<String> imageUrls) {
        super(context, R.layout.gridview_item_image, imageUrls);

        this.context = context;
        this.imageUrls = imageUrls;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gridview_item_image, parent, false);

            Glide.with(context)
                    .load(imageUrls.get(position))
                    .into((ImageView) convertView);
        }

        return convertView;
    }
}