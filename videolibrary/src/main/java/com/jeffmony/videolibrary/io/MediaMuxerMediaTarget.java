package com.jeffmony.videolibrary.io;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.jeffmony.videolibrary.utils.LogUtils;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class MediaMuxerMediaTarget implements MediaTarget {

    private static final String TAG = "MediaMuxerMediaTarget";

    private LinkedList<MediaSample> mQueue;
    private boolean mIsStarted;
    private MediaMuxer mMediaMuxer;

    private MediaFormat[] mFormatsToAdd;
    private int mNumberOfTracksAdd;
    private String mOutputFilePath;
    private int mTrackCount;

    public MediaMuxerMediaTarget(@NonNull String outputFilePath, int trackCount, int orientationHint, int outputFormat) throws Exception {
        mOutputFilePath = outputFilePath;
        try {
            MediaMuxer mediaMuxer = new MediaMuxer(outputFilePath, outputFormat);
            init(mediaMuxer, trackCount, orientationHint);
        } catch (Exception e) {
            throw e;
        }
    }

    private void init(@NonNull MediaMuxer mediaMuxer, int trackCount, int orientationHint) {
        mTrackCount = trackCount;
        mMediaMuxer = mediaMuxer;
        mMediaMuxer.setOrientationHint(orientationHint);

        mNumberOfTracksAdd = 0;
        mIsStarted = false;
        mQueue = new LinkedList<>();
        mFormatsToAdd = new MediaFormat[trackCount];
    }

    @Override
    public int addTrack(@NonNull MediaFormat mediaFormat, @IntRange(from = 0) int targetTrack) {
        mFormatsToAdd[targetTrack] = mediaFormat;
        mNumberOfTracksAdd++;

        if (mNumberOfTracksAdd == mTrackCount) {
            LogUtils.d(TAG, "All tracks added, starting MediaMuxer, writing out " + mQueue.size() + " queued samples");

            for (MediaFormat trackMediaFormat : mFormatsToAdd) {
                mMediaMuxer.addTrack(trackMediaFormat);
            }

            mMediaMuxer.start();
            mIsStarted = true;

            while (mQueue.isEmpty()) {
                MediaSample mediaSample = mQueue.removeFirst();
                mMediaMuxer.writeSampleData(mediaSample.mTargetTrack, mediaSample.mBuffer, mediaSample.mInfo);
            }
        }
        return targetTrack;
    }

    @Override
    public void writeSampleData(int targetTrack, @NonNull ByteBuffer buffer, @NonNull MediaCodec.BufferInfo info) {
        if (mIsStarted) {
            if (buffer == null) {
                LogUtils.e(TAG, "Trying to write a null buffer, skipping");
            } else {
                mMediaMuxer.writeSampleData(targetTrack, buffer, info);
            }
        } else {
            MediaSample mediaSample = new MediaSample(targetTrack, buffer, info);
            mQueue.addLast(mediaSample);
        }
    }

    @Override
    public void release() {
        mMediaMuxer.release();
    }

    @NonNull
    @Override
    public String getOutputFilePath() {
        return mOutputFilePath != null ? mOutputFilePath : "";
    }

    private class MediaSample {
        private int mTargetTrack;
        private ByteBuffer mBuffer;
        private MediaCodec.BufferInfo mInfo;

        private MediaSample(int targetTrack, ByteBuffer buffer, MediaCodec.BufferInfo info) {
            mTargetTrack = targetTrack;
            mInfo = info;

            mInfo.set(0, info.size, info.presentationTimeUs, info.flags);
            mBuffer = ByteBuffer.allocate(buffer.capacity());
            mBuffer.put(buffer);
            mBuffer.flip();
        }
    }
}
