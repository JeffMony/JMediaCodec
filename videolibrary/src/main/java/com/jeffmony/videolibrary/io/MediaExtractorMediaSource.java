package com.jeffmony.videolibrary.io;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;

import com.jeffmony.videolibrary.utils.FileUtils;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class MediaExtractorMediaSource implements MediaSource {

    private final MediaExtractor mExtractor;
    private final MediaRange mRange;

    private int mOrientationHint;
    private long mSize;

    public MediaExtractorMediaSource(@NonNull String path) throws Exception {
        this(path, new MediaRange(0, Long.MAX_VALUE));
    }

    public MediaExtractorMediaSource(@NonNull String path, @NonNull MediaRange range) throws Exception {
        mRange = range;
        mExtractor = new MediaExtractor();
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

        try {
            mExtractor.setDataSource(path);
        } catch (Exception e) {
            throw e;
        }
        mediaMetadataRetriever.setDataSource(path);
        String rotation = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (rotation != null) {
            mOrientationHint = Integer.parseInt(rotation);
        }
        mSize = FileUtils.getSize(new File(path));
        mediaMetadataRetriever.release();
    }

    @Override
    public int getOrientationHint() {
        return mOrientationHint;
    }

    @Override
    public int getTrackCount() {
        return mExtractor.getTrackCount();
    }

    @NonNull
    @Override
    public MediaFormat getTrackFormat(int track) {
        return mExtractor.getTrackFormat(track);
    }

    @Override
    public void selectTrack(int track) {
        mExtractor.selectTrack(track);
    }

    @Override
    public void seekTo(long position, int mode) {
        mExtractor.seekTo(position, mode);
    }

    @Override
    public int getSampleTrackIndex() {
        return mExtractor.getSampleTrackIndex();
    }

    @Override
    public int readSampleData(@NonNull ByteBuffer buffer, int offset) {
        return mExtractor.readSampleData(buffer, offset);
    }

    @Override
    public long getSampleTime() {
        return mExtractor.getSampleTime();
    }

    @Override
    public int getSampleFlags() {
        return mExtractor.getSampleFlags();
    }

    @Override
    public void advance() {
        mExtractor.advance();
    }

    @Override
    public void release() {
        mExtractor.release();
    }

    @Override
    public long getSize() {
        return mSize;
    }

    @NonNull
    @Override
    public MediaRange getSelection() {
        return mRange;
    }
}
