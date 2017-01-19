package com.fghz.album.entity;

/**
 * 相册中的元素---一个相册的组成
 * Created by me on 16-12-21.
 */

public class AlbumItem {
        private String name;
        private String url;
        public AlbumItem(String name, String url) {
            this.name = name;
            this.url = url;

        }
        public String getName() {
            return name;
        }
        public String getImageId() {
            return url;
        }
}
