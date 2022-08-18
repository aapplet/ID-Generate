package io.github.aapplet.segment;

import io.github.aapplet.segment.loader.SegmentData;

public class SegmentModel implements SegmentData {

    private long step;

    private long maxId;

    public void setStep(long step) {
        this.step = step;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long getMaxId() {
        return maxId;
    }

}
