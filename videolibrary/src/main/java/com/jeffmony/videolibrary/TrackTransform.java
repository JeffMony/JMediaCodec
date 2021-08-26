package com.jeffmony.videolibrary;

import android.media.MediaFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeffmony.videolibrary.codec.Decoder;
import com.jeffmony.videolibrary.codec.Encoder;
import com.jeffmony.videolibrary.io.MediaSource;
import com.jeffmony.videolibrary.io.MediaTarget;
import com.jeffmony.videolibrary.render.Renderer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-27
 */

public class TrackTransform {
    private final MediaSource mMediaSource;
    private final Decoder mDecoder;
    private final Renderer mRenderer;
    private final Encoder mEncoder;
    private final MediaTarget mMediaTarget;
    private final MediaFormat mTargetFormat;
    private final int mSourceTrack;
    private final int mTargetTrack;

    private TrackTransform(@NonNull MediaSource mediaSource,
                           @Nullable Decoder decoder,
                           @Nullable Renderer renderer,
                           @Nullable Encoder encoder,
                           @NonNull MediaTarget mediaTarget,
                           @Nullable MediaFormat targetFormat,
                           int sourceTrack, int targetTrack) {
        mMediaSource = mediaSource;
        mDecoder = decoder;
        mRenderer = renderer;
        mEncoder = encoder;
        mMediaTarget = mediaTarget;
        mTargetFormat = targetFormat;
        mSourceTrack = sourceTrack;
        mTargetTrack = targetTrack;
    }

    public MediaSource getMediaSource() {
        return mMediaSource;
    }

    public Decoder getDecoder() {
        return mDecoder;
    }

    public Renderer getRenderer() {
        return mRenderer;
    }

    public Encoder getEncoder() {
        return mEncoder;
    }

    public MediaTarget getMediaTarget() {
        return mMediaTarget;
    }

    public MediaFormat getTargetFormat() {
        return mTargetFormat;
    }

    public int getSourceTrack() {
        return mSourceTrack;
    }

    public int getTargetTrack() {
        return mTargetTrack;
    }

    public static class Builder {
        private final MediaSource mediaSource;
        private final int sourceTrack;
        private final MediaTarget mediaTarget;

        private Decoder decoder;
        private Renderer renderer;
        private Encoder encoder;
        private MediaFormat targetFormat;
        private int targetTrack;

        public Builder(@NonNull MediaSource mediaSource, int sourceTrack, @NonNull MediaTarget mediaTarget) {
            this.mediaSource = mediaSource;
            this.sourceTrack = sourceTrack;
            this.mediaTarget = mediaTarget;
            this.targetTrack = sourceTrack;
        }

        public Builder setDecoder(Decoder decoder) {
            this.decoder = decoder;
            return this;
        }

        public Builder setRenderer(Renderer renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder setEncoder(Encoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public Builder setTargetFormat(MediaFormat targetFormat) {
            this.targetFormat = targetFormat;
            return this;
        }

        public Builder setTargetTrack(int targetTrack) {
            this.targetTrack = targetTrack;
            return this;
        }

        public TrackTransform build() {
            return new TrackTransform(mediaSource, decoder,
                    renderer, encoder, mediaTarget, targetFormat,
                    sourceTrack, targetTrack);
        }
    }
}
