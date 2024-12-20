package com.example.map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class AvatarAdapter extends BaseAdapter {
    private Context context;
    private int[] avatarIds;

    public AvatarAdapter(Context context, int[] avatarIds) {
        this.context = context;
        this.avatarIds = avatarIds;
    }

    @Override
    public int getCount() {
        return avatarIds.length;
    }

    @Override
    public Object getItem(int position) {
        return avatarIds[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(avatarIds[position]);
        return imageView;
    }
}
