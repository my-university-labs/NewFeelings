package com.fghz.album.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.demo.Classifier;

import java.util.List;

import com.fghz.album.Config;
import com.fghz.album.R;


/**
 * Created by me on 17-1-1.
 */

public class PhotoTypeAdapter extends ArrayAdapter<Classifier.Recognition> {
    private int resourceId;
    public PhotoTypeAdapter(Context context, int textViewResourceId,
                         List<Classifier.Recognition> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Classifier.Recognition type= getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        ImageView memoryImage = (ImageView) view.findViewById(R.id.type_image);
        int i;
        for (i = 0; i < Config.tf_type_times; ++i) {
            if (Config.tf_type_name[i].equals(type.getTitle())) {
                break;
            }
        }

        memoryImage.setImageResource(Config.tf_type_image[i]);
        TextView tv = (TextView) view.findViewById(R.id.type_name);
        tv.setText("图片类别: " + type.getTitle() + "\n自信程度: " + type.getConfidence());
        return view;
    }
}
