package com.fghz.album.adapter;

/**
 * 照片细节的适配器
 * Created by me on 16-12-21.
 */

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;
import java.util.Map;

import com.fghz.album.Config;
import com.fghz.album.R;

import static android.media.ThumbnailUtils.extractThumbnail;
import static com.fghz.album.utils.ImagesScaner.getBitmap;

public class HorizontalScrollViewAdapter extends BaseAdapter
{

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Map> mDatas;
    private String url;

    public HorizontalScrollViewAdapter(Context context, List<Map> mDatas)
    {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mDatas = mDatas;
        if (Config.mImageCache == null) {
            final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
            final int maxSize = 1024 * 1024 * memClass / 8;
            Config.mImageCache = new LruCache(maxSize) {
                protected int sizeOf(String key, Bitmap value) {
                    // TODO 自动生成的方法存根
                    return value.getByteCount();
                }
            };
        }
    }

    @Override
    public int getCount()
    {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Log.d("Horizontal: ", "position " + position);
        ViewHolder viewHolder = null;
        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(
                    R.layout.gallery_item, parent, false);
            viewHolder.mImg = (ImageView) convertView
                    .findViewById(R.id.id_index_gallery_item_image);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        try {
            url = (String) mDatas.get(position).get("_data");
            Bitmap bitmap = (Bitmap) Config.mImageCache.get(url);

            if (bitmap != null) {
                ;
            } else {
                loadThumBitmap(url);
                bitmap = (Bitmap) Config.mImageCache.get(url);
            }

            viewHolder.mImg.setImageBitmap(bitmap);
        } catch (Exception e) {
            viewHolder.mImg.setImageResource(R.drawable.none);
        }
        return convertView;
    }

    private class ViewHolder
    {
        ImageView mImg;
    }
    private void loadThumBitmap(final String url) {
        Bitmap bitmap = getBitmap(mContext,url);
        if (bitmap != null) {
            ;
        } else {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                bitmap = BitmapFactory.decodeFile(url, options);
                bitmap = extractThumbnail(bitmap,180 , 180);
            } catch (Exception e) {
                Log.d("Error: " , " " + e);
            }
        }
        Config.mImageCache.put(url, bitmap);
        notifyDataSetChanged();
    }

}

