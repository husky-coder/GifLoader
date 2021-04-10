#include <jni.h>
#include <malloc.h>
#include <cstring>
#include <android/bitmap.h>

#include "LogCat.h"

extern "C" {
#include "gif_lib.h"
}

// native中表示颜色的顺序是abgr
// 定义宏运算颜色
#define argb(a, r, g, b) (((a)&0xff)<<24)|(((b)&0xff)<<16)|(((g)&0xff)<<8)|((r)&0xff)

// 自定义保存gif信息结构体，其实主要是存放当前帧索引，其他信息可以直接通过GifFileType获取
struct GifBean {
    int totalFrame;     // 总帧数
    int currentFrame;   // 当前帧索引
    int *delay;         // 当前帧延迟时间
};

extern "C"
JNIEXPORT jlong JNICALL
Java_com_husky_gif_GifLoader_loadGif(JNIEnv *env, jobject thiz, jstring path_) {
    // TODO: implement loadGif()
    // 将jni字符串转换为c++的字符串
    const char *path = env->GetStringUTFChars(path_, 0);

    // 定义一个接收打开文件错误码变量
    int errorCode;
    // 打开gif图片
    GifFileType *gifFileType = DGifOpenFileName(path, &errorCode);
    // 判断错误码处理错误情况
//    if (errorCode != 0) {
//        LOGD("errorCode = %d", errorCode);
//        return 0L;
//    }

    // 初始化缓冲区，打开gif文件后就马上调用初始化缓冲区
    DGifSlurp(gifFileType);

    // 创建保存gif信息结构体
    GifBean *gifBean = static_cast<GifBean *>(malloc(sizeof(GifBean)));
    // 清空刚开辟的内存空间，通过malloc开辟的内存空间需要手动清空，不然会保存之前的内容(脏数据)
    memset(gifBean, 0, sizeof(GifBean));

    // 给gifBean赋值
    gifBean->totalFrame = gifFileType->ImageCount;
    gifBean->currentFrame = 0;
    gifBean->delay = 0;
    // 将gifBean与gifFileType进行绑定，类似setTag，在使用的时候获取
    gifFileType->UserData = gifBean;

    /**
     * 测试每一帧的延迟时间
     */
    SavedImage *savedImage;    // 某一帧
    ExtensionBlock *extensionBlock = nullptr;    // 某一帧里的扩展快
    int i, j, delay;
    for (i = 0; i < gifFileType->ImageCount; i++) { // 遍历所有帧
        savedImage = &(gifFileType->SavedImages[i]);    // 得到某一帧
        for (int j = 0; j < savedImage->ExtensionBlockCount; j++) { // 遍历某一帧中的所有扩展快
            if (savedImage->ExtensionBlocks[j].Function ==
                GRAPHICS_EXT_FUNC_CODE) {    // 找到对应的扩展快，里面包含每一帧的延迟时间
                extensionBlock = &(savedImage->ExtensionBlocks[j]);
            }
        }
        // 遍历完某一帧里的所有扩展快后判断是否找到对应扩展快
        if (extensionBlock) {
            /**
             *
             */
            delay = 10 * (extensionBlock->Bytes[2] << 8 | extensionBlock->Bytes[1]);
            LOGD("第%d帧延迟%d毫秒", i, delay);
        }
    }


    // 释放c++字符串
    env->ReleaseStringUTFChars(path_, path);
    // 将指向GifFileType结构体的指针转化为jlong类型返回给java层
    return reinterpret_cast<jlong>(gifFileType);
//    return (jlong) gifFileType; // 这样转换也行
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_husky_gif_GifLoader_getWidth(JNIEnv *env, jobject thiz, jlong gif_handle) {
    // TODO: implement getWidth()
    GifFileType *gifFileType = reinterpret_cast<GifFileType *>(gif_handle);
    return gifFileType->SWidth;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_husky_gif_GifLoader_getHeight(JNIEnv *env, jobject thiz, jlong gif_handle) {
    // TODO: implement getHeight()
    GifFileType *gifFileType = reinterpret_cast<GifFileType *>(gif_handle);
    return gifFileType->SHeight;
}

/**
 * 绘制bitmap
 */
void drawBitmap(GifFileType *gifFileType, AndroidBitmapInfo bitmapInfo, int *pixels) {
    GifBean *gifBean = static_cast<GifBean *>(gifFileType->UserData);

    // 1、gifFileType->SavedImages获取到gif图片队列地址，也就是一维数组
    // 2、通过索引获取当前帧信息，当前帧信息包含的不是完整的像素信息，而是经过压缩的数据，例如颜色索引等
    SavedImage savedImage = gifFileType->SavedImages[gifBean->currentFrame];

    /**
     * SavedImage中比较重要的两部分是描述和像素（在颜色表中的索引）
     * 1、图片的有效像素不一定是完整的图片宽高，所以在图片描述里保存了上边和左边的偏移量、有效宽高
     * 2、
     */
    GifImageDesc gifImageDesc = savedImage.ImageDesc;
    // 获取像素颜色字典
    ColorMapObject *colorMapObject = gifImageDesc.ColorMap;

    /**
     * 测试扩展快中的处置方法
     * 0 - 不使用处置方法
     * 1 - 不处置图形，把图形从当前位置移除
     * 2 - 回复到背景色
     * 3 - 回复到先前状态
     * 4 - 7 自定义
     */
    ExtensionBlock extensionBlock;
    for (int i = 0; i < savedImage.ExtensionBlockCount; i++) {
        if (savedImage.ExtensionBlocks[i].Function == GRAPHICS_EXT_FUNC_CODE) {
            extensionBlock = savedImage.ExtensionBlocks[i];
            break;
        }
    }
    // 判断处置方法

    // 定义指针记录bitmap的起始地址地址
    int *px = pixels;
    // 定位到有效像素行起始地址
    px = (int *) ((char *) px + bitmapInfo.stride * gifImageDesc.Top);
    // 1、先遍历行，也就是高度
    for (int y = gifImageDesc.Top; y < gifImageDesc.Top + gifImageDesc.Height; y++) {
        // 记录每一行的起始地址
        int *line = px;
        // 2、再遍历列，也就是宽度
        for (int x = gifImageDesc.Left; x < gifImageDesc.Left + gifImageDesc.Width; x++) {
            /**
             * (y - gifImageDesc.Top)   得到的是有效像素的第n行
             * (y - gifImageDesc.Top) * gifImageDesc.Width  得到的是第n行开始的位置
             * (x - gifImageDesc.Left)  得到的是有效像素列偏移的位置
             *
             * (y - gifImageDesc.Top) * gifImageDesc.Width + (x - gifImageDesc.Left)
             * 得到的是有效像素偏移第n行第n列的位置（也就是数组下标，索引）
             */
            // 定义bitmap中每个像素（其实不是真正的像素，是bitmap中二维数组的索引）的索引
            int pointPixel = (y - gifImageDesc.Top) * gifImageDesc.Width + (x - gifImageDesc.Left);
            // 根据下标得到像素（其实也不是真正的像素）
            GifByteType gifByteType = savedImage.RasterBits[pointPixel];
            // 真正的像素颜色
            GifColorType gifColorType = colorMapObject->Colors[gifByteType];
            // 将真正的像素颜色渲染到bitmap二维数组指定的索引中
            line[x] = argb(255, gifColorType.Red, gifColorType.Green, gifColorType.Blue);
        }
        /**
         * 遍历完一行后开始换行
         * px   是某一行的其实位置
         *
         * （此处可以使用字节的步进也可以使用像素的步进）
         * bitmapInfo.stride    是每一行的字节数，一个像素四个字节
         * (char *) gif数据流以字符形式存储，所以转换为字符再步进，避免错乱影响像素渲染
         */
        px = (int *) ((char *) px + bitmapInfo.stride);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_husky_gif_GifLoader_updateBitmap(JNIEnv *env, jobject thiz, jlong gif_handle,
                                          jobject bitmap) {
    // TODO: implement updateBitmap()
    // 获取bitmap宽高的两种方式
    // 1、通过GifFileType获取
    GifFileType *gifFileType = reinterpret_cast<GifFileType *>(gif_handle);
//    int width = gifFileType->SWidth;
//    int height = gifFileType->SHeight;

    // 2、通过bitmap获取
    AndroidBitmapInfo bitmapInfo;   // 定义native层对bitmap操作的信息对象
    AndroidBitmap_getInfo(env, bitmap, &bitmapInfo);    // 将jni传进来的bitmap转化为native层操作bitmap的信息对象
    int width = bitmapInfo.width;   // 获取宽度
    int height = bitmapInfo.height; // 获取高度

    int *pixels;    // int指针，一级指针，指向一维数组
    // 锁住bitmap，并将bitmap转换为二维数组
    // pixels指针取地址，表示指针的指针（二维指针，也就是二维数组），bitmap在native层都是以二维数组的形式表示
    AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&pixels));

    // 绘制渲染bitmap
    drawBitmap(gifFileType, bitmapInfo, pixels);

    // 解锁
    AndroidBitmap_unlockPixels(env, bitmap);

    // 帧索引更新
    GifBean *gifBean = static_cast<GifBean *>(gifFileType->UserData);
    gifBean->currentFrame++;
    if (gifBean->currentFrame > gifBean->totalFrame - 1) {
        gifBean->currentFrame = 0;  // 重置从第一帧开始
    }

    // 返回下一帧延迟播放的时间，目前返回100，通过固定延迟时间处理
    return 100;
}