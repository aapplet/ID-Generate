package com.github.aapplet.segment.loader;

import org.apache.ibatis.annotations.*;

public interface SegmentMapper {

    /**
     * get new segment
     *
     * @param tag business tag
     * @return new segment
     */
    @Results(value = {
            @Result(column = "step", property = "step"),
            @Result(column = "max_id", property = "maxId"),
    })
    @Select("SELECT step,max_id FROM sys_segment WHERE tag = #{tag}")
    SegmentData getSegment(String tag);

    /**
     * init segment
     *
     * @param tag business tag
     */
    @Insert("INSERT IGNORE INTO sys_segment(tag) VALUE(#{tag})")
    void insertSegment(String tag);

    /**
     * update segment max_id
     *
     * @param tag business tag
     */
    @Update("UPDATE sys_segment SET max_id = max_id + step WHERE tag = #{tag}")
    void updateSegment(String tag);

    /**
     * update step increment
     *
     * @param tag business tag
     */
    @Update("UPDATE sys_segment SET step = step * 2 WHERE tag = #{tag}")
    void updateStepIncrement(String tag);

    /**
     * update step decrement
     *
     * @param tag business tag
     */
    @Update("UPDATE sys_segment SET step = step / 2 WHERE tag = #{tag}")
    void updateStepDecrement(String tag);

}
