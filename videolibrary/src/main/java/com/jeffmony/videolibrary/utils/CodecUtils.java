package com.jeffmony.videolibrary.utils;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class CodecUtils {

    private static final String TAG = "CodecUtils";

    public static MediaCodec getAndConfigureCodec(@NonNull MediaFormat mediaFormat,
                                                  @Nullable Surface surface,
                                                  boolean isEncoder) throws Exception {
        MediaCodec mediaCodec = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaCodec = getAndConfigureCodecByConfig(mediaFormat, surface, isEncoder);
            } else {
                mediaCodec = getAndConfigureCodecByType(mediaFormat, surface, isEncoder);
            }
            if (mediaCodec == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    throw new IllegalStateException("Try fallbackToGetCodecByType");
                } else {
                    throw new IllegalStateException("MediaFormat=[" + mediaFormat.toString() +"] codec not found");
                }
            }
            return mediaCodec;
        } catch (Exception e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                try {
                    mediaCodec = getAndConfigureCodecByType(mediaFormat, surface, isEncoder);
                    if (mediaCodec == null) {
                        throw new IllegalStateException("MediaFormat=[" + mediaFormat.toString() +"] codec not found");
                    }
                    return mediaCodec;
                } catch (IOException | IllegalStateException ex) {
                    throw  ex;
                }
            }
            throw e;
        }
    }

    private static MediaCodec getAndConfigureCodecByConfig(@NonNull MediaFormat format,
                                                           @Nullable Surface surface,
                                                           boolean isEncoder) throws Exception {
        MediaCodec codec = null;
        String mimeType = format.getString(MediaFormat.KEY_MIME);
        List<Callable<MediaCodec>> supportedMediaCodecs = findCodecForFormatOrType(isEncoder, mimeType, null);
        if (!supportedMediaCodecs.isEmpty()) {
            codec = createAndConfigureCodec(format, surface, isEncoder, supportedMediaCodecs);
        }
        return codec;
    }

    private static MediaCodec getAndConfigureCodecByType(@NonNull MediaFormat mediaFormat,
                                                         @Nullable Surface surface,
                                                         boolean isEncoder) throws Exception {
        String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
        MediaCodec mediaCodec = null;
        List<Callable<MediaCodec>> supportedMediaCodecs = findCodecForFormatOrType(isEncoder, mimeType, null);
        if (!supportedMediaCodecs.isEmpty()) {
            mediaCodec = createAndConfigureCodec(mediaFormat, surface, isEncoder, supportedMediaCodecs);
        }

        return mediaCodec;
    }

    private static List<Callable<MediaCodec>> findCodecForFormatOrType(boolean encoder,
                                                                       @NonNull String mimeType,
                                                                       @Nullable MediaFormat format) {
        List<Callable<MediaCodec>> supportedMediaCodecs = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
            for (MediaCodecInfo info : codecList.getCodecInfos()) {
                if (info.isEncoder() != encoder) {
                    continue;
                }
                try {
                    MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(mimeType);
                    if (caps != null && (format == null || caps.isFormatSupported(format))) {
                        supportedMediaCodecs.add(() -> MediaCodec.createByCodecName(info.getName()));
                    }
                } catch (Exception e) {
                    LogUtils.w(TAG, "Mimetype=" + mimeType + " is not supported, exception = " + e);
                }
            }
        } else {
            supportedMediaCodecs.add(() -> encoder ? MediaCodec.createEncoderByType(mimeType) : MediaCodec.createDecoderByType(mimeType));
        }
        return supportedMediaCodecs;
    }

    private static MediaCodec createAndConfigureCodec(@NonNull MediaFormat format, @Nullable Surface surface, boolean isEncoder,
                                                      @NonNull List<Callable<MediaCodec>> supportedMediaCodecs) throws Exception {
        MediaCodec codec = null;
        for (Callable<MediaCodec> callable : supportedMediaCodecs) {
            try {
                codec = callable.call();
                if (codec != null) {
                    configureMediaFormat(codec, format, surface, isEncoder);
                    break;
                }
            } catch (Exception e) {
                if (codec != null) {
                    codec.release();
                    codec = null;
                }
                throw e;
            }
        }
        return codec;
    }

    private static void configureMediaFormat(@NonNull MediaCodec mediaCodec,
                                             @NonNull MediaFormat mediaFormat,
                                             @Nullable Surface surface,
                                             boolean isEncoder) throws IllegalStateException {
        mediaCodec.configure(mediaFormat, surface, null, isEncoder ? MediaCodec.CONFIGURE_FLAG_ENCODE : 0);
    }
}
