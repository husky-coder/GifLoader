package com.husky.gif;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * 获取存储路径工具类
 * <p>
 * Created by luhailong on 2017/7/4.
 */
public class StorageUtil {

    /**
     * 检查外部存储是否可用
     *
     * @return
     */
    public static boolean isExitsSdcard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable())
            return true;
        else
            return false;
    }

    /**
     * 获取内部存储缓存路径(不需要读写权限(4.4及以上)也不需要判断外部存储是否可用)
     *
     * @param context
     * @return
     */
    public static String getInternalCacheDir(Context context) {
        return context.getCacheDir().getPath();
    }

    /**
     * 获取内部存储文件路径(不需要读写权限(4.4及以上)也不需要判断外部存储是否可用)
     * 根据type获取对应的目录路径(type为空返回内部存储文件根目录路径)
     *
     * @param context
     * @param type
     * @return
     */
    public static String getInternalFilesDir(Context context, String type) {
        return TextUtils.isEmpty(type) ? context.getFilesDir().getPath() : (new File(context.getFilesDir(), type)).getPath();
    }

    /**
     * 获取外部存储缓存路径(不需要读写权限(4.4及以上))
     * 外部存储不可用返回内部存储缓存路径
     *
     * @param context
     * @return
     */
    public static String getExternalCacheDir(Context context) {
        return isExitsSdcard() ? context.getExternalCacheDir().getPath() : getInternalCacheDir(context);
    }

    /**
     * 获取外部存储文件路径(不需要读写权限(4.4及以上))
     * 外部存储不可用返回内部存储文件路径
     * 根据type获取对应的目录路径(type为空返回外部存储缓存路径)
     *
     * @param context
     * @return
     */
    public static String getExternalFilesDir(Context context, String type) {
        return isExitsSdcard() ? TextUtils.isEmpty(type) ? getExternalCacheDir(context) : context.getExternalFilesDir(type).getPath() : getInternalFilesDir(context, type);
    }

    /**
     * 获取外部存储共有路径(根据type获取对应的目录)(type为空返回外部存储共有根目录路径)
     * 外部存储不可用返回内部存储文件路径
     *
     * @param type
     * @return
     */
    public static String getExternalStoragePublicDirectory(Context context, String type) {
        return isExitsSdcard() ? TextUtils.isEmpty(type) ? Environment.getExternalStorageDirectory().getPath() : Environment.getExternalStoragePublicDirectory(type).getPath() : getInternalFilesDir(context, type);
    }
}
