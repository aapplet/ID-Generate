package io.github.aapplet.segment.core;

import io.github.aapplet.segment.loader.SegmentData;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class IdSegmentBuffer {

    // 业务标识
    private final String key;
    // 读写锁
    private final ReadWriteLock readWriteLock;
    // 装载线程状态
    private final AtomicBoolean loadThreadState;
    // 最后更新时间戳
    private long lastUpdateTimestamp;
    // 号段链
    private IdSegmentChain segmentChain;

    // 初始化
    public IdSegmentBuffer(String key, SegmentData segmentData) {
        this.key = key;
        this.readWriteLock = new ReentrantReadWriteLock();
        this.loadThreadState = new AtomicBoolean(true);
        this.segmentChain = new IdSegmentChain(segmentData);
    }

    // 获取标识
    public String getKey() {
        return key;
    }

    // 获取读锁
    public Lock getReadLock() {
        return readWriteLock.readLock();
    }

    // 获取写锁
    public Lock getWriteLock() {
        return readWriteLock.writeLock();
    }

    // 加载线程可用
    public boolean loadThreadAvailable() {
        return loadThreadState.compareAndSet(true, false);
    }

    // 加载线程就绪
    public void loadThreadReady() {
        loadThreadState.set(true);
    }

    // 获取最后更新时间戳
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    // 设置最后更新时间戳
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    // 当前号段
    public IdSegment currentSegment() {
        return segmentChain.getIdSegment();
    }

    // 下一段状态
    public boolean nextSegmentReady() {
        return segmentChain.getNextSegmentChain() != null;
    }

    // 装载下一段
    public void loadNextSegment(SegmentData segmentData) {
        this.segmentChain.setNextSegmentChain(segmentData);
    }

    // 切换下一段
    public void switchNextSegment() {
        this.segmentChain = segmentChain.getNextSegmentChain();
    }

}