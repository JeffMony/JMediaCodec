package com.jeffmony.videolibrary.filter.video.gl;

import android.graphics.PointF;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeffmony.videolibrary.filter.GlFrameRenderFilter;
import com.jeffmony.videolibrary.filter.Transform;
import com.jeffmony.videolibrary.filter.util.GlFilterUtil;
import com.jeffmony.videolibrary.filter.video.gl.parameter.ShaderParameter;
import com.jeffmony.videolibrary.render.GlRenderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

/**
 * Implementation of GlFrameRenderFilter, which renders source video frame onto target video frame,
 * optionally applying pixel and geometric transformation.
 */
public class VideoFrameRenderFilter implements GlFrameRenderFilter {

    protected static final String DEFAULT_VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +

                    "void main()\n" +
                    "{\n" +
                    "gl_Position = uMVPMatrix * aPosition;\n" +
                    "vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}";

    protected static final String DEFAULT_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +

                    "void main()\n" +
                    "{\n" +
                    "gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    private final String mVertexShader;
    private final String mFragmentShader;
    private final ShaderParameter[] mShaderParameters;
    private final Transform mTransform;

    private float[] mMvpMatrix = new float[16];
    private float[] mInputFrameTextureMatrix = new float[16];
    private int mMvpMatrixOffset;

    private FloatBuffer mTriangleVertices;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f, 1.0f, 0, 0.f, 1.f,
            1.0f, 1.0f, 0, 1.f, 1.f,
    };

    private int mVertexShaderHandle;
    private int mFragmentShaderHandle;
    private int mGlProgram;
    private int mvpMatrixHandle;
    private int uStMatrixHandle;
    private int inputFrameTextureHandle;
    private int aPositionHandle;
    private int aTextureHandle;

    /**
     * Create filter which scales source frame to fit target frame, apply vertex and frame shaders. Can be used for
     * things like pixel modification.
     * @param vertexShader vertex shader
     * @param fragmentShader fragment shader
     * @param shaderParameters shader parameters (uniforms and/or attributes) if any, null otherwise
     */
    protected VideoFrameRenderFilter(@NonNull String vertexShader,
                                     @NonNull String fragmentShader,
                                     @Nullable ShaderParameter[] shaderParameters) {
        this(vertexShader, fragmentShader, shaderParameters, null);
    }

    /**
     * Create frame render filter with source video frame, then scale, then position and then rotate the bitmap around its center as specified.
     * Use provided vertex and fragment filter, to do things like pixel modification.
     * @param vertexShader vertex shader
     * @param fragmentShader fragment shader
     * @param shaderParameters shader parameters (uniforms and/or attributes) if any, null otherwise
     * @param transform {@link Transform} that defines positioning of source video frame within target video frame
     */
    protected VideoFrameRenderFilter(@NonNull String vertexShader,
                                     @NonNull String fragmentShader,
                                     @Nullable ShaderParameter[] shaderParameters,
                                     @Nullable Transform transform) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
        mShaderParameters = shaderParameters;
        mTransform = transform != null
                ? transform
                : new Transform(new PointF(1f, 1f), new PointF(0.5f, 0.5f), 0);

        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
    }

    @Override
    public void init() {
        Matrix.setIdentityM(mInputFrameTextureMatrix, 0);

        mVertexShaderHandle = GlRenderUtils.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShader);
        if (mVertexShaderHandle == 0) {
            throw new RuntimeException("failed loading vertex shader");
        }
        mFragmentShaderHandle = GlRenderUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShader);
        if (mFragmentShaderHandle == 0) {
            release();
            throw new RuntimeException("failed loading fragment shader");
        }
        mGlProgram = GlRenderUtils.createProgram(mVertexShaderHandle, mFragmentShaderHandle);
        if (mGlProgram == 0) {
            release();
            throw new RuntimeException("failed creating glProgram");
        }
        aPositionHandle = GLES20.glGetAttribLocation(mGlProgram, "aPosition");
        GlRenderUtils.checkGlError("glGetAttribLocation aPosition");
        if (aPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        aTextureHandle = GLES20.glGetAttribLocation(mGlProgram, "aTextureCoord");
        GlRenderUtils.checkGlError("glGetAttribLocation aTextureCoord");
        if (aTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }
        mvpMatrixHandle = GLES20.glGetUniformLocation(mGlProgram, "uMVPMatrix");
        GlRenderUtils.checkGlError("glGetUniformLocation uMVPMatrix");
        if (mvpMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        uStMatrixHandle = GLES20.glGetUniformLocation(mGlProgram, "uSTMatrix");
        GlRenderUtils.checkGlError("glGetUniformLocation uSTMatrix");
        if (uStMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }
    }

    @Override
    public void setVpMatrix(@NonNull float[] vpMatrix, int vpMatrixOffset) {
        mMvpMatrix = GlFilterUtil.createFilterMvpMatrix(vpMatrix, mTransform);
        mMvpMatrixOffset = vpMatrixOffset;
    }

    @Override
    public void initInputFrameTexture(int textureHandle, @NonNull float[] transformMatrix) {
        inputFrameTextureHandle = textureHandle;
        mInputFrameTextureMatrix = transformMatrix;
    }

    @Override
    public void apply(long presentationTimeNs) {
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GlRenderUtils.checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GlRenderUtils.checkGlError("glEnableVertexAttribArray aPositionHandle");
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(aTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GlRenderUtils.checkGlError("glVertexAttribPointer aTextureHandle");
        GLES20.glEnableVertexAttribArray(aTextureHandle);
        GlRenderUtils.checkGlError("glEnableVertexAttribArray aTextureHandle");

        GlRenderUtils.checkGlError("onDrawFrame start");
        GLES20.glUseProgram(mGlProgram);
        GlRenderUtils.checkGlError("glUseProgram");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inputFrameTextureHandle);

        if (mShaderParameters != null) {
            for (ShaderParameter shaderParameter : mShaderParameters) {
                shaderParameter.apply(mGlProgram);
            }
        }

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mMvpMatrix, mMvpMatrixOffset);
        GLES20.glUniformMatrix4fv(uStMatrixHandle, 1, false, mInputFrameTextureMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GlRenderUtils.checkGlError("glDrawArrays");
    }

    @Override
    public void release() {
        GLES20.glDeleteProgram(mGlProgram);
        GLES20.glDeleteShader(mVertexShaderHandle);
        GLES20.glDeleteShader(mFragmentShaderHandle);
        GLES20.glDeleteBuffers(1, new int[]{aTextureHandle}, 0);
        mGlProgram = 0;
        mVertexShaderHandle = 0;
        mFragmentShaderHandle = 0;
        aTextureHandle = 0;
    }
}

