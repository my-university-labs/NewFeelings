package com.fghz.album;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.fghz.album.dao.MyDatabaseOperator;
import com.fghz.album.dao.SystemDatabseOperator;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.TensorFlowImageClassifier;

import static com.fghz.album.utils.ImageDealer.dealImageForTF;
import static com.fghz.album.utils.ImageDealer.do_tensorflow;
import static com.fghz.album.utils.ImageDealer.insertImageIntoDB;

/**
 * Created by dongchangzhang on 17-1-1.
 */

public class WelcomeActivity extends AppCompatActivity {
    // for permission
    private static final int PERMISSION_REQUEST_STORAGE = 200;
    // scan image and save them
    private List<String> stillInDeviceImages;
    private List<String> notBeClassifiedImages;

    private ContentValues value;
    private MyDatabaseOperator myoperator;

    private TensorFlowImageClassifier classifier;


    private int i = 0;
    private int size = 0;
    private TextView textView = null;
    private TextView textViewTitle = null;
    private ProgressBar pbar;
    private final String[] actions =  {
            "全部进入APP时处理", "全部后台处理", "根据图片数量决定"};

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // scanning images
                case 0x1:
                    Log.d("MESSAGE", "0x1111");
                    if (textView != null)
                        textView.setText("\n正在扫描图片 ");
                    break;
                // scanning images done
                case 0x2:
                    do_afterScanImage();
                    break;
                // classifying image with tf
                //
                case 0x3:
                    textView.setText("正在准备...");
                    break;
                case 0x23:
                    i++;
                    if (textView != null)
                        textView.setText("\n正在处理图片 " + i + "/" + size);
                    break;
                // this activity will be finished
                case 0x24:
                    do_finishThisActivity();
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_welcome);
        textView = (TextView) findViewById(R.id.work_process);
        textViewTitle = (TextView) findViewById(R.id.app_title);
        pbar = (ProgressBar) findViewById(R.id.progressBar);
        pbar.setVisibility(pbar.GONE);
        setAppName();

        if (Build.VERSION.SDK_INT >= 23) {
            // check permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                // require permission for wr
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                }, PERMISSION_REQUEST_STORAGE);
            }
            else {
                prepareForApplication();
            }
        }
        else {
            prepareForApplication();
        }
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
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pbar.setVisibility(pbar.VISIBLE);
                myHandler.sendEmptyMessage(0x3);
                prepareForApplication();
            } else {
                Toast.makeText(WelcomeActivity.this,
                        "对不起，不能访问存储卡我不能继续工作！",
                        Toast.LENGTH_LONG).show();
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        WelcomeActivity.this.finish();
                    }
                };
                timer.schedule(task, 1000 * 2);
            }
        }
    }

    /**
     * open another thread to scan images and judge whether the image had
     * been deleted in other places or add some new image but has not been classified by tf
     */
    private void prepareForApplication() {
        new Thread(new Runnable() {
            @Override
            public void run() {
            Looper.prepare();
            do_prepare(WelcomeActivity.this);
            Looper.loop();
            }
        }).start();

    }
    /**
     * when open application, this function will scan images in device and check them with the db
     * of this application to confirm whether the image has been classified by tf or whether the
     * images had been delete in other places, and do something to keep application running with
     * no error
     * @param ctx
     */
    private void do_prepare(Context ctx) {

        // mark images which in my db but not in device
        stillInDeviceImages = new ArrayList<>();
        // save images which are not be classified by tf
        notBeClassifiedImages = new ArrayList<>();
        // get all image in phone
        List<Map> imagesInDevice = SystemDatabseOperator.getExternalImageInfo(ctx);
        // operator for my db
        MyDatabaseOperator operator = new MyDatabaseOperator(ctx, Config.DB_NAME, Config.dbversion);

        String url;
        List<Map> findResult;
        // for every image in device
        for (Map<String, String> imageInfo : imagesInDevice) {
            myHandler.sendEmptyMessage(0x1);
            url = imageInfo.get("_data");
            // test whether had been classified
            findResult = operator.search("TFInformation", "url = '" + url + "'");
            if (findResult.size() == 0) {
                // not be classified
                notBeClassifiedImages.add(url);
                Log.d("TFInformation", "no");
            }
            else {
                // had been classified
                stillInDeviceImages.add(url);
                Log.d("TFInformation", "yes");

            }
        }
        size = notBeClassifiedImages.size();
        // for every image in db
        List<Map> imagesInDB = operator.search("AlbumPhotos");
        for (Map<String, String> imageInfo : imagesInDB) {
            url = imageInfo.get("url");
            // test whether had been deleted
            findResult = operator.search("AlbumPhotos", "url = '" + url + "'");
            if (findResult.size() == 0) {
                // had been deleted, erase it in db
                operator.erase("AlbumPhotos", "url = ?", new String[] { "'" + url + "'"});
                operator.erase("TFInformation", "url = ?", new String[] { "'" + url + "'"});
            }
            else {
                // not be deleted, do nothing
            }
        }
        // for every album in db
        String album_name;
        List<Map> typeInAlbum = operator.search("Album");
        for (Map<String, String> albumInfo : typeInAlbum) {
            album_name = albumInfo.get("album_name");
            findResult = operator.search("AlbumPhotos", "album_name = '" + album_name + "'");
            if (findResult.size() == 0) {
                // had been deleted, erase it in db
                operator.erase("Album", "album_name = ?", new String[] { "'" + album_name + "'"});
            }
            else {
                // not be deleted, do nothing
            }
        }
        operator.close();
        myHandler.sendEmptyMessage(0x2);
    }

    /**
     * confirm what level you like to use or do something by level
     */
    private void do_afterScanImage() {
        Log.d("MESSAGE", "0x1");
        final MyDatabaseOperator operator = new MyDatabaseOperator(WelcomeActivity.this, Config.DB_NAME, Config.dbversion);
        List<Map> findResult = operator.search("Settings");

        try {
            // not first open the application
            String tmp = (String) findResult.get(0).get("notFirstIn");
            int level = Integer.parseInt((String) findResult.get(0).get("updateTime"));
            Log.d("LEVEL", "" + level);
            do_byLevel(level);
        } catch (Exception e) {
            // is first open this application

            AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
            builder.setTitle("选择图片处理的时间");
            builder.setIcon(R.drawable.things);
            builder.setItems(actions, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    final MyDatabaseOperator operator = new MyDatabaseOperator(WelcomeActivity.this, Config.DB_NAME, Config.dbversion);
                    ContentValues values = new ContentValues();
                    values.put("notFirstIn", "true");
                    values.put("updateTime", which);
                    operator.insert("Settings", values);
                    operator.close();
                    Toast.makeText(WelcomeActivity.this,  actions[which], Toast.LENGTH_SHORT).show();
                    do_byLevel(which);
                }
            });

            builder.show();
        }
        operator.close();
    }

    /**
     * when to classify images? you have three choices:
     * 1. when open app && when all images have been classified -> goto MainActivity
     * 2. when open app, just only scan picture and clear app'db, classifying images in background
     * 3. if image is not too much, goto MainActivity until new images have been classified, or do
     * it as choice 2
     * @param level 1, 2 or 3
     */
    private void do_byLevel(int level) {
        if (level == 0) {
            classifyNewImages();
        }
        else if (level == 1) {
            Config.needToBeClassified = notBeClassifiedImages;
            myHandler.sendEmptyMessage(0x24);
        }
        else if (level == 2) {
            if (notBeClassifiedImages.size() <= Config.imageNumber) {
                classifyNewImages();
            }
            else {
                Config.needToBeClassified = new ArrayList<>();
                Config.needToBeClassified.addAll(notBeClassifiedImages.subList(Config.imageNumber, notBeClassifiedImages.size()));
                notBeClassifiedImages = notBeClassifiedImages.subList(0, Config.imageNumber);
                classifyNewImages();
            }
        }
    }

    /**
     * when handler get the message that 'tf is end', the do this function to update UI,
     * goto MainActivity and finish this activity
     */
    private void do_finishThisActivity() {
        pbar.setVisibility(pbar.GONE);
        textView.setText("尽情享受吧");
        //setAppName();
        final Intent it = new Intent(getApplication(), MainActivity.class); //你要转向的Activity
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(it);
                WelcomeActivity.this.finish();
            }
        };
        timer.schedule(task, 1000 * 2);
    }
    private void setAppName() {
        textViewTitle.setText("New Feelings");
        textViewTitle.setTextSize(32);
        textViewTitle.setTextColor(Color.rgb(140, 21, 119));
    }

    /**
     * for every image will be classified, this function will classify them
     */
    private void classifyNewImages() {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
            Looper.prepare();
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
                Bitmap bitmap;
                value = new ContentValues();
                myoperator = new MyDatabaseOperator(WelcomeActivity.this, Config.DB_NAME, Config.dbversion);
                for (String image : notBeClassifiedImages) {
                    myHandler.sendEmptyMessage(0x23);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                    bitmap = BitmapFactory.decodeFile(image, options);
                    insertImageIntoDB(image, do_tensorflow(bitmap, classifier), myoperator, value);
                }
                myoperator.close();
                myHandler.sendEmptyMessage(0x24);
            Looper.loop();
            }
        }).start();
    }
}
