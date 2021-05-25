package com.android.samples.nativeegl.opengl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author xiatian05
 * @date 2021/5/25
 * @ver 11.20.0
 */
class NativeGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private NativeOpenGL mNativeOpenGL;

    public NativeGLSurfaceView(Context context) {
        this(context, null);
    }

    public NativeGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NativeGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mNativeOpenGL = new NativeOpenGL();
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mNativeOpenGL.nativeSurfaceCreate(holder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mNativeOpenGL.nativeSurfaceChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mNativeOpenGL.nativeSurfaceDestroyed();
    }
}
