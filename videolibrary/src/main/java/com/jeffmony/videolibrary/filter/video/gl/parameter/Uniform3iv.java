/*
 * Copyright 2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").  See License in the project root for
 * license information.
 */
package com.jeffmony.videolibrary.filter.video.gl.parameter;

import android.opengl.GLES20;

import androidx.annotation.NonNull;

import java.nio.IntBuffer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

/**
 * Three integer element vector shader parameter
 */
public class Uniform3iv extends ShaderParameter {

    private int mCount;
    private IntBuffer mBuffer;

    /**
     * Create shader parameter
     * @param name parameter name, as defined in shader code
     * @param count number of vectors
     * @param buffer buffer containing new vector values
     */
    public Uniform3iv(@NonNull String name, int count, @NonNull int[] buffer) {
        this(name, count, IntBuffer.wrap(buffer));
    }

    /**
     * Create shader parameter
     * @param name parameter name, as defined in shader code
     * @param count number of vectors
     * @param buffer buffer containing new vector values
     */
    public Uniform3iv(@NonNull String name, int count, @NonNull IntBuffer buffer) {
        super(TYPE_UNIFORM, name);

        mCount = count;
        mBuffer = buffer;
    }

    @Override
    public void apply(int glProgram) {
        GLES20.glUniform3iv(getLocation(glProgram), mCount, mBuffer);
    }
}
