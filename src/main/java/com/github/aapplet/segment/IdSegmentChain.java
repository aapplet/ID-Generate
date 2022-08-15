package com.github.aapplet.segment;

import com.github.aapplet.segment.loader.SegmentData;
import lombok.Data;

@Data
public class IdSegmentChain {

    // 当前号段
    private final IdSegment idSegment;
    // 下一号段
    private volatile IdSegmentChain nextChain;

    // 初始化
    public IdSegmentChain(SegmentData segmentData) {
        this.idSegment = new IdSegment(segmentData);
    }

}