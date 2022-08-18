package io.github.aapplet.segment.core;

import io.github.aapplet.segment.loader.SegmentData;

import java.util.concurrent.atomic.AtomicLong;

public final class IdSegment {

    private final long step;
    private final long maxId;
    private final AtomicLong value;

    // 初始化号段
    public IdSegment(SegmentData segmentData) {
        this.step = segmentData.getStep();
        this.maxId = segmentData.getMaxId();
        this.value = new AtomicLong(maxId - step);
    }

    // 获取步长
    public long getStep() {
        return step;
    }

    // 获取最大ID
    public long getMaxId() {
        return maxId;
    }

    // 递增并获取ID
    public long incrementAndGet() {
        return value.incrementAndGet();
    }

}