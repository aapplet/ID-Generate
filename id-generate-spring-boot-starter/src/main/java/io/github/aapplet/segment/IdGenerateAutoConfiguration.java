package io.github.aapplet.segment;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

public class IdGenerateAutoConfiguration {

    @Bean
    IdGenerator idGenerator(IdSegmentLoader idSegmentLoader) {
        return new IdGenerator(idSegmentLoader);
    }

    @Bean
    IdSegmentLoader segmentManager(JdbcTemplate jdbcTemplate) {
        return new IdSegmentManager(jdbcTemplate);
    }

}