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
import com.fghz.album.entity.AlbumItem;

import static android.media.ThumbnailUtils.extractThumbnail;
import static com.fghz.album.utils.ImagesScaner.getBitmap;

/**
 * 相册的适配器
 * Created by me on 16-12-21.
 */

public class AlbumAdapter extends ArrayAdapter<AlbumItem> {
    private int resourceId;
    private Context context;
    public AlbumAdapter(Context context, int textViewResourceId,
                        List<AlbumItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        this.context = context;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final AlbumItem album = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);


        }
        TextView albumName = (TextView) convertView.findViewById(R.id.album_name);
        albumName.setText(album.getName());
        ImageView myImageView = (ImageView) convertView.findViewById(R.id.album_image);

        String url = album.getImageId();
        Glide
                .with(context)
                .load(url)
                .centerCrop()
                .error(R.drawable.error)
                .crossFade()
                .thumbnail(0.1f).into(myImageView);
        return convertView;
    }

}