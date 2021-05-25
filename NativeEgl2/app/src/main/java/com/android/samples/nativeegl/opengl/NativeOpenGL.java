package com.android.samples.nativeegl.opengl;

import android.view.Surface;

/**
 * @author xiatian05
 * @date 2021/5/25
 * @ver 11.20.0
 */
class NativeOpenGL {
    static {
        System.loadLibrary("native-lib");
    }

    public native void nativeSurfaceCreate(Surface surface);

    public native void nativeSurfaceChanged(int width, int height);

    public native void nativeSurfaceDestroyed();
}
