package io.github.aapplet.segment;

public interface IdSegmentLoader {

    /**
     * initialize segment
     *
     * @param key service key
     * @return new segment
     */
    IdSegment initSegment(String key);

    /**
     * get new segment
     *
     * @param key service key
     * @return new segment
     */
    IdSegment getSegment(String key);

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