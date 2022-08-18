package io.github.aapplet.segment;

import io.github.aapplet.segment.core.IdGenerator;
import io.github.aapplet.segment.core.IdProduction;
import io.github.aapplet.segment.loader.SegmentLoader;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

public class IdGenerateAutoConfiguration {

    @Bean
    IdGenerator idGenerator(SegmentLoader segmentLoader, ObjectProvider<IdGenerateCustomizer> idGenerateCustomizers) {
        idGenerateCustomizers.forEach(idGenerateCustomizer -> {
            idGenerateCustomizer.customize(null);
        });
        return new IdProduction(segmentLoader);
    }

    @Bean
    SegmentManager segmentManager(SqlSessionTemplate sqlSessionTemplate) {
        return new SegmentManager(sqlSessionTemplate);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    IdGenerateCustomizer idGenerateCustomizer() {
        return obj -> {
        };
    }

}
