package io.github.aapplet.segment.loader;

/**
 * database loader
 */
public interface SegmentLoader {

    /**
     * initialize segment
     *
     * @param key service key
     * @return new segment
     */
    SegmentData initSegment(String key);

    /**
     * get new segment
     *
     * @param key service key
     * @return new segment
     */
    SegmentData getSegment(String key);

    /**
     * step increment
     *
     * @param key service key
     */
    void updateStepIncrement(String key);

    /**
     * step decrement
     *
     * @param key service key
     */
    void updateStepDecrement(String key);

}
