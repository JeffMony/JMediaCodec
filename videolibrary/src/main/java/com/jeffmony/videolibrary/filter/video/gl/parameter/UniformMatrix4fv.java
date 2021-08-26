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

import java.nio.FloatBuffer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

/**
 * 4x4 float value matrix shader parameter
 */
public class UniformMatrix4fv extends ShaderParameter {

    private int count;
    private boolean transpose;
    private float[] matrix;
    private int offset;
    private FloatBuffer buffer;

    /**
     * Create shader parameter
     * @param name parameter name, as defined in shader code
     * @param count number of matrices
     * @param transpose flag indicating if matrix is transposed
     * @param matrix matrix values
     * @param offset matrix offset
     */
    public UniformMatrix4fv(@NonNull String name, int count, boolean transpose, @NonNull float[] matrix, int offset) {
        super(TYPE_UNIFORM, name);

        this.count = count;
        this.transpose = transpose;
        this.matrix = matrix;
        this.offset = offset;
    }

    /**
     * Create shader parameter
     * @param name parameter name, as defined in shader code
     * @param count number of matrices
     * @param transpose flag indicating if matrix is transposed
     * @param buffer buffer containing matrix values
     */
    public UniformMatrix4fv(@NonNull String name, int count, boolean transpose, @NonNull FloatBuffer buffer) {
        super(TYPE_UNIFORM, name);

        this.count = count;
        this.transpose = transpose;
        this.buffer = buffer;
    }

    @Override
    public void apply(int glProgram) {
        if (buffer != null) {
            GLES20.glUniformMatrix4fv(getLocation(glProgram), count, transpose, buffer);
        } else {
            GLES20.glUniformMatrix4fv(getLocation(glProgram), count, transpose, matrix, offset);
        }
    }
}
