package com.jeffmony.videolibrary.data;

import androidx.annotation.NonNull;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class AudioTrackFormat extends MediaTrackFormat {

    public int mChannelCount;
    public int mSampleRate;
    public int mBitrate;
    public long mDuration;

    public AudioTrackFormat(int index, @NonNull String mimeType) {
        super(index, mimeType);
    }

    public AudioTrackFormat(@NonNull AudioTrackFormat format) {
        super(format);
        mChannelCount = format.mChannelCount;
        mSampleRate = format.mSampleRate;
        mBitrate = format.mBitrate;
        mDuration = format.mDuration;
    }
}
