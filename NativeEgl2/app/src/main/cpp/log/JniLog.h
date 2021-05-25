//
// Created by baidu on 2021/5/25.
//

#ifndef NATIVEEGL_JNILOG_H
#define NATIVEEGL_JNILOG_H

#include "android/log.h"

#define LOGD(FORMAT, ...) __android_log_print (ANDROID_LOG_DEBUG, "jniLog", FORMAT, ##__VA_ARGS__)
#define LOGE(FORMAT, ...) __android_log_print (ANDROID_LOG_ERROR, "jniLog", FORMAT, ##__VA_ARGS__)


#endif //NATIVEEGL_JNILOG_H
