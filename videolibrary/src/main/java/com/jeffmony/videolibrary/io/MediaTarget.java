package com.jeffmony.videolibrary.io;

import android.media.MediaCodec;
import android.media.MediaFormat;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public interface MediaTarget {
    /**
     * Adds a track with the specified format.
     * @param mediaFormat The media format for the track. This must not be an empty MediaFormat.
     * @param targetTrack target track index
     * @return index of a newly added track
     */
    int addTrack(@NonNull MediaFormat mediaFormat, @IntRange(from = 0) int targetTrack);

    /**
     * Writes an encoded sample into the muxer.
     * @param targetTrack target track index
     * @param buffer encoded data
     * @param info metadata
     */
    void writeSampleData(int targetTrack, @NonNull ByteBuffer buffer, @NonNull MediaCodec.BufferInfo info);

    /**
     * Release all resources. Make sure to call this when MediaTarget is no longer needed
     */
    void release();

    /**
     * Get output file path
     * @return output file path
     */
    @NonNull
    String getOutputFilePath();
}
