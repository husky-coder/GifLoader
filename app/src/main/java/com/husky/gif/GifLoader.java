package com.husky.gif;

import android.graphics.Bitmap;
import android.view.View;

import java.util.HashMap;

/**
 * gif图片加载器
 */
public class GifLoader {

    private static final String TAG = "GifLoader";

    private static GifLoader gifLoader; // 单例对象

    private HashMap<String, View> gifViews;
    private HashMap<String, Long> gifHandles;
    private long gifHandle; // gif在native层的句柄，也可以认为是引用

    static {
        System.loadLibrary("gifloader");
    }

    private GifLoader() {
        gifViews = new HashMap<>(8);
        gifHandles = new HashMap<>(8);
    }

    public static GifLoader getInstance() {
        if (gifLoader == null) {
            synchronized (GifLoader.class) {
                if (gifLoader == null) {
                    gifLoader = new GifLoader();
                }
            }
        }
        return gifLoader;
    }

    /**
     * 加载gif图片返回native层的一个句柄（引用），后续的操作通过该句柄（引用）
     *
     * @param path gif图片路径
     */
    public void load(String path) {
        gifHandle = loadGif(path);
    }

    public int getWidth() {
        return getWidth(gifHandle);
    }

    public int getHeight() {
        return getHeight(gifHandle);
    }

    public int updateBitmap(Bitmap bitmap) {
        return updateBitmap(gifHandle, bitmap);
    }

    /**
     * native加载gif图片
     *
     * @param path gif图片路径
     * @return native层gif句柄（引用）
     */
    public native long loadGif(String path);

    /**
     * 获取gif图片宽度
     *
     * @param gifHandle native层gif句柄（引用）
     * @return gif图片宽度
     */
    public native int getWidth(long gifHandle);

    /**
     * 获取gif图片高度
     *
     * @param gifHandle native层gif句柄（引用）
     * @return gif图片高度
     */
    public native int getHeight(long gifHandle);

    /**
     * 更新图片
     *
     * @param gifHandle native层gif句柄（引用）
     * @param bitmap    java层bitmap
     * @return 延迟时间
     */
    public native int updateBitmap(long gifHandle, Bitmap bitmap);
}
