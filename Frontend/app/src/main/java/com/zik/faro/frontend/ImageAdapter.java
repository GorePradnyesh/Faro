package com.zik.faro.frontend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by granganathan on 7/13/16.
 */
public class ImageAdapter extends ArrayAdapter {
    private Context context;
    private LayoutInflater inflater;

    public ImageAdapter(Context context) {
        super(context, R.layout.gridview_item_image);

        this.context = context;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gridview_item_image, parent, false);

            Glide.with(context)
                    .load(getItem(position))
                    .into((ImageView) convertView);
        }

        return convertView;
    }
}