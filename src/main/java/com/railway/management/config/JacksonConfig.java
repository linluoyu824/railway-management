package com.railway.management.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        log.info("正在配置ObjectMapper Bean, 并注册JavaTimeModule...");
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册模块以支持Java 8的日期/时间类型 (例如 LocalDateTime)
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}