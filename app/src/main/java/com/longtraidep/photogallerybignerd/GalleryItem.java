package com.longtraidep.photogallerybignerd;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GalleryItem {

    @Expose
    @SerializedName(value = "title")
    private String mCaption;

    @Expose
    @SerializedName(value = "id")
    private String mId;

    @Expose
    @SerializedName(value = "url_s")
    private String mUrl;

    @Override
    public String toString() {
        return mCaption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
