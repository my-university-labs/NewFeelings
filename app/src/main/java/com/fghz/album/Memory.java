package com.fghz.album;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fghz.album.adapter.MemoryAdapter;
import com.fghz.album.entity.MemoryItem;
import com.fghz.album.R;

import static com.fghz.album.utils.ImagesScaner.getAlbumInfo;

/**
 * 回忆 栏的fregment的具体定义
 */

public class Memory extends Fragment {
    private MemoryAdapter adapter;
    private List<Map<String, String>> result;
    public Memory() {

    }
    private List<MemoryItem> memoryList = new ArrayList<MemoryItem>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_memory,container,false);
        GridView gridView = (GridView) view.findViewById(R.id.memory_list);
        initMemory();

        adapter = new MemoryAdapter(getActivity(), R.layout.memory_item, memoryList);

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Log.d("Memeory", "" + position);
                Intent intent = new Intent(getActivity(), MovieShowActivity.class);
                if (memoryList.get(position).getType() == "全部图片") {

                } else {
                    intent.putExtra("type", memoryList.get(position).getType());
                }
                startActivity(intent);
            }
        });
        return view;

    }
    private void initMemory() {
        MemoryItem memory;
        result = getAlbumInfo(getContext());
        if (result.size() == 0) return;
        memory = new MemoryItem(result.get(0).get("show_image"), "全部图片");
        memoryList.add(memory);

        for (Map<String, String> s: result) {
            memory = new MemoryItem(s.get("show_image"), s.get("album_name"));
            memoryList.add(memory);
        }
    }
    public void onRefresh() {
        memoryList.clear();
        initMemory();
        adapter.notifyDataSetChanged();
    }
}

