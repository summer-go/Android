package com.android.samples.camera1;

import android.nfc.Tag;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class EglMainActivity extends AppCompatActivity implements EglSurfaceView.Renderer{
    public static final String TAG = "EglMainActivity";
    private EglSurfaceView eglSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_main);

        eglSurfaceView = new EglSurfaceView(this);
        eglSurfaceView.setRenderer(this);

        eglSurfaceView.setRendererMode(EglSurfaceView.RENDERMODE_WHEN_DIRTY);
        eglSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eglSurfaceView.requestRender();
            }
        });

        setContentView(eglSurfaceView);
    }

    @Override
    public void onSurfaceCreated() {
        Log.e(TAG, "onSurfaceCreated");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.e(TAG, "onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        Log.e(TAG, "onDrawFrame");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        float r = (float) Math.random();
        float g = (float) Math.random();
        float b = (float) Math.random();

//        GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f);

        GLES20.glClearColor(r, g, b, 1.0f);

    }
}