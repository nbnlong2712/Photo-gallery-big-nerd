package com.longtraidep.photogallerybignerd;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private boolean mHasQuit = false;
    private static final int MESSAGE_DOWNLOAD = 0;    //What

    private Handler mRequestHandler;                  //Handler
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>(); //T là key, String là value  //Obj

    private Handler mResponseHandler;       //Giữ handler được truyền từ Main Thread (main thread thường gửi yêu cầu download image, và handler này nhận nó
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener listener)
    {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url)
    {
        Log.i(TAG, "Got a URL: " + url);
        if (url == null)
        {
            mRequestMap.remove(target);
        }
        else {
            mRequestMap.put(target, url);      //target không trùng lặp, target is key, url is value
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler()
        {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD)    //what: MESSAGE_DOWNLOAD
                {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);    //tải xuống thứ mình cần từ target (key)
                }
            }
        };
    }

    public void clearQueue()
    {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    public void handleRequest(final T target)
    {
        try {
            final String url = mRequestMap.get(target);     //lấy url(value) từ target(key)
            if (url == null)
                return;

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);   //từ url, tải xuống thứ cần (dưới dạng byte, ở trường hợp này là 1 ảnh nào đó)
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);  //decode ảnh đó ra bitmap
            Log.i(TAG, "Bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit)
                    {
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image", e);
        }
    }
}
