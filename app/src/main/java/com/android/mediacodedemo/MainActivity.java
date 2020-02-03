package com.android.mediacodedemo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

    private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final int REQUEST_PERMISSION_OK = 0x1;

    private SurfaceView mSurfaceView;
    private MediaCodecThread mCodecThread = null;
    private String mVideoPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "DCIM" + File.separator + "Camera" + File.separator
            + "video_20200127_162636.mp4";
    private String mVideoUrl = "http://videoconverter.vivo.com.cn/201706/655_1498479540118.mp4.main.m3u8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (SurfaceView) findViewById(R.id.video_surface);
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCodecThread == null) {
            mCodecThread = new MediaCodecThread(holder.getSurface(), mVideoPath);
            mCodecThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCodecThread != null) {
            mCodecThread.interrupt();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSION_OK);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_OK) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,  "存储权限已开通", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,  "存储权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
