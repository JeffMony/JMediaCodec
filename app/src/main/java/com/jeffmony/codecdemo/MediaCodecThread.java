package com.jeffmony.codecdemo;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

public class MediaCodecThread extends Thread {

    private static final String TAG = "MediaCodecThread";

    private MediaExtractor mExtractor;
    private MediaCodec mCodec;

    private Surface mSurface;
    private String mVideoPath;

    public MediaCodecThread(Surface surface, String videoPath) {
        mSurface = surface;
        mVideoPath = videoPath;
    }

    @Override
    public void run() {
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(mVideoPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int index = 0; index < mExtractor.getTrackCount(); index++) {
            MediaFormat format = mExtractor.getTrackFormat(index);
            Log.i(TAG, "index="+index+", format="+format);

            String mime = format.getString(MediaFormat.KEY_MIME);

            if(mime.startsWith("video/")) {
                mExtractor.selectTrack(index);

                try {
                    mCodec = MediaCodec.createDecoderByType(mime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mCodec.configure(format, mSurface, null, 0);
                break;
            }
        }

        if (mCodec == null) {
            return;
        }
        mCodec.start();

        ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mCodec.getOutputBuffers();

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        boolean isEOS = false;
        long start = System.currentTimeMillis();

        while(!Thread.interrupted()) {
            if(!isEOS) {
                int inIndex = mCodec.dequeueInputBuffer(10 * 1000); //10ms
                if (inIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    int sampleSize = mExtractor.readSampleData(buffer, 0);

                    if (sampleSize < 0) {
                        mCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    } else {
                        mCodec.queueInputBuffer(inIndex, 0, sampleSize, mExtractor.getSampleTime(), 0);
                        mExtractor.advance();
                    }
                }
            }

            int outIndex = mCodec.dequeueOutputBuffer(bufferInfo, 10 * 1000);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    outputBuffers = mCodec.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    break;
                default:
                    ByteBuffer buffer = outputBuffers[outIndex];

                    while(bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - start) {
                        try {
                            sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    mCodec.releaseOutputBuffer(outIndex, true);
                    break;
            }

            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                break;
            }
        }

        mCodec.stop();
        mCodec.release();
        mExtractor.release();

    }
}
