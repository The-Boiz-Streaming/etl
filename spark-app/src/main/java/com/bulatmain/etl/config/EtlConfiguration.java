package com.bulatmain.etl.config;

import com.bulatmain.etl.config.properties.EtlJobProperties;
import com.bulatmain.etl.scheduler.SchedulerService;
import com.bulatmain.etl.service.EtlJobService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class EtlConfiguration {
    @Bean
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .appName("ETL Spark Application")
                .getOrCreate();
    }

    @Bean
    public SchedulerService schedulerService() {
        return new SchedulerService();
    }

    @Slf4j
    @RequiredArgsConstructor
    public static class PostConstructScheduleRunner {
        Collection<EtlJobProperties> etlJobProperties;
        EtlJobService service;

        @PostConstruct
        public void run() {
            Map<String, Duration> intervals = etlJobProperties.stream()
                    .collect(Collectors.toMap(EtlJobProperties::getName, e -> Duration.ofMillis(e.getPeriodMs())));
            try {
                service.schedule(intervals);
            } catch (Exception e) {
                log.error("Could not schedule jobs {}", etlJobProperties);
            }
        }
    }

}