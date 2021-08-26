/*
 * Copyright 2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").  See License in the project root for
 * license information.
 */
package com.jeffmony.videolibrary.transcoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.jeffmony.videolibrary.codec.Decoder;
import com.jeffmony.videolibrary.codec.Encoder;
import com.jeffmony.videolibrary.codec.Frame;
import com.jeffmony.videolibrary.io.MediaSource;
import com.jeffmony.videolibrary.io.MediaTarget;
import com.jeffmony.videolibrary.render.Renderer;

import java.util.concurrent.TimeUnit;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class AudioTrackTranscoder extends TrackTranscoder {
    private static final String TAG = AudioTrackTranscoder.class.getSimpleName();

    @VisibleForTesting int lastExtractFrameResult;
    @VisibleForTesting int lastDecodeFrameResult;
    @VisibleForTesting int lastEncodeFrameResult;

    @NonNull private MediaFormat sourceAudioFormat;

    AudioTrackTranscoder(@NonNull MediaSource mediaSource,
                         int sourceTrack,
                         @NonNull MediaTarget mediaTarget,
                         int targetTrack,
                         @NonNull MediaFormat targetFormat,
                         @NonNull Renderer renderer,
                         @NonNull Decoder decoder,
                         @NonNull Encoder encoder) throws Exception {
        super(mediaSource, sourceTrack, mediaTarget, targetTrack, targetFormat, renderer, decoder, encoder);

        lastExtractFrameResult = RESULT_FRAME_PROCESSED;
        lastDecodeFrameResult = RESULT_FRAME_PROCESSED;
        lastEncodeFrameResult = RESULT_FRAME_PROCESSED;

        initCodecs();
    }

    private void initCodecs() throws Exception {
        // create anc configure the encoder

        sourceAudioFormat = mMediaSource.getTrackFormat(mSourceTrack);

        mEncoder.init(mTargetFormat);
        mRenderer.init(null, sourceAudioFormat, mTargetFormat);
        mDecoder.init(sourceAudioFormat, null);
    }

    @Override
    public void start() throws Exception {
        mMediaSource.selectTrack(mSourceTrack);

        mEncoder.start();
        mDecoder.start();
    }

    @Override
    public int processNextFrame() throws Exception {
        if (!mEncoder.isRunning() || !mDecoder.isRunning()) {
            // can't do any work
            return ERROR_TRANSCODER_NOT_RUNNING;
        }
        int result = RESULT_FRAME_PROCESSED;

        // extract the frame from the incoming stream and send it to the decoder
        if (lastExtractFrameResult != RESULT_EOS_REACHED) {
            lastExtractFrameResult = extractAndEnqueueInputFrame();
        }

        // receive the decoded frame and send it to the encoder
        if (lastDecodeFrameResult != RESULT_EOS_REACHED) {
            lastDecodeFrameResult = queueDecodedInputFrame();
        }

        // get the encoded frame and write it into the target file
        if (lastEncodeFrameResult != RESULT_EOS_REACHED) {
            lastEncodeFrameResult = writeEncodedOutputFrame();
        }

        if (lastEncodeFrameResult == RESULT_OUTPUT_MEDIA_FORMAT_CHANGED) {
            result = RESULT_OUTPUT_MEDIA_FORMAT_CHANGED;
        }

        if (lastExtractFrameResult == RESULT_EOS_REACHED
            && lastDecodeFrameResult == RESULT_EOS_REACHED
            && lastEncodeFrameResult == RESULT_EOS_REACHED) {
            result = RESULT_EOS_REACHED;
        }

        return result;
    }

    @Override
    public void stop() {
        mEncoder.stop();
        mEncoder.release();

        mDecoder.stop();
        mDecoder.release();
    }

    private int extractAndEnqueueInputFrame() throws Exception {
        int extractFrameResult = RESULT_FRAME_PROCESSED;

        int selectedTrack = mMediaSource.getSampleTrackIndex();
        if (selectedTrack == mSourceTrack || selectedTrack == NO_SELECTED_TRACK) {
            int tag = mDecoder.dequeueInputFrame(0);
            if (tag >= 0) {
                Frame frame = mDecoder.getInputFrame(tag);
                if (frame == null) {
                    throw new Exception("NO_FRAME_AVAILABLE");
                }
                int bytesRead = mMediaSource.readSampleData(frame.mBuffer, 0);
                long sampleTime = mMediaSource.getSampleTime();
                int sampleFlags = mMediaSource.getSampleFlags();
                if (bytesRead <= 0 || (sampleFlags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    frame.mBufferInfo.set(0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    mDecoder.queueInputFrame(frame);
                    extractFrameResult = RESULT_EOS_REACHED;
                    Log.d(TAG, "EoS reached on the input stream");
                } else if (sampleTime >= mSourceMediaSelection.getEnd()) {
                    frame.mBufferInfo.set(0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    mDecoder.queueInputFrame(frame);
                    advanceToNextTrack();
                    extractFrameResult = RESULT_EOS_REACHED;
                    Log.d(TAG, "Selection end reached on the input stream");
                } else {
                    frame.mBufferInfo.set(0, bytesRead, sampleTime, sampleFlags);
                    mDecoder.queueInputFrame(frame);
                    mMediaSource.advance();
                    //Log.d(TAG, "Sample time: " + sampleTime + ", source bytes read: " + bytesRead);
                }
            } else {
                switch (tag) {
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        //Log.d(TAG, "Will try getting decoder input buffer later");
                        break;
                    default:
                        Log.e(TAG, "Unhandled value " + tag + " when decoding an input frame");
                        break;
                }
            }
        }

        return extractFrameResult;
    }

    private int queueDecodedInputFrame() throws Exception {
        int decodeFrameResult = RESULT_FRAME_PROCESSED;

        int tag = mDecoder.dequeueOutputFrame(0);
        if (tag >= 0) {
            Frame decoderOutputFrame = mDecoder.getOutputFrame(tag);
            if (decoderOutputFrame == null) {
                throw new Exception("NO_FRAME_AVAILABLE");
            }

            if (decoderOutputFrame.mBufferInfo.presentationTimeUs >= mSourceMediaSelection.getStart()
                    || (decoderOutputFrame.mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                mRenderer.renderFrame(decoderOutputFrame,
                        TimeUnit.MICROSECONDS.toNanos(decoderOutputFrame.mBufferInfo.presentationTimeUs - mSourceMediaSelection.getStart()));
            }
            mDecoder.releaseOutputFrame(tag, false);

            if ((decoderOutputFrame.mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "EoS on decoder output stream");
                decodeFrameResult = RESULT_EOS_REACHED;
            }
        } else {
            switch (tag) {
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    // Log.d(TAG, "Will try getting decoder output later");
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    sourceAudioFormat = mDecoder.getOutputFormat();
                    mRenderer.onMediaFormatChanged(sourceAudioFormat, mTargetFormat);
                    Log.d(TAG, "Decoder output format changed: " + sourceAudioFormat);
                    break;
                default:
                    Log.e(TAG, "Unhandled value " + tag + " when receiving decoded input frame");
                    break;
            }
        }

        return decodeFrameResult;
    }

    private int writeEncodedOutputFrame() throws Exception {
        int encodeFrameResult = RESULT_FRAME_PROCESSED;

        int tag = mEncoder.dequeueOutputFrame(0);
        if (tag >= 0) {
            Frame frame = mEncoder.getOutputFrame(tag);
            if (frame == null) {
                throw new Exception("NO_FRAME_AVAILABLE");
            }

            if ((frame.mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "Encoder produced EoS, we are done");
                mProgress = 1.0f;
                encodeFrameResult = RESULT_EOS_REACHED;
            } else if (frame.mBufferInfo.size > 0
                    && (frame.mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                mMediaMuxer.writeSampleData(mTargetTrack, frame.mBuffer, frame.mBufferInfo);
                if (mDuration > 0) {
                    mProgress = ((float) frame.mBufferInfo.presentationTimeUs) / mDuration;
                }
            }

            mEncoder.releaseOutputFrame(tag);
        } else {
            switch (tag) {
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    //                        Log.d(TAG, "Will try getting encoder output buffer later");
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    // TODO for now, we assume that we only get one media format as a first buffer
                    MediaFormat outputMediaFormat = mEncoder.getOutputFormat();
                    if (!mTargetTrackAdded) {
                        mTargetFormat = outputMediaFormat;
                        mTargetTrack = mMediaMuxer.addTrack(outputMediaFormat, mTargetTrack);
                        mTargetTrackAdded = true;
                        mRenderer.onMediaFormatChanged(sourceAudioFormat, mTargetFormat);
                    }
                    encodeFrameResult = RESULT_OUTPUT_MEDIA_FORMAT_CHANGED;
                    Log.d(TAG, "Encoder output format received " + outputMediaFormat);
                    break;
                default:
                    Log.e(TAG, "Unhandled value " + tag + " when receiving encoded output frame");
                    break;
            }
        }

        return encodeFrameResult;
    }
}
