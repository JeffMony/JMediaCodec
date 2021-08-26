package com.jeffmony.videolibrary.data;

import androidx.annotation.NonNull;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class VideoTrackFormat extends MediaTrackFormat {
    public int mWidth;
    public int mHeight;
    public int mBitrate;
    public int mFrameRate;
    public int mKeyFrameInterval;
    public long mDuration;
    public int mRotation;

    public VideoTrackFormat(int index, @NonNull String mimeType) {
        super(index, mimeType);
    }

    public VideoTrackFormat(@NonNull VideoTrackFormat format) {
        super(format);
        mWidth = format.mWidth;
        mHeight = format.mHeight;
        mBitrate = format.mBitrate;
        mFrameRate = format.mFrameRate;
        mKeyFrameInterval = format.mKeyFrameInterval;
        mDuration = format.mDuration;
        mRotation = format.mRotation;
    }
}
