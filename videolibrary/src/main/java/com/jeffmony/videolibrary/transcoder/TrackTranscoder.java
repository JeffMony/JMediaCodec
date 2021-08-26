package com.jeffmony.videolibrary.transcoder;

import android.media.MediaCodec;
import android.media.MediaFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeffmony.videolibrary.codec.Decoder;
import com.jeffmony.videolibrary.codec.Encoder;
import com.jeffmony.videolibrary.io.MediaRange;
import com.jeffmony.videolibrary.io.MediaSource;
import com.jeffmony.videolibrary.io.MediaTarget;
import com.jeffmony.videolibrary.render.Renderer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public abstract class TrackTranscoder {
    public static final int NO_SELECTED_TRACK = -1;
    public static final int UNDEFINED_VALUE = -1;

    public static final int ERROR_TRANSCODER_NOT_RUNNING = -3;

    public static final int RESULT_OUTPUT_MEDIA_FORMAT_CHANGED = 1;
    public static final int RESULT_FRAME_PROCESSED = 2;
    public static final int RESULT_EOS_REACHED = 3;

    @NonNull
    protected final MediaSource mMediaSource;
    @NonNull protected final MediaTarget mMediaMuxer;
    @Nullable protected final Renderer mRenderer;
    @Nullable protected final Decoder mDecoder;
    @Nullable protected final Encoder mEncoder;
    @NonNull protected final MediaRange mSourceMediaSelection;

    protected int mSourceTrack;
    protected int mTargetTrack;

    protected boolean mTargetTrackAdded;

    @Nullable protected MediaFormat mTargetFormat;

    protected long mDuration = UNDEFINED_VALUE;
    protected float mProgress;

    TrackTranscoder(@NonNull MediaSource mediaSource,
                    int sourceTrack,
                    @NonNull MediaTarget mediaTarget,
                    int targetTrack,
                    @Nullable MediaFormat targetFormat,
                    @Nullable Renderer renderer,
                    @Nullable Decoder decoder,
                    @Nullable Encoder encoder) {
        mMediaSource = mediaSource;
        mSourceTrack = sourceTrack;
        mTargetTrack = targetTrack;
        mMediaMuxer = mediaTarget;
        mTargetFormat = targetFormat;
        mRenderer = renderer;
        mDecoder = decoder;
        mEncoder = encoder;
        mSourceMediaSelection = mediaSource.getSelection();

        MediaFormat sourceMedia = mediaSource.getTrackFormat(sourceTrack);
        if (sourceMedia.containsKey(MediaFormat.KEY_DURATION)) {
            mDuration = sourceMedia.getLong(MediaFormat.KEY_DURATION);
            if (targetFormat != null) {
                targetFormat.setLong(MediaFormat.KEY_DURATION, mDuration);
            }
        }


        if (mSourceMediaSelection.getEnd() < mSourceMediaSelection.getStart()) {
            throw new IllegalArgumentException("Range end should be greater than range start");
        }

        // adjust for range
        mDuration = Math.min(mDuration, mSourceMediaSelection.getEnd());
        mDuration -= mSourceMediaSelection.getStart();
    }

    public abstract void start() throws Exception;

    public abstract int processNextFrame() throws Exception;

    public abstract void stop();

    public int getSourceTrack() {
        return mSourceTrack;
    }

    public int getTargetTrack() {
        return mTargetTrack;
    }

    public float getProgress() {
        return mProgress;
    }

    @NonNull
    public String getEncoderName() {
        return mEncoder.getName();
    }

    @NonNull
    public String getDecoderName() {
        return mDecoder.getName();
    }

    @NonNull
    public MediaFormat getTargetMediaFormat() {
        return mTargetFormat;
    }

    protected void advanceToNextTrack() {
        // done with this track, advance until track switches to let other track transcoders finish work
        while (mMediaSource.getSampleTrackIndex() == mSourceTrack) {
            mMediaSource.advance();
            if ((mMediaSource.getSampleFlags() & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                // reached the end of container, no more tracks left
                return;
            }
        }
    }

}
