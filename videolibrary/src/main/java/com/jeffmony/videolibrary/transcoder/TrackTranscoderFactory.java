/*
 * Copyright 2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").  See License in the project root for
 * license information.
 */
package com.jeffmony.videolibrary.transcoder;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeffmony.videolibrary.codec.Decoder;
import com.jeffmony.videolibrary.codec.Encoder;
import com.jeffmony.videolibrary.io.MediaSource;
import com.jeffmony.videolibrary.io.MediaTarget;
import com.jeffmony.videolibrary.render.PassthroughSoftwareRenderer;
import com.jeffmony.videolibrary.render.Renderer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class TrackTranscoderFactory {
    private static final String TAG = TrackTranscoderFactory.class.getSimpleName();

    /**
     * Create a proper transcoder for a given source track and target media format.
     *
     * @param sourceTrack  source track id
     * @param mediaSource  {@link MediaExtractor} for reading data from the source
     * @param mediaTarget  {@link MediaTarget} for writing data to the target
     * @param targetFormat {@link MediaFormat} with target video track parameters, null if writing "as is"
     * @return implementation of {@link TrackTranscoder} for a given track
     */
    @NonNull
    public TrackTranscoder create(int sourceTrack,
                                  int targetTrack,
                                  @NonNull MediaSource mediaSource,
                                  @Nullable Decoder decoder,
                                  @Nullable Renderer renderer,
                                  @Nullable Encoder encoder,
                                  @NonNull MediaTarget mediaTarget,
                                  @Nullable MediaFormat targetFormat) throws Exception {
        if (targetFormat == null) {
            return new PassthroughTranscoder(mediaSource, sourceTrack, mediaTarget, targetTrack);
        }

        String trackMimeType = targetFormat.getString(MediaFormat.KEY_MIME);
        if (trackMimeType == null) {
            throw new Exception("SOURCE_TRACK_MIME_TYPE_NOT_FOUND");
        }

        if (trackMimeType.startsWith("video") || trackMimeType.startsWith("audio")) {
            if (decoder == null) {
                throw new Exception("Error.DECODER_NOT_PROVIDED");
            } else if (encoder == null) {
                throw new Exception("Error.DECODER_NOT_PROVIDED");
            }
        }

        if (trackMimeType.startsWith("video")) {
            if (renderer == null) {
                throw new Exception("Error.DECODER_NOT_PROVIDED");
            }
            return new VideoTrackTranscoder(mediaSource,
                                            sourceTrack,
                                            mediaTarget,
                                            targetTrack,
                                            targetFormat,
                                            renderer,
                                            decoder,
                                            encoder);
        } else if (trackMimeType.startsWith("audio")) {
            Renderer audioRenderer = renderer == null
                    ? new PassthroughSoftwareRenderer(encoder)
                    : renderer;

            return new AudioTrackTranscoder(mediaSource,
                                            sourceTrack,
                                            mediaTarget,
                                            targetTrack,
                                            targetFormat,
                                            audioRenderer,
                                            decoder,
                                            encoder);
        } else {
            Log.i(TAG, "Unsupported track mime type: " + trackMimeType + ", will use passthrough transcoder");
            return new PassthroughTranscoder(mediaSource, sourceTrack, mediaTarget, targetTrack);
        }
    }
}
