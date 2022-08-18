package io.github.aapplet.segment.core;

import io.github.aapplet.segment.loader.SegmentData;

/**
 * Segment Chain
 */
public final class IdSegmentChain {

    // 当前号段
    private final IdSegment idSegment;

    // 下一号段链
    private IdSegmentChain nextSegmentChain;

    // 初始化号段链
    public IdSegmentChain(SegmentData segmentData) {
        this.idSegment = new IdSegment(segmentData);
    }

    // 获取当前号段
    public IdSegment getIdSegment() {
        return idSegment;
    }

    // 获取下一号段链
    public IdSegmentChain getNextSegmentChain() {
        return nextSegmentChain;
    }

    // 设置下一号段链
    public void setNextSegmentChain(SegmentData segmentData) {
        this.nextSegmentChain = new IdSegmentChain(segmentData);
    }

}