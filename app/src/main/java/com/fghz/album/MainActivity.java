package com.fghz.album;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.TensorFlowImageClassifier;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fghz.album.dao.MyDatabaseHelper;

import com.fghz.album.R;
import com.fghz.album.dao.MyDatabaseOperator;

import static com.fghz.album.utils.ImageDealer.do_tensorflow;
import static com.fghz.album.utils.ImageDealer.insertImageIntoDB;

/**
 * created by dongchangzhang
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //UI Object
    private TextView txt_photos;
    private TextView txt_albums;

    //Fragment Object
    private Photos photos;
    private Albums albums;
    private FragmentManager fManager;
    private List<Classifier.Recognition> results;
    private static final int PERMISSION_REQUEST_CAMERA = 300;

    // for camera to save image
    private Uri contentUri;
    private File newFile;

    // notification
    private int NOTI_CODE_HAVE_NEW = 1;
    private int NOTI_CODE_CLASSIFYING = 2;
    private int NOTI_CODE_FINISHED = 3;
    private int NOTI_CODE_NEW_PHOTO = 4;
    private NotificationManager manager;

    private TensorFlowImageClassifier classifier;

    // actionBar
    public static android.support.v7.app.ActionBar actionBar;
    // fragment
    private FragmentTransaction fTransaction;

    private boolean havaInAlbum = false;


    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // scanning images
                case 0x11:
                    albums.onRefresh();
                    break;

            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // fragment
        fManager = getFragmentManager();
        bindViews();
        txt_photos.performClick();

        classifyImagesAtBackground();
    }

    private void sendMessages(String title, String message, int code) {
        // show notification about tf information of image
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentText(message);
        mBuilder.setFullScreenIntent(null,true);
        mBuilder.setAutoCancel(true);

        manager.notify(code, mBuilder.build());
    }

    private void sendMessages(int now, int sum, int code) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.camera));
        //禁止用户点击删除按钮删除
        builder.setAutoCancel(true);
        //禁止滑动删除
        builder.setOngoing(false);
        //取消右上角的时间显示
        builder.setShowWhen(true);
        if (now != sum)
            builder.setContentTitle("正在新处理图片...   "+ now +"/" + sum);
        else {
            builder.setContentTitle("图片处理完成");
            sendMessages("图片处理完成", "享受您的精彩之旅吧！", NOTI_CODE_FINISHED);
        }
        builder.setProgress(sum,now,false);
        //builder.setContentInfo(progress+"%");
        builder.setOngoing(true);
        builder.setShowWhen(false);
        //Intent intent = new Intent(this,DownloadService.class);
        //intent.putExtra("command",1);
        Notification notification = builder.build();
        NotificationManager manger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(code, notification);
    }
    // classify images at background
    private void classifyImagesAtBackground() {
        if (Config.needToBeClassified == null) {
            // do nothing
            Log.d("Main-TF", "null");
        }
        else if (Config.needToBeClassified.size() == 0) {
            // do nothing
            Log.d("Main-TF", "0");
        }
        else {
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    Looper.prepare();
                    Bitmap bitmap;
                    // init tensorflow
                    if (classifier == null) {
                        // get permission
                        classifier = new TensorFlowImageClassifier();
                        try {
                            classifier.initializeTensorFlow(
                                    getAssets(), Config.MODEL_FILE, Config.LABEL_FILE,
                                    Config.NUM_CLASSES, Config.INPUT_SIZE, Config.IMAGE_MEAN,
                                    Config.IMAGE_STD, Config.INPUT_NAME, Config.OUTPUT_NAME);
                        } catch (final IOException e) {

                        }
                    }
                    ContentValues value = new ContentValues();
                    MyDatabaseOperator myoperator = new MyDatabaseOperator(MainActivity.this,
                            Config.DB_NAME, Config.dbversion);
                    int now = 1;
                    sendMessages("正在为您处理" + Config.needToBeClassified.size()
                            +  "张新的图片", "您可以到通知中心查看处理进度", NOTI_CODE_HAVE_NEW);
                    for (String image : Config.needToBeClassified) {
                        Log.d("classifyImages", image);



                        sendMessages(now++, Config.needToBeClassified.size(), NOTI_CODE_CLASSIFYING);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                        bitmap = BitmapFactory.decodeFile(image, options);
                        insertImageIntoDB(image, do_tensorflow(bitmap, classifier), myoperator, value);
                        if (havaInAlbum) {
                            myHandler.sendEmptyMessage(0x11);
                        }

                    }
                    Config.needToBeClassified = null;
                    myoperator.close();
                    Looper.loop();
                }
            }).start();
        }
    }
    /**
     * ActionBar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        // search item
//        MenuItem searchItem = menu.findItem(R.id.action_search);
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * 监听菜单栏目的动作，当按下不同的按钮执行相应的动作
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 返回
                System.out.println("title");
                getFragmentManager().popBackStack();
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle(" 相册");
                break;
//            case R.id.action_search:
//                // 搜索
//               System.out.println("search");
//                break;
            case R.id.action_camera:
                // 拍照
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{ Manifest.permission.CAMERA },
                                PERMISSION_REQUEST_CAMERA);
                    }
                    else {
                        startCamera();
                    }
                }
                else {
                    startCamera();
                }
                break;
            case R.id.action_voice:
                Intent intent = new Intent(MainActivity.this, MapActivity.class);

                startActivity(intent);
                break;
                // 语音
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * do it after require permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode,grantResults);
    }

    /**
     * if have permission will do this, or show a toast
     * @param requestCode
     * @param grantResults
     */
    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(MainActivity.this,
                        "对不起，我需要相机的权限！",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 打开相机获取图片
     */
    private void startCamera() {
        File imagePath = new File(Environment.getExternalStorageDirectory(), "tmp");
        if (!imagePath.exists()) imagePath.mkdirs();
        newFile = new File(imagePath, "default_image.jpg");
        //第二参数是在manifest.xml定义 provider的authorities属性
        contentUri = FileProvider.getUriForFile(this, "com.fghz.album.fileprovider", newFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //兼容版本处理，因为 intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) 只在5.0以上的版本有效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ClipData clip = ClipData.newUri(getContentResolver(), "A photo", contentUri);
            intent.setClipData(clip);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            List<ResolveInfo> resInfoList =
                    getPackageManager()
                            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, contentUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        startActivityForResult(intent, 1);
    }
    // 接受拍照的结果
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ContentResolver contentProvider = getContentResolver();
            ParcelFileDescriptor mInputPFD;
            try {
                //获取contentProvider图片
                mInputPFD = contentProvider.openFileDescriptor(contentUri, "r");
                final FileDescriptor fileDescriptor = mInputPFD.getFileDescriptor();

                // new thread to deal image by tensorflow
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dealPics(fileDescriptor);
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // deal image by tensorflow
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void dealPics(FileDescriptor fileDescriptor) {
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        // init tensorflow
        if (classifier == null) {
            // get permission
            classifier = new TensorFlowImageClassifier();
            try {
                classifier.initializeTensorFlow(
                        getAssets(), Config.MODEL_FILE, Config.LABEL_FILE,
                        Config.NUM_CLASSES, Config.INPUT_SIZE, Config.IMAGE_MEAN,
                        Config.IMAGE_STD, Config.INPUT_NAME, Config.OUTPUT_NAME);
            } catch (final IOException e) {

            }
        }
        // resize bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) Config.INPUT_SIZE) / width;
        float scaleHeight = ((float) Config.INPUT_SIZE) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        // get classifier information
        results = classifier.recognizeImage(newbm);
        for (final Classifier.Recognition result : results) {
            System.out.println("Result: " + result.getTitle());
        }
        // call function to save image
        String url = saveImage("", bitmap);
        Log.d("Detected = ", String.valueOf(results));
        // update db
        Config.dbHelper = new MyDatabaseHelper(this, "Album.db", null, Config.dbversion);
        SQLiteDatabase db = Config.dbHelper.getWritableDatabase();
        ContentValues values_ablum = new ContentValues();
        ContentValues values = new ContentValues();
        String album_type;
        Cursor cursor_album = null;
        for (Classifier.Recognition cr : results) {
            int i;
            for (i = 0; i < Config.tf_type_times; ++i) {
                if (Config.tf_type_name[i].equals(cr.getTitle())) {
                    break;
                }
            }
            album_type = Config.album_type_name[i];
            cursor_album = db.query("Album", null, "album_name ='" + album_type + "'", null, null, null, null);
            if (!cursor_album.moveToFirst()) {
                values_ablum.put("album_name", album_type);
                values_ablum.put("show_image", url);
                db.insert("Album", null, values_ablum);
                values_ablum.clear();
            }

            values.put("album_name", album_type);
            values.put("url", url);
            db.insert("AlbumPhotos", null, values);
            values.clear();
        }
        db.close();

       sendMessages("新的图片", String.valueOf(results), NOTI_CODE_NEW_PHOTO);

    }
    // save image
    private String saveImage(String type, Bitmap bitmap) {
        FileOutputStream b = null;
        // save images to this location
        File file = new File(Config.location);
        // 创建文件夹 @ Config.location
        file.mkdirs();
        String str=null;
        Date date=null;
        // 获取当前时间，进一步转化为字符串
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        date =new Date();
        str=format.format(date);
        String fileName = Config.location + str + ".jpg";

        try {
            b = new FileOutputStream(fileName);
            // 把数据写入文件
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                b.flush();
                b.close();
                // reflash the fragment of Photos
                Config.workdone = false;
                photos.onReflash(fileName);
                Config.workdone = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                File imagePath = new File(Environment.getExternalStorageDirectory(), "tmp");
                newFile = new File(imagePath, "default_image.jpg");
                newFile.delete();
                Log.d("Delete", "True");
            } catch(Exception e) {
                Log.e("Delete", "Error");
            }
        }
        return fileName;
    }

    //UI组件初始化与事件绑定
    private void bindViews() {
        // 定位textview
        txt_photos = (TextView) findViewById(R.id.all_photos);
        txt_albums = (TextView) findViewById(R.id.all_albums);
        // 对其设置监听动作
        txt_photos.setOnClickListener(this);
        txt_albums.setOnClickListener(this);
    }

    //重置所有文本的选中状态为未点击状态
    private void setSelected(){
        txt_photos.setSelected(false);
        txt_albums.setSelected(false);
    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction){
        if(photos != null)fragmentTransaction.hide(photos);
        if(albums != null)fragmentTransaction.hide(albums);
    }

    /**
     * 监听textview的按钮事件
     *
     * @param v
     */

    @Override
    public void onClick(View v) {

        fTransaction = fManager.beginTransaction();
        hideAllFragment(fTransaction);
        switch (v.getId()){
            // 照片

            case R.id.all_photos:
                // set ActionBar tile && set no click action
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle("照片");
                setSelected();
                txt_photos.setSelected(true);
                // 暂时使用弹出堆栈，以避免从相簿进入相册无法返回
                // 可以使用其他方法，这个方法不好，下面相同
                getFragmentManager().popBackStack();
                if(photos == null){
                    photos = new Photos();
                    fTransaction.add(R.id.ly_content,photos);
                }else{
                    fTransaction.show(photos);
                }

                break;

            // 相册
            case R.id.all_albums:

                // same as photos
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle("相册");

                setSelected();
                txt_albums.setSelected(true);
                getFragmentManager().popBackStack();
                if(albums == null){
                    albums = new Albums();
                    fTransaction.add(R.id.ly_content,albums);
                }else{
                    albums.onRefresh();
                    fTransaction.show(albums);
                }
                havaInAlbum = true;
                break;
        }
        fTransaction.commit();
    }
}
