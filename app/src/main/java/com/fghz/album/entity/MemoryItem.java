package com.fghz.album.entity;

/**
 * 回忆的
 * Created by me on 16-12-21.
 */

public class MemoryItem {
    private String imageId;
    private String type;
    public MemoryItem(String imageId, String type) {
        this.imageId = imageId;
        this.type = type;
    }
    public String getImageId() {
        return imageId;
    }
    public String getType() { return type; }
}
