package com.jeffmony.videolibrary.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeffmony.videolibrary.utils.CodecUtils;

import java.nio.ByteBuffer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class MediaCodecDecoder implements Decoder {

    private MediaCodec mMediaCodec;

    private boolean mIsRunning;
    private boolean mIsReleased;
    private final MediaCodec.BufferInfo mOutputBufferInfo = new MediaCodec.BufferInfo();

    @Override
    public void init(@NonNull MediaFormat format, @Nullable Surface surface) throws Exception {
        mMediaCodec = CodecUtils.getAndConfigureCodec(format, surface, false);
        mIsReleased = (mMediaCodec == null);
    }

    @Override
    public void start() {
        startDecoder();
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
                ByteBuffer[] decoderInputBuffers = mMediaCodec.getInputBuffers();
                inputBuffer = decoderInputBuffers[tag];
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
    public int dequeueOutputFrame(long timeout) {
        return mMediaCodec.dequeueOutputBuffer(mOutputBufferInfo, timeout);
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
            return new Frame(tag, buffer, mOutputBufferInfo);
        }

        return null;
    }

    @Override
    public void releaseOutputFrame(int tag, boolean render) {
        mMediaCodec.releaseOutputBuffer(tag, render);
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
            mIsReleased = false;
        }
    }

    @Override
    public String getName() {
        return mMediaCodec.getName();
    }

    private void startDecoder() {
        mMediaCodec.start();
        mIsRunning = true;
    }

}
