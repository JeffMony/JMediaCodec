package com.jeffmony.videolibrary;

import com.jeffmony.videolibrary.transcoder.TrackTranscoder;
import com.jeffmony.videolibrary.transcoder.TrackTranscoderFactory;

import java.util.List;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-27
 */

public class TransformationJob {

    private static final String TAG = "TransformationJob";

    List<TrackTranscoder> mTrackTranscoder;
    float mLastProgress;

    TrackTranscoderFactory mTrackTranscoderFactory;

    private final List<TrackTransform> mTrackTransforms;

    TransformationJob(List<TrackTransform> trackTransforms) {
        mTrackTransforms = trackTransforms;
    }
}
