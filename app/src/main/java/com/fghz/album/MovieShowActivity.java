package com.fghz.album;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.bumptech.glide.Glide;
import com.fghz.album.entity.PhotoItem;

import static com.fghz.album.utils.ImagesScaner.getAlbumPhotos;
import static com.fghz.album.utils.ImagesScaner.getMediaImageInfo;
import com.fghz.album.R;
import com.fghz.album.view.GlideRoundTransform;

/**
 * Created by me on 17-1-4.
 */

public class MovieShowActivity extends AppCompatActivity {
    private String type;
    private List<Map> result;
    private int count = 0;
    private ImageView iv;
    private MediaPlayer mediaPlayer01;
    private Timer timer;
    private List<PhotoItem> photoList = new ArrayList<PhotoItem>();
    private Handler myHandler = new Handler()
    {
        @Override
        //重写handleMessage方法,根据msg中what的值判断是否执行后续操作
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0x24:
                    try {
                        Glide
                                .with(MovieShowActivity.this)
                                .load(photoList.get(count % photoList.size()).getImageId())
                                .error(R.drawable.error)
                                .crossFade()
                                .thumbnail(0.1f)
                                .into(iv);
                    }  catch (Exception e) {
                        ;
                    }
                    break;
                case 0x23:

                    MovieShowActivity.this.finish();
            }
        }
    };
    private TimerTask  task= new TimerTask() {
        @Override
        public void run() {
            count++;
            Log.d("MainActivity",count + "");
            myHandler.sendEmptyMessage(0x24);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ui界面最上边的动作栏
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.movie_show);

        getMessage();
        initPhoto();
        timer = new Timer();
        timer.schedule(task, 10, 3500);
        iv = (ImageView) findViewById(R.id.movie_image);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Looper.prepare();

                mediaPlayer01 = MediaPlayer.create(getBaseContext(), R.raw.music);
                mediaPlayer01.start();
//                Looper.loop();
//            }
//        }).start();
    }
    @Override
    protected void onResume() {
        /**
         * 设置为横屏
         */
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();//取消任务
        mediaPlayer01.stop();
        myHandler.removeCallbacks(task);//取消任务
        myHandler.removeCallbacksAndMessages(null);//即取消任务，且清除消息
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("Stop", "yes");
            myHandler.sendEmptyMessage(0x23);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void playMusic() {

    }
    protected void getMessage() {
        Intent intent = getIntent();
        try {

            type = intent.getStringExtra("type");
        } catch (Exception e) {
            type = null;
            Log.d("ERROR: ", "" + e);
        }
        Log.d("Info: ", "" + " " + type);
    }

    // 初始化照片数组
    private void initPhoto() {
        PhotoItem photo;
        if (type == null) {
            final List<Map> mediaImageInfo;
            mediaImageInfo = getMediaImageInfo(MovieShowActivity.this);
            for (Map<String, String> map : mediaImageInfo) {
                // in this map, the key of url is _data
                String url = map.get("_data");
                if (url != null) {
                    photo = new PhotoItem(url);
                    photoList.add(photo);
                }
            }
        } else {
            this.result = getAlbumPhotos(MovieShowActivity.this, this.type);
            for (Map<String, String> map : result) {
                // in this map, the key of url is _data
                String url = map.get("url");
                if (url != null) {
                    photo = new PhotoItem(url);
                    photoList.add(photo);
                }
            }
        }
    }
}
