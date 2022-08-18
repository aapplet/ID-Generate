package io.github.aapplet.segment;

import io.github.aapplet.segment.loader.SegmentData;
import io.github.aapplet.segment.loader.SegmentLoader;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class SegmentManager implements SegmentLoader {

    private final SegmentMapper segmentMapper;

    public SegmentManager(SqlSessionTemplate sqlSessionTemplate) {
        sqlSessionTemplate.getConfiguration().addMapper(SegmentMapper.class);
        this.segmentMapper = sqlSessionTemplate.getMapper(SegmentMapper.class);
    }

    @Override
    public SegmentData initSegment(String key) {
        segmentMapper.insertSegment(key);
        return this.getSegment(key);
    }

    @Override
    public SegmentData getSegment(String key) {
        segmentMapper.updateSegment(key);
        return segmentMapper.getSegment(key);
    }

    @Override
    public void updateStepIncrement(String key) {
        segmentMapper.updateStepIncrement(key);
    }

    @Override
    public void updateStepDecrement(String key) {
        segmentMapper.updateStepDecrement(key);
    }

}
