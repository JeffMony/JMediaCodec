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
 * Four integer value shader parameter
 */
public class Uniform4i extends ShaderParameter {

    private int mValue1;
    private int mValue2;
    private int mValue3;
    private int mValue4;

    /**
     * Create shader parameter
     * @param name parameter name, as defined in shader code
     * @param value1 first parameter value
     * @param value2 second parameter value
     * @param value3 third parameter value
     * @param value4 fourth parameter value
     */
    public Uniform4i(@NonNull String name, int value1, int value2, int value3, int value4) {
        super(TYPE_UNIFORM, name);

        mValue1 = value1;
        mValue2 = value2;
        mValue3 = value3;
        mValue4 = value4;
    }

    @Override
    public void apply(int glProgram) {
        GLES20.glUniform4i(getLocation(glProgram), mValue1, mValue2, mValue3, mValue4);
    }
}
