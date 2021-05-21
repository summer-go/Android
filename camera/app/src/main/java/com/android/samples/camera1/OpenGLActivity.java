package com.android.samples.camera1;

import android.opengl.GLSurfaceView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class OpenGLActivity extends AppCompatActivity {

    private GLSurfaceView gLView;
    private MyGLSurfaceView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_open_g_l);


        glView = new MyGLSurfaceView(this);
        setContentView(glView);
    }
}