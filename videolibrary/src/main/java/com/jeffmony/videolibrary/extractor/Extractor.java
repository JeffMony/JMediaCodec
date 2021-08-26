package com.jeffmony.videolibrary.extractor;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.jeffmony.videolibrary.data.AudioTrackFormat;
import com.jeffmony.videolibrary.data.GenericTrackFormat;
import com.jeffmony.videolibrary.data.SourceMedia;
import com.jeffmony.videolibrary.data.VideoTrackFormat;
import com.jeffmony.videolibrary.utils.FileUtils;
import com.jeffmony.videolibrary.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class Extractor {

    private static final String TAG = "Extractor";
    private static volatile Extractor sInstance = null;

    private static final String KEY_ROTATION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            ? MediaFormat.KEY_ROTATION : "rotation-degrees";

    public static Extractor getInstance() {
        if (sInstance == null) {
            synchronized (Extractor.class) {
                if (sInstance == null) {
                    sInstance = new Extractor();
                }
            }
        }
        return sInstance;
    }

    public void updateSourceMedia(@NonNull SourceMedia sourceMedia) {
        if (TextUtils.isEmpty(sourceMedia.mPath)) {
            sourceMedia = null;
            return;
        }
        File sourceFile = new File(sourceMedia.mPath);
        if (!sourceFile.exists()) {
            sourceMedia = null;
            return;
        }

        if (!sourceFile.isFile()) {
            sourceFile = null;
            return;
        }

        long size = FileUtils.getSize(sourceFile);
        if (size == 0) {
            sourceMedia = null;
            return;
        }

        sourceMedia.mDuration = getMediaDuration(sourceMedia.mPath) / 1000f;

        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(sourceMedia.mPath);
            sourceMedia.mTracks = new ArrayList<>(mediaExtractor.getTrackCount());

            for (int track = 0; track < mediaExtractor.getTrackCount(); track++) {
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(track);
                String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType == null) {
                    continue;
                }

                if (mimeType.startsWith("video")) {
                    VideoTrackFormat videoTrack = new VideoTrackFormat(track, mimeType);
                    videoTrack.mWidth = getInt(mediaFormat, MediaFormat.KEY_WIDTH);
                    videoTrack.mHeight = getInt(mediaFormat, MediaFormat.KEY_HEIGHT);
                    videoTrack.mDuration = getLong(mediaFormat, MediaFormat.KEY_DURATION);
                    videoTrack.mFrameRate = getInt(mediaFormat, MediaFormat.KEY_FRAME_RATE);
                    videoTrack.mKeyFrameInterval = getInt(mediaFormat, MediaFormat.KEY_I_FRAME_INTERVAL);
                    videoTrack.mRotation = getInt(mediaFormat, KEY_ROTATION, 0);
                    videoTrack.mBitrate = getInt(mediaFormat, MediaFormat.KEY_BIT_RATE);
                    sourceMedia.mTracks.add(videoTrack);
                } else if (mimeType.startsWith("audio")) {
                    AudioTrackFormat audioTrack = new AudioTrackFormat(track, mimeType);
                    audioTrack.mChannelCount = getInt(mediaFormat, MediaFormat.KEY_CHANNEL_COUNT);
                    audioTrack.mSampleRate = getInt(mediaFormat, MediaFormat.KEY_SAMPLE_RATE);
                    audioTrack.mDuration = getLong(mediaFormat, MediaFormat.KEY_DURATION);
                    audioTrack.mBitrate = getInt(mediaFormat, MediaFormat.KEY_BIT_RATE);
                    sourceMedia.mTracks.add(audioTrack);
                } else {
                    sourceMedia.mTracks.add(new GenericTrackFormat(track, mimeType));
                }

            }

        } catch (Exception e) {
            LogUtils.w(TAG, "Failed to extractor sourceMedia : " + e);
        }
    }

    private int getInt(@NonNull MediaFormat mediaFormat, @NonNull String key) {
        return getInt(mediaFormat, key, -1);
    }

    private int getInt(@NonNull MediaFormat mediaFormat, @NonNull String key, int defaultValue) {
        if (mediaFormat.containsKey(key)) {
            return mediaFormat.getInteger(key);
        }
        return defaultValue;
    }

    private long getLong(@NonNull MediaFormat mediaFormat, @NonNull String key) {
        if (mediaFormat.containsKey(key)) {
            return mediaFormat.getLong(key);
        }
        return -1;
    }

    private long getMediaDuration(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        mediaMetadataRetriever.release();
        return Long.parseLong(durationStr);
    }
}
