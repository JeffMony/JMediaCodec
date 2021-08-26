package com.jeffmony.videolibrary.filter;

import androidx.annotation.NonNull;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public interface GlFrameRenderFilter extends GlFilter {

    /**
     * Initialize texture associated with {@link android.graphics.SurfaceTexture} if input video frames
     * @param textureHandle texture handle of input video texture
     * @param transformMatrix transform matrix of input video texture
     */
    void initInputFrameTexture(int textureHandle, @NonNull float[] transformMatrix);
}
