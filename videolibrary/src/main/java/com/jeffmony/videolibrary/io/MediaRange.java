package com.jeffmony.videolibrary.io;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public class MediaRange {

    private final long start;
    private final long end;

    /**
     * Create an instance of MediaRange
     * @param start range start, in microseconds
     * @param end range end, in microseconds, greater than start
     */
    public MediaRange(long start, long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Get range start, in microseconds
     */
    public long getStart() {
        return start;
    }

    /**
     * Get range end, in microseconds
     */
    public long getEnd() {
        return end;
    }
}

