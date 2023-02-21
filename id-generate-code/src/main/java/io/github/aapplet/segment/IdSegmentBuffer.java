package io.github.aapplet.segment;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IdSegmentBuffer {

    // 业务标识
    private final String key;
    // 号段缓存
    private final IdSegment[] idSegments;
    // 读写锁
    private final ReadWriteLock readWriteLock;
    // 装载线程是否可用
    private final AtomicBoolean loadThreadState;
    // 下一段是否处于可切换状态
    private volatile boolean nextSegmentReady;
    // 最后更新时间戳
    private volatile long lastUpdateTimestamp;
    // 当前号段下标
    private volatile int currentIndex;

    public IdSegmentBuffer(String key) {
        this.key = key;
        this.idSegments = new IdSegment[]{new IdSegment(), new IdSegment()};
        this.readWriteLock = new ReentrantReadWriteLock();
        this.loadThreadState = new AtomicBoolean(true);
        this.nextSegmentReady = false;
        this.lastUpdateTimestamp = 0;
        this.currentIndex = 0;
    }

    // 业务标识
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

    // 下一段下标
    public int nextIndex() {
        return (currentIndex + 1) & 1;
    }

    // 当前号段
    public IdSegment getSegment() {
        return this.idSegments[this.currentIndex];
    }

    // 下一号段
    public IdSegment nextSegment() {
        return this.idSegments[this.nextIndex()];
    }

    // 切换号段
    public void switchSegment() {
        this.nextSegmentReady = false;
        this.currentIndex = this.nextIndex();
    }

    // 下一段是否准备就绪
    public boolean isNextSegmentReady() {
        return nextSegmentReady;
    }

    // 下一号段准备就绪
    public void nextSegmentReady() {
        this.nextSegmentReady = true;
    }

    // 获取最后更新时间戳
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    // 设置最后更新时间戳
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    // 加载线程可用
    public boolean loadThreadAvailable() {
        return loadThreadState.compareAndSet(true, false);
    }

    // 加载线程就绪
    public void loadThreadReady() {
        loadThreadState.set(true);
    }

}