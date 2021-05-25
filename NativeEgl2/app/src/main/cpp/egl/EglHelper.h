//
// Created by baidu on 2021/5/25.
//

#ifndef NATIVEEGL_EGLHELPER_H
#define NATIVEEGL_EGLHELPER_H

#include "EGL/egl.h"
#include "../log/JniLog.h"

class EglHelper {

public:
    EGLDisplay mEglDisplay;
    EGLContext mEglContext;
    EGLSurface mEglSurface;

public:
    EglHelper();
    ~EglHelper();

    int initEgl(EGLNativeWindowType surface);
    int swapBuffers();
    void destroyEgl();
};


#endif //NATIVEEGL_EGLHELPER_H
