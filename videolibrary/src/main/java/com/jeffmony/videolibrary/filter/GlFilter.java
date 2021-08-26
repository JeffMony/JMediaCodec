package com.jeffmony.videolibrary.filter;

import androidx.annotation.NonNull;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public interface GlFilter {

    /**
     * Initialize filter. This will be called during renderer initialization on a thread with GL context,
     * after GL surface has been created and set current.
     * Filter is expected to do all its GL initialization (compiling shaders, loading textures, etc.) in this method.
     */
    void init();

    /**
     * Set VP (projection + view) matrix on a filter. VP matrix is created and configured by a renderer
     * and pass copies of it are passed into to filters using this method, so they can use it to
     * configure their own MVP matrices. Model matrix not present. View matrix is configured to have its camera angle match video's.
     * Projection matrix is configured to orthogonal projection to account for video frame's aspect ratio: (-aspectRatio, aspectRatio, -1, 1, -1, 1)
     * Filters can set up their model matrix and then multiply it by renderer's VP matrix to match their geometry to video frame's.
     *
     * @param vpMatrix VP (projection * view) matrix configured by the renderer, usually
     * @param vpMatrixOffset offset into VP matrix
     */
    void setVpMatrix(@NonNull float[] vpMatrix, int vpMatrixOffset);

    /**
     * Apply GL rendering to a frame
     * @param presentationTimeNs presentation time of a frame, in nanoseconds
     */
    void apply(long presentationTimeNs);

    /**
     * Release all GL resources, such as shaders, textures, etc.
     */
    void release();
}
