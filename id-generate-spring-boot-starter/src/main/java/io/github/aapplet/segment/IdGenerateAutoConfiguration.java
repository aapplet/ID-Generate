package io.github.aapplet.segment;

import io.github.aapplet.segment.core.IdGenerator;
import io.github.aapplet.segment.core.IdProduction;
import io.github.aapplet.segment.loader.SegmentLoader;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;

public class IdGenerateAutoConfiguration {

    @Bean
    IdGenerator idGenerator(SegmentLoader segmentLoader) {
        return new IdProduction(segmentLoader);
    }

    @Bean
    SegmentManager segmentManager(SqlSessionTemplate sqlSessionTemplate) {
        return new SegmentManager(sqlSessionTemplate);
    }

}
