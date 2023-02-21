package io.github.aapplet.segment;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class IdSegmentManager implements IdSegmentLoader {

    private final JdbcTemplate jdbcTemplate;

    public IdSegmentManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * initialize segment
     *
     * @param key service key
     * @return new segment
     */
    @Override
    public IdSegment initSegment(String key) {
        jdbcTemplate.update("INSERT IGNORE INTO sys_segment(`biz_tag`) VALUE(?)", key);
        return this.getSegment(key);
    }

    /**
     * get new segment
     *
     * @param key service key
     * @return new segment
     */
    @Override
    public IdSegment getSegment(String key) {
        final String UPDATE_SQL = "UPDATE sys_segment SET max_id = max_id + step WHERE biz_tag = ?";
        final String SELECT_SQL = "SELECT max_id, step FROM sys_segment WHERE biz_tag = ?";
        jdbcTemplate.update(UPDATE_SQL, key);
        return jdbcTemplate.queryForObject(SELECT_SQL, BeanPropertyRowMapper.newInstance(IdSegment.class), key);
    }

    /**
     * step increment
     *
     * @param key service key
     */
    @Override
    public void updateStepIncrement(String key) {
        jdbcTemplate.update("UPDATE sys_segment SET step = step * 2 WHERE biz_tag = ?", key);
    }

    /**
     * step decrement
     *
     * @param key service key
     */
    @Override
    public void updateStepDecrement(String key) {
        jdbcTemplate.update("UPDATE sys_segment SET step = step / 2 WHERE biz_tag = ?", key);
    }

}