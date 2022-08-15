package com.github.aapplet.segment;

import lombok.Data;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Data
public class IdSegmentBuffer {

    // 标识
    private final String key;
    // 读写锁
    private final ReadWriteLock readWriteLock;
    // 线程状态
    private final AtomicBoolean loadThreadState;
    // 号段链
    private volatile IdSegmentChain segmentChain;
    // 最后更时间戳
    private volatile long lastUpdateTimestamp;

    // 初始化号段状态
    public IdSegmentBuffer(String key, IdSegmentChain segmentChain) {
        this.key = key;
        this.segmentChain = segmentChain;
        this.readWriteLock = new ReentrantReadWriteLock();
        this.loadThreadState = new AtomicBoolean(true);
    }

    // 当前号段
    public IdSegment currentSegment() {
        return segmentChain.getIdSegment();
    }

    // 下一链准备就绪
    public boolean nextChainIsReady() {
        return segmentChain.getNextChain() != null;
    }

    // 切换下一号段
    public void switchNextSegment() {
        this.setSegmentChain(segmentChain.getNextChain());
    }

}