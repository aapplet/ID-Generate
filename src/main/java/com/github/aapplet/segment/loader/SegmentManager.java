package com.github.aapplet.segment.loader;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class SegmentManager implements SegmentLoader {

    private final SegmentMapper segmentMapper;
    private final TransactionTemplate transactionTemplate;

    public SegmentManager(SqlSessionTemplate sqlSessionTemplate, TransactionTemplate transactionTemplate) {
        sqlSessionTemplate.getConfiguration().addMapper(SegmentMapper.class);
        this.segmentMapper = sqlSessionTemplate.getMapper(SegmentMapper.class);
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public SegmentData initSegment(String tag) {
        segmentMapper.insertSegment(tag);
        return this.getSegment(tag);
    }

    @Override
    public SegmentData getSegment(String tag) {
        return transactionTemplate.execute(status -> {
            segmentMapper.updateSegment(tag);
            return segmentMapper.getSegment(tag);
        });
    }

    @Override
    public void updateStepIncrement(String tag) {
        segmentMapper.updateStepIncrement(tag);
    }

    @Override
    public void updateStepDecrement(String tag) {
        segmentMapper.updateStepDecrement(tag);
    }

}