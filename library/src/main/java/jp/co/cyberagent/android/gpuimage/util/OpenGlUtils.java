/*
 * Copyright (C) 2018 CyberAgent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage.util;

import static android.opengl.GLES30.GL_RGB;
import static android.opengl.GLES30.GL_RGBA;
import static android.opengl.GLES30.GL_UNSIGNED_BYTE;
import static android.opengl.GLES30.glTexSubImage2D;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.hardware.Camera.Size;
import android.hardware.HardwareBuffer;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.os.Build;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;

public class OpenGlUtils {
    public static final int NO_TEXTURE = -1;

    public static int loadTexture(final Bitmap img, final int usedTexId) {
        return loadTexture(img, usedTexId, true);
    }

    public static int loadTexture(final Bitmap img, final int usedTexId, final boolean recycle) {
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES30.glGenTextures(1, textures, 0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && img.getConfig() == Config.HARDWARE) {
                final int internalFormat;
                final int format;
                final int bufferFormat = img.getHardwareBuffer().getFormat();
                if (bufferFormat == HardwareBuffer.RGB_888) {
                    internalFormat = GL_RGB;
                    format = GL_RGB;
                } else if (bufferFormat == HardwareBuffer.RGBA_8888) {
                    internalFormat = GL_RGBA;
                    format = GL_RGBA;
                } else {
                    throw new IllegalStateException("Hardware buffers in just RGB_888 and RGBA_8888 supported");
                }
                // TODO (work w/ float bitmaps)
                final int type = GL_UNSIGNED_BYTE;
                final int result = GPUImageNativeLibrary.drawHardwareBufferToTexture(img.getWidth(), img.getHeight(), format, internalFormat, type, img.getHardwareBuffer());
            } else {
                GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, img, 0);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && img.getConfig() == Config.HARDWARE) {
                // TODO (work w/ float bitmaps)
                final int type = GL_UNSIGNED_BYTE;
                final int format;
                final int bufferFormat = img.getHardwareBuffer().getFormat();
                if (bufferFormat == HardwareBuffer.RGB_888) {
                    format = GL_RGB;
                } else if (bufferFormat == HardwareBuffer.RGBA_8888) {
                    format = GL_RGBA;
                } else {
                    throw new IllegalStateException("Hardware buffers in just RGB_888 and RGBA_8888 supported");
                }
                GPUImageNativeLibrary.drawHardwareBufferToTextureWithId(usedTexId, format, type, img.getWidth(), img.getHeight(), img.getHardwareBuffer());
            } else {
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId);
                GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, img);
            }
            textures[0] = usedTexId;
        }
        if (recycle) {
            img.recycle();
        }
        return textures[0];
    }

    public static int loadTexture(final IntBuffer data, final int width, final int height, final int usedTexId) {
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES30.glGenTextures(1, textures, 0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height,
                    0, GLES30.GL_RGBA, GL_UNSIGNED_BYTE, data);
        } else {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId);
            glTexSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, width,
                    height, GLES30.GL_RGBA, GL_UNSIGNED_BYTE, data);
            textures[0] = usedTexId;
        }
        return textures[0];
    }

    public static int loadTextureAsBitmap(final IntBuffer data, final Size size, final int usedTexId) {
        Bitmap bitmap = Bitmap
                .createBitmap(data.array(), size.width, size.height, Config.ARGB_8888);
        return loadTexture(bitmap, usedTexId);
    }

    public static int loadShader(final String strSource, final int iType) {
        int[] compiled = new int[1];
        int iShader = GLES30.glCreateShader(iType);
        GLES30.glShaderSource(iShader, strSource);
        GLES30.glCompileShader(iShader);
        GLES30.glGetShaderiv(iShader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", "Compilation\n" + GLES30.glGetShaderInfoLog(iShader));
            return 0;
        }
        return iShader;
    }

    public static int loadProgram(final String strVSource, final String strFSource) {
        int iVShader;
        int iFShader;
        int iProgId;
        int[] link = new int[1];
        iVShader = loadShader(strVSource, GLES30.GL_VERTEX_SHADER);
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed");
            return 0;
        }
        iFShader = loadShader(strFSource, GLES30.GL_FRAGMENT_SHADER);
        if (iFShader == 0) {
            Log.d("Load Program", "Fragment Shader Failed");
            return 0;
        }

        iProgId = GLES30.glCreateProgram();

        GLES30.glAttachShader(iProgId, iVShader);
        GLES30.glAttachShader(iProgId, iFShader);

        GLES30.glLinkProgram(iProgId);

        GLES30.glGetProgramiv(iProgId, GLES30.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed");
            return 0;
        }
        GLES30.glDeleteShader(iVShader);
        GLES30.glDeleteShader(iFShader);
        return iProgId;
    }

    public static float rnd(final float min, final float max) {
        float fRandNum = (float) Math.random();
        return min + (max - min) * fRandNum;
    }
}
