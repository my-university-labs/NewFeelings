package com.fghz.album;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.bumptech.glide.Glide;
import com.fghz.album.adapter.HorizontalScrollViewAdapter;
import com.fghz.album.view.MyHorizontalScrollView;
import com.fghz.album.view.MyHorizontalScrollView.CurrentImageChangeListener;
import com.fghz.album.view.MyHorizontalScrollView.OnItemClickListener;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.fghz.album.utils.ImagesScaner.getAlbumPhotos;
import static com.fghz.album.utils.ImagesScaner.getMediaImageInfo;
import com.fghz.album.R;

/**
 * 查看照片细节的activity
 * 点击照片后进入这个界面查看照片细节
 * Created by me on 16-12-20.
 */
// tf
// some method may be used about db or others
// adapter && view

public class PhotoDetailActivity extends AppCompatActivity /*implements View.OnClickListener */{

    // 初始化几个textview， 可以点击并且出发事件
    private TextView txt_back;
//    private TextView txt_share;
    private TextView txt_love;
    private TextView txt_delete;

    // 自定义的布局， 实现下面缩略图，上面大图
    private MyHorizontalScrollView mHorizontalScrollView;
    // 适配器
    private HorizontalScrollViewAdapter mAdapter;
    private ImageView mImg;
    // 照片数组。照片在drawable文件夹中，名字为a.png ...
    private List<Map> mDatas ;

    private PhotoViewAttacher mAttacher;
    // which image
    int position_now = -1;
    // image url
    String url = null;
    // has been init
    boolean init = false;
    private String type = null;

    private int position_tmp;

    private Handler myHandler = new Handler()
    {
        @Override
        //重写handleMessage方法,根据msg中what的值判断是否执行后续操作
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0x21:

                    Glide
                            .with(PhotoDetailActivity.this)
                            .load((String) mDatas.get(position_tmp).get("_data"))
                            .error(R.drawable.error)
                            .thumbnail(0.1f)
                            .into(mImg);

                    break;

                case 0x22:

                    break;
            }
        }
    };

    public PhotoDetailActivity() {

    }

    // 重写创建活动的方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fg_detail);
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#11000000")));
//        getSupportActionBar().setSplitBackgroundDrawable(new ColorDrawable(Color.parseColor("#11000000")));
        // 绑定textview按钮
//        bindViews();
        // get photo list
        initPhoto();
        if (type != null)
            mDatas = getAlbumPhotos(this, this.type);
        // 下面设置下面缩略图上面大图。
        else
            mDatas = getMediaImageInfo(this.getBaseContext());
        mImg = (ImageView) findViewById(R.id.id_content);
//        mAttacher = new PhotoViewAttacher(mImg);
        Glide.with(PhotoDetailActivity.this).load((String) mDatas.get(position_now).get("_data")).into(mImg);

        mHorizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.id_horizontalScrollView);
        mAdapter = new HorizontalScrollViewAdapter(this, mDatas);
        //添加滚动回调
        mHorizontalScrollView
                .setCurrentImageChangeListener(new CurrentImageChangeListener()
                {
                    @Override
                    public void onCurrentImgChanged(int position,
                                                    View viewIndicator)
                    {
                        if (!init) {
                            position = position_now;
                            init = true;
                        }
                        Log.d("PhotoDetail: ", "Image change to: " + position);
                        try {
                            position_tmp = position;
                            myHandler.sendEmptyMessage(0x21);
                        } catch (Exception e) {
                            ;
                        }

                        viewIndicator.setBackgroundColor(Color.parseColor("#AA024DA4"));
                    }
                });
        //添加点击回调
        mHorizontalScrollView.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onClick(View view, int position)
            {
                mImg.setImageURI(Uri.fromFile(new File((String)mDatas.get(position).get("_data"))));

                view.setBackgroundColor(Color.parseColor("#AA024DA4"));
            }
        });
        //设置适配器
        mHorizontalScrollView.initDatas(mAdapter, position_now);
    }
    private void initPhoto() {
        Intent intent = getIntent();
        try {
            position_now = intent.getIntExtra("position", -1);
            url = intent.getStringExtra("url");
            type = intent.getStringExtra("type");

        } catch (Exception e) {
            Log.d("ERROR: ", "" + e);
        }
        Log.d("Test-----------------: ", "" + position_now + " " + url);
    }
    //UI组件初始化与事件绑定
    private void bindViews() {
        // 返回删除等按钮
//        txt_back = (TextView) findViewById(R.id.back);
//        txt_share = (TextView) findViewById(R.id.share);
//        txt_love = (TextView) findViewById(R.id.love);
//        txt_delete = (TextView) findViewById(R.id.delete);
        // 设置监听
//        txt_back.setOnClickListener(this);
//        txt_share.setOnClickListener(this);
//        txt_love.setOnClickListener(this);
//        txt_delete.setOnClickListener(this);
    }
    // 恢复点击状态为未点击状态
    private void setSelect() {
//        txt_back.setSelected(false);
//        txt_share.setSelected(false);
//        txt_love.setSelected(false);
//        txt_delete.setSelected(false);
    }
    /**
     * 生成动作栏上的菜单项目
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_for_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * 监听菜单栏目的动作，当按下不同的按钮执行相应的动作
     *
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                // 返回
                this.finish();
                break;
            case R.id.action_about:
                // go to PhotoInfoActivity
                Intent intent = new Intent(this, PhotoInfoActivity.class);
                int position;
                if (!init) {
                    position = position_now;
                } else {
                    position =  mHorizontalScrollView.getmShowIndex();
                }
                // send args
                intent.putExtra("position", position);
                intent.putExtra("url", (String)mDatas.get(position).get("_data"));
                startActivity(intent);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // 下面按钮（返回删除等）的点击动作
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            // 返回
//            case R.id.back:
//                setSelect();
//                txt_back.setSelected(true);
//                System.out.println("1");
//                PhotoDetailActivity.this.finish(); // 结束当前的activity， 返回上一个界面
//                break;
////            case R.id.share: // 分享
////                setSelect();
////                txt_share.setSelected(true);
////                System.out.println("2");
////
////                break;
//            case R.id.love: // 喜爱
//                setSelect();
//                txt_love.setSelected(true);
//                System.out.println("3");
//
//                break;
////            case R.id.delete: // 删除
////                setSelect();
////                txt_delete.setSelected(true);
////
////                txt_delete.setSelected(false);
////                System.out.println("4");
////
////                break;
//        }
//    }

}
