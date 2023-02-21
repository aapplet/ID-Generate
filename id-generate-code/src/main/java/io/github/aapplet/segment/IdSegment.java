package io.github.aapplet.segment;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class IdSegment {

    private final AtomicLong value = new AtomicLong();
    private volatile long maxId;
    private volatile int step;

    public void setValue(IdSegment idSegment) {
        this.maxId = idSegment.getMaxId();
        this.step = idSegment.getStep();
        this.value.set(maxId - step);
    }

    public long getAndIncrement() {
        return value.getAndIncrement();
    }

}