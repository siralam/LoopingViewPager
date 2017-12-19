package com.asksira.loopingviewpagerdemo;

public class PagerItem {

    private String imageUrl;
    private String description;

    public PagerItem (String url, String description) {
        imageUrl = url;
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
