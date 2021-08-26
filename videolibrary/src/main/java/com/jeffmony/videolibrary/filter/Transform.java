package com.jeffmony.videolibrary.filter;

import android.graphics.PointF;

import androidx.annotation.NonNull;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class Transform {
    @NonNull
    public final PointF mSize;
    @NonNull public final PointF mPosition;
    public final float mRotation;

    /**
     * Create a geometric transform
     * @param size size in X and Y direction, relative to target video frame
     * @param position position of source video frame  center, in relative coordinate in 0 - 1 range
     *                 in fourth quadrant (0,0 is top left corner)
     * @param rotation rotation angle of overlay, relative to target video frame, counter-clockwise, in degrees
     **/
    public Transform(@NonNull PointF size, @NonNull PointF position, float rotation) {
        mSize = size;
        mPosition = position;
        mRotation = rotation;
    }
}
