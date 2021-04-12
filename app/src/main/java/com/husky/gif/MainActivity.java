package com.husky.gif;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ImageView gifView;
    private Bitmap bitmap;

    private String gifPath;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int delay = GifLoader.getInstance().updateBitmap(bitmap);
            gifView.setImageBitmap(bitmap);
            handler.sendEmptyMessageDelayed(0, delay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gifView = (ImageView) findViewById(R.id.gifview);

        gifPath = StorageUtil.getExternalFilesDir(this, null) + "/demo.gif";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            copyGifToSdcard(this);
        }
    }

    /**
     * 播放gif
     *
     * @param view
     */
    public void openGif(View view) {
        Log.d(TAG, "gif = " + gifPath);
        File file = new File(gifPath);

        if (!file.exists()) {
            Toast.makeText(this, "gif文件不存在！", Toast.LENGTH_SHORT).show();
            return;
        }

        GifLoader.getInstance().load(file.getAbsolutePath());
        Log.d(TAG, "file = " + file.getName());

        int width = GifLoader.getInstance().getWidth();
        int height = GifLoader.getInstance().getHeight();
        Log.d(TAG, "width = " + width);
        Log.d(TAG, "height = " + height);

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int delay = GifLoader.getInstance().updateBitmap(bitmap);
        Log.d(TAG, "delay = " + delay);
        gifView.setImageBitmap(bitmap);
        handler.sendEmptyMessageDelayed(0, delay);
    }

    /**
     * 复制assets目录下的gif文件到sd卡根目录
     *
     * @param context
     */
    public void copyGifToSdcard(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(gifPath);
                if (file.exists()) {
                    Toast.makeText(context, "gif已经存在sd卡根目录！", Toast.LENGTH_SHORT).show();
                    return;
                }

                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    is = getAssets().open("demo.gif");
                    fos = new FileOutputStream(gifPath);

                    byte[] buffer = new byte[1024];
                    int size = -1;
                    while ((size = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, size);
                    }
                    Log.d(TAG, "文件复制sd卡根目录成功！");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        List<String> permissions = new ArrayList<>();
        // 检查是否获取了权限
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissions.size() == 0) {
            // 已经有了权限
            copyGifToSdcard(this);
        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用logAction，否则申请到权限之后再调用。
            String[] requestPermissions = new String[permissions.size()];
            permissions.toArray(requestPermissions);
            requestPermissions(requestPermissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean hasPermission = true;
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            hasPermission = false;
                            break;
                        }
                    }
                    if (hasPermission) {
                        // 获得用户授权
                        copyGifToSdcard(this);
                    } else {
                        // 如果用户没有授权，那么应该说明意图，引导用户去设置里面授权。
                        Toast.makeText(this, "应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                        finish();
                    }
                }
                break;
            default:
        }
    }
}
