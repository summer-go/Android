#include <jni.h>
#include <string>

#include "egl/EglHelper.h"
#include "egl/EglThread.h"
#include "android/native_window.h"
#include "android/native_window_jni.h"
#include "pthread.h"

EglThread *eglThread = NULL;

void callBackOnCreate(){
    LOGE("callBackOnCreate");
}

void callBackOnChange(int width, int height){
    LOGE("callBackOnChange");
}

void callBackOnDraw(){
    glClearColor(0.0, 1.0, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    LOGE("callBackOnDraw");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_android_samples_nativeegl_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_android_samples_nativeegl_opengl_NativeOpenGL_nativeSurfaceCreate(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jobject surface) {
    eglThread = new EglThread();
    eglThread->callBackOnCreate(callBackOnCreate);
    eglThread->callBackOnChange(callBackOnChange);
    eglThread->callBackOnDraw(callBackOnDraw);
    eglThread->setRenderModule(RENDER_MODULE_MANUAL);

    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
    eglThread->onSurfaceCreate(nativeWindow);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_samples_nativeegl_opengl_NativeOpenGL_nativeSurfaceChanged(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jint width,
                                                                            jint height) {
    if(eglThread){
        eglThread->onSurfaceChange(width, height);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_samples_nativeegl_opengl_NativeOpenGL_nativeSurfaceDestroyed(JNIEnv *env,
                                                                              jobject thiz) {
    if(eglThread){
        eglThread->isExit = true;
        //等待线程结束
        pthread_join(eglThread->mEglThread, NULL);
        delete eglThread;
        eglThread = NULL;
    }
}