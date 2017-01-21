package com.fghz.album.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.bumptech.glide.Glide;
import com.fghz.album.Config;
import com.fghz.album.R;
import com.fghz.album.entity.MemoryItem;

import static android.media.ThumbnailUtils.extractThumbnail;
import static com.fghz.album.utils.ImagesScaner.getBitmap;

/**
 * 回忆栏目的适配器
 * Created by me on 16-12-21.
 */

public class MemoryAdapter extends ArrayAdapter<MemoryItem> {
    private int resourceId;
    private Context context;
    public MemoryAdapter(Context context, int textViewResourceId,
                         List<MemoryItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        this.context = context;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final MemoryItem memory = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
            holder.img = (ImageView) convertView.findViewById(R.id.memory_photo);
            holder.tv = (TextView) convertView.findViewById(R.id.memory_title);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }
        final String url = memory.getImageId();
        holder.tv.setText(memory.getType());
        Glide
                .with(context)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .crossFade()
                .thumbnail(0.1f)
                .into(holder.img);
        return convertView;
    }
    private static class ViewHolder
    {
        public ImageView img;
        public TextView tv;
    }

}