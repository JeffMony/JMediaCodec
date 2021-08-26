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

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

/**
 * One float value shader parameter
 */
public class Uniform1f extends ShaderParameter {

    private float mValue;

    /**
     * Create shader parameter
     * @param name parameter name, as defined in shader code
     * @param value parameter value
     */
    public Uniform1f(@NonNull String name, float value) {
        super(TYPE_UNIFORM, name);
        mValue = value;
    }

    @Override
    public void apply(int glProgram) {
        GLES20.glUniform1f(getLocation(glProgram), mValue);
    }
}
