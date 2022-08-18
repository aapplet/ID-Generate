package io.github.aapplet.segment;

import org.apache.ibatis.annotations.*;

public interface SegmentMapper {

    /**
     * get new segment
     *
     * @param key business tag
     * @return new segment
     */
    @Results(value = {
            @Result(column = "step", property = "step"),
            @Result(column = "max_id", property = "maxId"),
    })
    @Select("SELECT step,max_id FROM sys_segment WHERE tag = #{key}")
    SegmentModel getSegment(String key);

    /**
     * init segment
     *
     * @param key business tag
     */
    @Insert("INSERT IGNORE INTO sys_segment(tag) VALUE(#{key})")
    void insertSegment(String key);

    /**
     * update segment max_id
     *
     * @param key business tag
     */
    @Update("UPDATE sys_segment SET max_id = max_id + step WHERE tag = #{key}")
    void updateSegment(String key);

    /**
     * update step increment
     *
     * @param key business tag
     */
    @Update("UPDATE sys_segment SET step = step * 2 WHERE tag = #{key}")
    void updateStepIncrement(String key);

    /**
     * update step decrement
     *
     * @param key business tag
     */
    @Update("UPDATE sys_segment SET step = step / 2 WHERE tag = #{key}")
    void updateStepDecrement(String key);

}
