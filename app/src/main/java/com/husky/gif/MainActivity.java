package com.husky.gif;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView gifView;
    private Bitmap bitmap;

    private String gifPath = StorageUtil.getExternalStoragePublicDirectory(this, "") + "/demo.gif";

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
     * @param view
     */
    public void copyGifToSdcard(View view) {
        File file = new File(gifPath);
        if (file.exists()) {
            Toast.makeText(this, "gif已经存在sd卡根目录！", Toast.LENGTH_SHORT).show();
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
}
