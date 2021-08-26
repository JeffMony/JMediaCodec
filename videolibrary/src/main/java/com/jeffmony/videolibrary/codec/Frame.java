package com.jeffmony.videolibrary.codec;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class Frame {
    public final int mTag;
    public final ByteBuffer mBuffer;
    public final MediaCodec.BufferInfo mBufferInfo;

    public Frame(int tag, ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        mTag = tag;
        mBuffer = buffer;
        if (bufferInfo == null) {
            mBufferInfo = new MediaCodec.BufferInfo();
        } else {
            mBufferInfo = bufferInfo;
        }
    }
}
