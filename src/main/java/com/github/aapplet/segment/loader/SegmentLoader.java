package com.github.aapplet.segment.loader;

public interface SegmentLoader {

    /**
     * init segment
     *
     * @param tag business name
     * @return new segment
     */
    SegmentData initSegment(String tag);

    /**
     * get new segment
     *
     * @param tag business name
     * @return new segment
     */
    SegmentData getSegment(String tag);

    /**
     * update step increment
     *
     * @param tag business name
     */
    void updateStepIncrement(String tag);

    /**
     * update step decrement
     *
     * @param tag business name
     */
    void updateStepDecrement(String tag);

}
