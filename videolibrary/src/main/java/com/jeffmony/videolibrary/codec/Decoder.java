package com.jeffmony.videolibrary.codec;

import android.media.MediaFormat;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public interface Decoder {
    /**
     * 初始化解码器
     * @param format
     * @param surface
     * @throws Exception
     */
    void init(@NonNull MediaFormat format, @Nullable Surface surface) throws Exception;

    /**
     * 启动解码器
     */
    void start();

    /**
     * 检测解码器是否正在工作
     * @return
     */
    boolean isRunning();

    /**
     * 从队列中取出需要解码的原始数据
     * @param timeout
     * @return
     */
    int dequeueInputFrame(long timeout);

    /**
     * 获取需要解码的帧
     * @param tag
     * @return
     */
    Frame getInputFrame(int tag);

    /**
     * 将需要解码的数据帧入队
     * @param frame
     */
    void queueInputFrame(Frame frame);

    /**
     * 将解码之后的数据出队
     * @param timeout
     * @return
     */
    int dequeueOutputFrame(long timeout);

    /**
     * 获取解码之后的数据帧
     * @param tag
     * @param render 是否需要被渲染在surface上
     * @return
     */
    Frame getOutputFrame(int tag, boolean render);

    /**
     * 释放解码之后的数据帧
     * @param tag
     * @param render
     */
    void releaseOutputFrame(int tag, boolean render);

    /**
     * 返回输出的格式
     * @return
     */
    MediaFormat getOutputFormat();

    void stop();

    void release();

    String getName();
}
