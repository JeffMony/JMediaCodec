package com.jeffmony.videolibrary.data;

import androidx.annotation.NonNull;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class MediaTrackFormat {
    public int mIndex;
    public String mMimeType;

    public MediaTrackFormat(int index, @NonNull String mimeType) {
        mIndex = index;
        mMimeType = mimeType;
    }

    public MediaTrackFormat(@NonNull MediaTrackFormat format) {
        mIndex = format.mIndex;
        mMimeType = format.mMimeType;
    }
}
