package com.jeffmony.videolibrary.codec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.jeffmony.videolibrary.utils.CodecUtils;

import java.nio.ByteBuffer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class MediaCodecEncoder implements Encoder {

    private MediaCodec mMediaCodec;

    private boolean mIsReleased = true;
    private boolean mIsRunning;

    private final MediaCodec.BufferInfo mEncoderOutputBufferInfo = new MediaCodec.BufferInfo();

    @Override
    public void init(@NonNull MediaFormat format) throws Exception {
        if (!format.containsKey(MediaFormat.KEY_COLOR_FORMAT)) {
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        }

        mMediaCodec = CodecUtils.getAndConfigureCodec(format, null, true);
        mIsReleased = (mMediaCodec == null);
    }

    @Override
    public Surface createInputSurface() {
        return mMediaCodec.createInputSurface();
    }

    @Override
    public void start() throws Exception {
        startEncoder();
    }

    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public int dequeueInputFrame(long timeout) {
        return mMediaCodec.dequeueInputBuffer(timeout);
    }

    @Override
    public Frame getInputFrame(int tag) {
        if (tag >= 0) {
            ByteBuffer inputBuffer;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                inputBuffer = mMediaCodec.getInputBuffer(tag);
            } else {
                ByteBuffer[] encoderInputBuffers = mMediaCodec.getInputBuffers();
                inputBuffer = encoderInputBuffers[tag];
            }
            return new Frame(tag, inputBuffer, null);
        }
        return null;
    }

    @Override
    public void queueInputFrame(Frame frame) {
        mMediaCodec.queueInputBuffer(frame.mTag,
                frame.mBufferInfo.offset,
                frame.mBufferInfo.size,
                frame.mBufferInfo.presentationTimeUs,
                frame.mBufferInfo.flags);
    }

    @Override
    public void signalEndOfInputStream() {
        mMediaCodec.signalEndOfInputStream();
    }

    @Override
    public int dequeueOutputFrame(long timeout) {
        return mMediaCodec.dequeueOutputBuffer(mEncoderOutputBufferInfo, timeout);
    }

    @Override
    public Frame getOutputFrame(int tag) {
        if (tag >= 0) {
            ByteBuffer buffer;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                buffer = mMediaCodec.getOutputBuffer(tag);
            } else {
                ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
                buffer = encoderOutputBuffers[tag];
            }
            return new Frame(tag, buffer, mEncoderOutputBufferInfo);
        }
        return null;
    }

    @Override
    public void releaseOutputFrame(int tag) {
        mMediaCodec.releaseOutputBuffer(tag, false);
    }

    @Override
    public MediaFormat getOutputFormat() {
        return mMediaCodec.getOutputFormat();
    }

    @Override
    public void stop() {
        if (mIsRunning) {
            mMediaCodec.stop();
            mIsRunning = false;
        }
    }

    @Override
    public void release() {
        if (!mIsReleased) {
            mMediaCodec.release();
            mIsReleased = true;
        }
    }

    @Override
    public String getName() {
        return mMediaCodec.getName();
    }

    private void startEncoder() {
        if (!mIsRunning) {
            mMediaCodec.start();
            mIsRunning = true;
        }
    }
}
