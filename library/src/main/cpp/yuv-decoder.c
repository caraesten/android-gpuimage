#include <jni.h>

#include <android/bitmap.h>
#include <GLES3/gl3.h>
#include <android/hardware_buffer_jni.h>

JNIEXPORT void JNICALL
Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_YUVtoRBGA(JNIEnv *env, jobject obj,
                                                                       jbyteArray yuv420sp,
                                                                       jint width, jint height,
                                                                       jintArray rgbOut) {
    int sz;
    int i;
    int j;
    int Y;
    int Cr = 0;
    int Cb = 0;
    int pixPtr = 0;
    int jDiv2 = 0;
    int R = 0;
    int G = 0;
    int B = 0;
    int cOff;
    int w = width;
    int h = height;
    sz = w * h;

    jint *rgbData = (jint *) ((*env)->GetPrimitiveArrayCritical(env, rgbOut, 0));
    jbyte *yuv = (jbyte *) (*env)->GetPrimitiveArrayCritical(env, yuv420sp, 0);

    for (j = 0; j < h; j++) {
        pixPtr = j * w;
        jDiv2 = j >> 1;
        for (i = 0; i < w; i++) {
            Y = yuv[pixPtr];
            if (Y < 0) Y += 255;
            if ((i & 0x1) != 1) {
                cOff = sz + jDiv2 * w + (i >> 1) * 2;
                Cb = yuv[cOff];
                if (Cb < 0) Cb += 127; else Cb -= 128;
                Cr = yuv[cOff + 1];
                if (Cr < 0) Cr += 127; else Cr -= 128;
            }

            //ITU-R BT.601 conversion
            //
            //R = 1.164*(Y-16) + 2.018*(Cr-128);
            //G = 1.164*(Y-16) - 0.813*(Cb-128) - 0.391*(Cr-128);
            //B = 1.164*(Y-16) + 1.596*(Cb-128);
            //
            Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
            R = Y + (Cr << 1) + (Cr >> 6);
            if (R < 0) R = 0; else if (R > 255) R = 255;
            G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
            if (G < 0) G = 0; else if (G > 255) G = 255;
            B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
            if (B < 0) B = 0; else if (B > 255) B = 255;
            rgbData[pixPtr++] = 0xff000000 + (R << 16) + (G << 8) + B;
        }
    }

    (*env)->ReleasePrimitiveArrayCritical(env, rgbOut, rgbData, 0);
    (*env)->ReleasePrimitiveArrayCritical(env, yuv420sp, yuv, 0);
}

JNIEXPORT void JNICALL
Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_YUVtoARBG(JNIEnv *env, jobject obj,
                                                                       jbyteArray yuv420sp,
                                                                       jint width, jint height,
                                                                       jintArray rgbOut) {
    int sz;
    int i;
    int j;
    int Y;
    int Cr = 0;
    int Cb = 0;
    int pixPtr = 0;
    int jDiv2 = 0;
    int R = 0;
    int G = 0;
    int B = 0;
    int cOff;
    int w = width;
    int h = height;
    sz = w * h;

    jint *rgbData = (jint *) ((*env)->GetPrimitiveArrayCritical(env, rgbOut, 0));
    jbyte *yuv = (jbyte *) (*env)->GetPrimitiveArrayCritical(env, yuv420sp, 0);

    for (j = 0; j < h; j++) {
        pixPtr = j * w;
        jDiv2 = j >> 1;
        for (i = 0; i < w; i++) {
            Y = yuv[pixPtr];
            if (Y < 0) Y += 255;
            if ((i & 0x1) != 1) {
                cOff = sz + jDiv2 * w + (i >> 1) * 2;
                Cb = yuv[cOff];
                if (Cb < 0) Cb += 127; else Cb -= 128;
                Cr = yuv[cOff + 1];
                if (Cr < 0) Cr += 127; else Cr -= 128;
            }

            //ITU-R BT.601 conversion
            //
            //R = 1.164*(Y-16) + 2.018*(Cr-128);
            //G = 1.164*(Y-16) - 0.813*(Cb-128) - 0.391*(Cr-128);
            //B = 1.164*(Y-16) + 1.596*(Cb-128);
            //
            Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
            R = Y + (Cr << 1) + (Cr >> 6);
            if (R < 0) R = 0; else if (R > 255) R = 255;
            G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
            if (G < 0) G = 0; else if (G > 255) G = 255;
            B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
            if (B < 0) B = 0; else if (B > 255) B = 255;
            rgbData[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
        }
    }

    (*env)->ReleasePrimitiveArrayCritical(env, rgbOut, rgbData, 0);
    (*env)->ReleasePrimitiveArrayCritical(env, yuv420sp, yuv, 0);
}

JNIEXPORT int JNICALL
Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_drawHardwareBufferToTexture(
        JNIEnv *jenv, jclass thiz, jint width, jint height, jint format, jint internalFormat, jint type, jobject hardwareBufferObj) {
    // TODO (factor out into a separate class, surround w/ API 26 checks)
    AHardwareBuffer* hardwareBuffer = AHardwareBuffer_fromHardwareBuffer(jenv, hardwareBufferObj);
    AHardwareBuffer_Desc desc;
    AHardwareBuffer_describe(hardwareBuffer, &desc);

    void* bufferPtr;

    int res = AHardwareBuffer_lock(hardwareBuffer, AHARDWAREBUFFER_USAGE_CPU_READ_RARELY, -1, NULL, &bufferPtr);

    if (res != 0) {
        return -1;
    }
    glPixelStorei(GL_UNPACK_ROW_LENGTH, desc.stride);
    glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, desc.width, desc.height, 0, format, type, bufferPtr);
    glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
    int res2 = AHardwareBuffer_unlock(hardwareBuffer, NULL);
    if (res2 != 0) {
        return -2;
    }
    return glGetError();
}

JNIEXPORT int JNICALL
Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_drawHardwareBufferToTextureWithId(
        JNIEnv *jenv, jclass thiz, jint textureId, jint format, jint type, jint width, jint height, jobject hardwareBufferObj) {
    // TODO (factor out into a separate class, surround w/ API 26 checks)
    AHardwareBuffer* hardwareBuffer = AHardwareBuffer_fromHardwareBuffer(jenv, hardwareBufferObj);
    void* bufferPtr;
    int res = AHardwareBuffer_lock(hardwareBuffer, AHARDWAREBUFFER_USAGE_CPU_READ_RARELY, -1, NULL, &bufferPtr);
    if (res != 0) {
        return -1;
    }
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, format, type, bufferPtr);
    int res2 = AHardwareBuffer_unlock(hardwareBuffer, NULL);
    if (res2 != 0) {
        return -2;
    }
    return glGetError();

}

JNIEXPORT void JNICALL
Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_adjustBitmap(JNIEnv *jenv, jclass thiz,
                                                                       jobject src) {
    unsigned char *srcByteBuffer;
    int result = 0;
    int i, j;
    AndroidBitmapInfo srcInfo;

    result = AndroidBitmap_getInfo(jenv, src, &srcInfo);
    if (result != ANDROID_BITMAP_RESULT_SUCCESS) {
        return;
    }

    result = AndroidBitmap_lockPixels(jenv, src, (void **) &srcByteBuffer);
    if (result != ANDROID_BITMAP_RESULT_SUCCESS) {
        return;
    }

    int width = srcInfo.width;
    int height = srcInfo.height;
    glReadPixels(0, 0, srcInfo.width, srcInfo.height, GL_RGBA, GL_UNSIGNED_BYTE, srcByteBuffer);

    int *pIntBuffer = (int *) srcByteBuffer;

    for (i = 0; i < height / 2; i++) {
        for (j = 0; j < width; j++) {
            int temp = pIntBuffer[(height - i - 1) * width + j];
            pIntBuffer[(height - i - 1) * width + j] = pIntBuffer[i * width + j];
            pIntBuffer[i * width + j] = temp;
        }
    }
    AndroidBitmap_unlockPixels(jenv, src);
}