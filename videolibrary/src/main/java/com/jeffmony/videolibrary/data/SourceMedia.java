package com.jeffmony.videolibrary.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class SourceMedia {
    public String mPath;
    public long mSize;
    public float mDuration;

    public List<MediaTrackFormat> mTracks = new ArrayList<>();
}
