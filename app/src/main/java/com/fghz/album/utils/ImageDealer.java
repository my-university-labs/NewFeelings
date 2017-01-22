package com.fghz.album.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.fghz.album.Config;
import com.fghz.album.dao.MyDatabaseOperator;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.TensorFlowImageClassifier;

import java.util.List;
import java.util.Map;

import static android.media.ThumbnailUtils.extractThumbnail;
import static com.fghz.album.utils.ImagesScaner.getBitmap;

/**
 * Created by dongchangzhang on 1/19/17.
 */

public class ImageDealer {
    /**
     * resize bitmap for tf
     * @param bitmap
     * @return
     */
    public static Bitmap dealImageForTF(Bitmap bitmap) {
        try {
            // resize
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float scaleWidth = ((float) Config.INPUT_SIZE) / width;
            float scaleHeight = ((float) Config.INPUT_SIZE) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            return newbm;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * resize image by url
     * @param context
     * @param url
     * @return
     */
    public static Bitmap getThumbnails(Context context, String url) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        bitmap = BitmapFactory.decodeFile(url, options);
        bitmap = extractThumbnail(bitmap,180 , 180);
        return bitmap;
    }

    /**
     * insert new image into db
     * @param image
     * @param results
     * @param operator
     * @param value
     */
    public static void insertImageIntoDB(String image, List<Classifier.Recognition> results,
                                          MyDatabaseOperator operator, ContentValues value) {
        if (results == null) return;
        List<Map> findResult;
        for (Classifier.Recognition cr : results) {
            String type = cr.getTitle();
            // AlbumPhotos
            value.clear();
            value.put("album_name", type);
            value.put("url", image);
            operator.insert("AlbumPhotos", value);
            // Album
            findResult = operator.search("Album", "album_name = '" + type + "'");
            if (findResult.size() == 0) {
                value.clear();
                value.put("album_name", type);
                value.put("show_image", image);
                operator.insert("Album", value);
            }
            //TFInfromation
            value.clear();
            value.put("url", image);
            value.put("tf_type", type);
            value.put("confidence", cr.getConfidence());
            operator.insert("TFInformation", value);
        }
    }
    /**
     * use tf to classify the image
     * @param bitmap
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static List<Classifier.Recognition>
    do_tensorflow(Bitmap bitmap, TensorFlowImageClassifier classifier) {
        // resize image
        Bitmap newbm = dealImageForTF(bitmap);
        // get results
        try {
            return classifier.recognizeImage(newbm);
        } catch (Exception e) {
            Log.e("TF-ERROR", "1");
            return null;
        }
    }

}
