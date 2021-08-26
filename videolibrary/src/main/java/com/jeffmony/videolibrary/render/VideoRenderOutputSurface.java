/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// from: https://android.googlesource.com/platform/cts/+/lollipop-release/tests/tests/media/src/android/media/cts/InputSurface.java
// blob: 157ed88d143229e4edb6889daf18fb73aa2fc5a5
// modified: removed unused methods
package com.jeffmony.videolibrary.render;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import androidx.annotation.NonNull;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

/**
 * Holds state associated with a Surface used for MediaCodec encoder input.
 * <p>
 * The constructor takes a Surface obtained from MediaCodec.createInputSurface(), and uses that
 * to create an EGL window surface.  Calls to eglSwapBuffers() cause a frame of data to be sent
 * to the video encoder.
 */
class VideoRenderOutputSurface {

    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    private EGLDisplay mEglDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEglContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEglSurface = EGL14.EGL_NO_SURFACE;

    private Surface mSurface;

    VideoRenderOutputSurface(@NonNull Surface surface) {
        mSurface = surface;
        eglSetup();
        makeCurrent();
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     */
    boolean swapBuffers() {
        return EGL14.eglSwapBuffers(mEglDisplay, mEglSurface);
    }

    /**
     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
     */
    void setPresentationTime(long nanoseconds) {
        EGLExt.eglPresentationTimeANDROID(mEglDisplay, mEglSurface, nanoseconds);
    }

    /**
     * Discard all resources held by this class, notably the EGL context.
     */
    public void release() {
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
            EGL14.eglDestroyContext(mEglDisplay, mEglContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(mEglDisplay);

            mEglDisplay = EGL14.EGL_NO_DISPLAY;
            mEglContext = EGL14.EGL_NO_CONTEXT;
            mEglSurface = EGL14.EGL_NO_SURFACE;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
    }

    private void eglSetup() {
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            mEglDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }

        // Configure EGL for recordable and OpenGL ES 2.0.  We want enough RGB bits
        // to minimize artifacts from possible YUV conversion.
        int[] egl14ConfigAttributes = {
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEglDisplay,
                                   egl14ConfigAttributes, 0,
                                   configs, 0,
                                   configs.length, numConfigs, 0)) {
            throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
        }

        // Configure context for OpenGL ES 2.0.
        int[] egl14ContextAttributes = {
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        };
        mEglContext = EGL14.eglCreateContext(mEglDisplay,
                                            configs[0],
                                            EGL14.EGL_NO_CONTEXT,
                                            egl14ContextAttributes,
                                            0);
        checkEglError("eglCreateContext");
        if (mEglContext == null) {
            throw new RuntimeException("null context");
        }

        // Create a window surface, and attach it to the Surface we received.
        int[] surfaceAttribs = {
            EGL14.EGL_NONE
        };
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay,
                configs[0], mSurface, surfaceAttribs, 0);
        checkEglError("eglCreateWindowSurface");
        if (mEglSurface == null) {
            throw new RuntimeException("surface was null");
        }
    }

    /**
     * Makes our EGL context and surface current.
     */
    private void makeCurrent() {
        if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    private void checkEglError(@NonNull String message) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(message + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }
}
