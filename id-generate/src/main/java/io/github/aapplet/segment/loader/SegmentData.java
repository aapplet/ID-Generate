package io.github.aapplet.segment.loader;

/**
 * database carrier
 */
public interface SegmentData {

    /**
     * segment step
     *
     * @return segment step
     */
    long getStep();

    /**
     * segment maximum id
     *
     * @return segment maximum id
     */
    long getMaxId();

}
