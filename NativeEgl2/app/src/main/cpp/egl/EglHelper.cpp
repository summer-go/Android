//
// Created by baidu on 2021/5/25.
//

#include "EglHelper.h"

int EglHelper::initEgl(EGLNativeWindowType surface) {
    // 1.得到默认的显示设备，即屏幕窗口
    mEglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if(mEglDisplay == EGL_NO_DISPLAY){
        LOGE("eglGetDisplay error");
        return -1;
    }

    // 2.初始化默认显示设备
    EGLint *version = new EGLint[2];
    if(!eglInitialize(mEglDisplay, &version[0], &version[1])){
        LOGE("eglInitialize error");
        return -1;
    }

    // 3.设置显示设备的属性
    const EGLint attrib_config_list[] = {
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_DEPTH_SIZE, 8,
            EGL_STENCIL_SIZE, 8,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_NONE
    };
    // 3.1根据所需的参数获取符合该参数的config_size,主要是解决有些手机eglChooseConfig失败的兼容性问题
    EGLint  num_config;
    if(!eglChooseConfig(mEglDisplay, attrib_config_list, NULL, 1, &num_config)){
        LOGE("eglChooseConfig error");
        return -1;
    }
    // 3.2根据获取的config_size得到eglConfig
    EGLConfig eglConfig;
    if(!eglChooseConfig(mEglDisplay, attrib_config_list, &eglConfig, num_config, &num_config)){
        LOGE("eglChooseConfig error");
        return -1;
    }
    // 4.创建egl上下文 eglCreateContext
    const EGLint attrib_ctx_list[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL_NONE
    };
    mEglContext = eglCreateContext(mEglDisplay, eglConfig, NULL, attrib_ctx_list);
    if(mEglContext == EGL_NO_CONTEXT){
        LOGE("eglCreateContext error");
        return -1;
    }
    // 5.创建渲染的surface
    mEglSurface = eglCreateWindowSurface(mEglDisplay, eglConfig, surface, NULL);
    if(mEglSurface == EGL_NO_SURFACE){
        LOGE("eglCreateWindowSurface error");
        return -1;
    }
    //6.绑定eglContext和Surface到display
    if(!eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface,mEglContext)){
        LOGE("eglCreateWindowSurface error");
        return -1;
    }
    // 7.刷新数据，显示渲染场景 -- eglSwapBuffers


    return 0;

}



int EglHelper::swapBuffers() {
    if(mEglDisplay != EGL_NO_DISPLAY && mEglSurface != EGL_NO_SURFACE){
        eglSwapBuffers(mEglDisplay, mEglSurface);
        return 0;
    }
    return -1;
}

void EglHelper::destroyEgl() {
    if(mEglDisplay != EGL_NO_DISPLAY){
        // 解绑display上的eglContext和Surface
        eglMakeCurrent(mEglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);

        // 销毁surface和eglContext
        if(mEglSurface != EGL_NO_SURFACE){
            eglDestroySurface(mEglDisplay, mEglSurface);
            mEglSurface = EGL_NO_SURFACE;
        }

        if(mEglContext != EGL_NO_CONTEXT){
            eglDestroyContext(mEglDisplay, mEglContext);
            mEglContext = EGL_NO_CONTEXT;
        }

        if(mEglDisplay != EGL_NO_DISPLAY){
            eglTerminate(mEglDisplay);
            mEglDisplay = EGL_NO_DISPLAY;
        }
    }
}

EglHelper::EglHelper() {
    mEglDisplay = EGL_NO_DISPLAY;
    mEglSurface = EGL_NO_SURFACE;
    mEglContext = EGL_NO_CONTEXT;
}

EglHelper::~EglHelper() {

}
