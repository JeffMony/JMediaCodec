package com.jeffmony.videolibrary.codec;

import android.media.MediaFormat;
import android.view.Surface;

import androidx.annotation.NonNull;

/**
 * @author : jeffli
 * @Email  : jeffmony@163.com
 * @Date   : 2021-08-26
 */

public interface Encoder {
    /**
     * 初始化编码器
     * @param format 编码的信息
     */
    void init(@NonNull MediaFormat format) throws Exception;

    /**
     * 编码输入的surface
     * @return
     */
    Surface createInputSurface();

    /**
     * 启动编码器
     */
    void start() throws Exception;

    /**
     * 编码器是否在工作
     * @return
     */
    boolean isRunning();

    /**
     * 通知最后一帧发出去了
     */
    void signalEndOfInputStream();

    /**
     * 从编码器队列中取数据，如果不为负，返回数据帧，否则不返回
     * @param timeout
     * @return
     */
    int dequeueInputFrame(long timeout);

    /**
     * 获取需要编码的原始数据
     * @param tag
     * @return
     */
    Frame getInputFrame(int tag);

    /**
     * 将需要编码的原始数据入队
     * @param frame
     */
    void queueInputFrame(Frame frame);

    /**
     * 出队解码之后的数据，如果返回不为负，则有数据，否则没有数据
     * @param timeout
     * @return
     */
    int dequeueOutputFrame(long timeout);

    /**
     * 返回解码之后的数据
     * @param tag
     * @return
     */
    Frame getOutputFrame(int tag);

    /**
     * 释放编码的ouput frame
     * @param tag
     */
    void releaseOutputFrame(int tag);

    /**
     * 返回输出的格式
     * @return
     */
    MediaFormat getOutputFormat();

    /**
     * 停止编码
     */
    void stop();

    /**
     * 释放编码资源
     */
    void release();

    /**
     * 获取编码名称
     * @return
     */
    String getName();
}
