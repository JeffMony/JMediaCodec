/*
 * Copyright 2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").  See License in the project root for
 * license information.
 */
package com.jeffmony.videolibrary.transcoder;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.jeffmony.videolibrary.io.MediaSource;
import com.jeffmony.videolibrary.io.MediaTarget;

import java.nio.ByteBuffer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

/**
 * A transcoder that simply reads data from the source and writes it "as is" to target
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PassthroughTranscoder extends TrackTranscoder {
    private static final String TAG = PassthroughTranscoder.class.getSimpleName();

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024; // default to 1 Mb buffer

    @VisibleForTesting ByteBuffer outputBuffer;
    @VisibleForTesting MediaCodec.BufferInfo outputBufferInfo;

    @VisibleForTesting int lastResult;

    PassthroughTranscoder(@NonNull MediaSource mediaSource,
                          int sourceTrack,
                          @NonNull MediaTarget mediaTarget,
                          int targetTrack) {
        super(mediaSource, sourceTrack, mediaTarget, targetTrack, null, null, null, null);
    }

    @Override
    public void start() throws Exception {
        mMediaSource.selectTrack(mSourceTrack);

        outputBufferInfo = new MediaCodec.BufferInfo();
    }

    @Override
    public void stop() {
        if (outputBuffer != null) {
            outputBuffer.clear();
            outputBuffer = null;
        }
    }

    @Override
    public int processNextFrame() {
        if (lastResult == RESULT_EOS_REACHED) {
            // we are done
            return lastResult;
        }

        // TranscoderJob expects the first result to be RESULT_OUTPUT_MEDIA_FORMAT_CHANGED, so that it can start the mMediaMuxer
        if (!mTargetTrackAdded) {
            mTargetFormat = mMediaSource.getTrackFormat(mSourceTrack);
            if (mDuration > 0) {
                mTargetFormat.setLong(MediaFormat.KEY_DURATION, mDuration);
            }

            mTargetTrack = mMediaMuxer.addTrack(mTargetFormat, mTargetTrack);
            mTargetTrackAdded = true;

            int bufferSize = mTargetFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)
                    ? mTargetFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    : DEFAULT_BUFFER_SIZE;
            outputBuffer = ByteBuffer.allocate(bufferSize);

            lastResult = RESULT_OUTPUT_MEDIA_FORMAT_CHANGED;
            return lastResult;
        }

        int selectedTrack = mMediaSource.getSampleTrackIndex();
        if (selectedTrack != NO_SELECTED_TRACK && selectedTrack != mSourceTrack) {
            lastResult = RESULT_FRAME_PROCESSED;
            return lastResult;
        }

        lastResult = RESULT_FRAME_PROCESSED;

        int bytesRead = mMediaSource.readSampleData(outputBuffer, 0);
        long sampleTime = mMediaSource.getSampleTime();
        int inputFlags = mMediaSource.getSampleFlags();

        if (bytesRead <= 0 || (inputFlags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            outputBuffer.clear();
            mProgress = 1.0f;
            lastResult = RESULT_EOS_REACHED;
            Log.d(TAG, "Reach EoS on input stream");
        } else if (sampleTime >= mSourceMediaSelection.getEnd()) {
            outputBuffer.clear();
            mProgress = 1.0f;
            outputBufferInfo.set(0, 0, sampleTime - mSourceMediaSelection.getStart(), outputBufferInfo.flags | MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            mMediaMuxer.writeSampleData(mTargetTrack, outputBuffer, outputBufferInfo);
            advanceToNextTrack();
            lastResult = RESULT_EOS_REACHED;
            Log.d(TAG, "Reach selection end on input stream");
        } else {
            if (sampleTime >= mSourceMediaSelection.getStart()) {
                int outputFlags = 0;
                if ((inputFlags & MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        outputFlags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    } else {
                        outputFlags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                    }
                }
                sampleTime -= mSourceMediaSelection.getStart();
                if (mDuration > 0) {
                    mProgress = ((float) sampleTime) / mDuration;
                }
                outputBufferInfo.set(0, bytesRead, sampleTime, outputFlags);
                mMediaMuxer.writeSampleData(mTargetTrack, outputBuffer, outputBufferInfo);
            }
            mMediaSource.advance();
        }

        return lastResult;
    }

    @Override
    @NonNull
    public String getEncoderName() {
        return "passthrough";
    }

    @Override
    @NonNull
    public String getDecoderName() {
        return "passthrough";
    }
}
