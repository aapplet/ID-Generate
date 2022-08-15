package com.github.aapplet.segment;

import com.github.aapplet.segment.loader.SegmentData;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class IdSegment {

    private final int step;
    private final long maxId;
    private final AtomicLong value;

    public IdSegment(SegmentData segmentData) {
        this.step = segmentData.getStep();
        this.maxId = segmentData.getMaxId();
        this.value = new AtomicLong(maxId - step);
    }

    public long incrementAndGet() {
        return this.value.incrementAndGet();
    }

}