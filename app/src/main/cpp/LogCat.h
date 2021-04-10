//
// Created by luhailong on 2021/4/3.
//

#ifndef GIFFRAME_LOGCAT_H
#define GIFFRAME_LOGCAT_H

#include <android/log.h>

// 定义常用宏
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "GifFrame", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "GifFrame", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "GifFrame", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "GifFrame", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "GifFrame", __VA_ARGS__)

#endif //GIFFRAME_LOGCAT_H
