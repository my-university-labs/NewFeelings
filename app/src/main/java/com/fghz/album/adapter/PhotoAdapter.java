package com.fghz.album.adapter;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

import com.fghz.album.Config;
import com.fghz.album.R;
import com.fghz.album.entity.PhotoItem;

import static android.media.ThumbnailUtils.extractThumbnail;
import static com.fghz.album.utils.ImagesScaner.getBitmap;

/**
 * 照片栏目的适配器
 * Created by me on 16-12-21.
 */
public class PhotoAdapter extends ArrayAdapter<PhotoItem> {
    private int resourceId;
    LayoutInflater mInflater;
    List<PhotoItem> mImageList;
    boolean mBusy = false;
    private Handler myHandler = new Handler()
    {
        @Override
        //重写handleMessage方法,根据msg中what的值判断是否执行后续操作
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0x24:
                    Log.d("change", "y");
                    notifyDataSetChanged();

                    break;

                case 0x123:

                    break;
            }
        }
    };

    public void setMImageList(List<PhotoItem> mImageList) {
        this.mImageList = mImageList;
    }
    public PhotoAdapter(Context context, int textViewResourceId,
                        List<PhotoItem> objects) {
        super(context, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
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
        mImageList = objects;
        resourceId = textViewResourceId;
    }
    @Override
    public int getCount() {
        // TODO 自动生成的方法存根
        return mImageList.size();
    }

    @Override
    public PhotoItem getItem(int position) {
        // TODO 自动生成的方法存根
        return mImageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO 自动生成的方法存根
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 获取当前项的item实例
        try {
            final PhotoItem photo = getItem(position);

            // 保存当前信息
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
                holder = new ViewHolder();
                holder.iv_thumbnail = (ImageView) convertView.findViewById(R.id.photo_small);
                holder.thumbnail_url = photo.getImageId();
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                if (!holder.thumbnail_url.equals(photo.getImageId())) {
                    holder.iv_thumbnail.setImageResource(R.drawable.loading);
                }
            }
            if (!isBusy()) {
                final String imgUrl = photo.getImageId();
                final Bitmap[] bmp = {(Bitmap) Config.mImageCache.get(imgUrl)};
                if (bmp[0] != null) {
                    ;
                } else {
                    try {
//                    holder.iv_thumbnail.setImageResource(R.drawable.b);

                        setBusy(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                        loadThumBitmap(imgUrl);
                        bmp[0] = (Bitmap) Config.mImageCache.get(imgUrl);
                                Looper.loop();
                            }
                        }).start();
                        setBusy(false);

                    } catch (Exception e) {
                        Log.d("Error:", "" + e);
                    }
                }
                holder.iv_thumbnail.setImageBitmap(bmp[0]);

            }

        } catch (Exception e) {
            ;
        }
        return convertView;
    }
    private void loadThumBitmap(final String url) {
        Bitmap bitmap = getBitmap(getContext(),url);
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
        myHandler.sendEmptyMessage(0x24);
    }
    @Override
    public int getItemViewType(int position) {
        // TODO 自动生成的方法存根
        return super.getItemViewType(position);
    }
    @Override
    public int getViewTypeCount() {
        // TODO 自动生成的方法存根
        return super.getViewTypeCount();
    }
    //用来保存各个控件的引用
    static class ViewHolder {
        ImageView iv_thumbnail;
        String thumbnail_url;
    }
    public boolean isBusy() {
        return mBusy;
    }

    public void setBusy(boolean busy) {
        this.mBusy = busy;
    }

}

