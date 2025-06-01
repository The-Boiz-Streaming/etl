package com.bulatmain.etl.config;

import com.bulatmain.etl.config.properties.*;
import com.bulatmain.etl.job.EtlJob;
import com.bulatmain.etl.job.TrackAddToPlaylistEtlJob;
import com.bulatmain.etl.job.TrackLikeEtlJob;
import com.bulatmain.etl.job.TrackPlaybackEtlJob;
import org.apache.avro.Schema;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class EtlJobsConfiguration {
    @Bean
    public TrackAddToPlaylistEtlJob trackAddToPlaylistEtlJob(
            TrackAddToPlaylistEtlJobProperties properties,
            SparkSession sparkSession,
            KafkaProperties kafkaProperties,
            ClickHouseProperties clickHouseProperties) throws IOException {
        var props = new PropertiesPack(properties, sparkSession, kafkaProperties, clickHouseProperties);
        return etlJob(props, p -> new TrackAddToPlaylistEtlJob(
                properties.getName(),
                sparkSession,
                p.kafkaOptions,
                p.clickhouseOptions,
                p.schema));
    }

    @Bean
    public TrackPlaybackEtlJob trackPlaybackEtlJob(
            TrackPlaybackEtlJobProperties properties,
            SparkSession sparkSession,
            KafkaProperties kafkaProperties,
            ClickHouseProperties clickHouseProperties) throws IOException {
        var props = new PropertiesPack(properties, sparkSession, kafkaProperties, clickHouseProperties);
        return etlJob(props, p -> new TrackPlaybackEtlJob(
                properties.getName(),
                sparkSession,
                p.kafkaOptions,
                p.clickhouseOptions,
                p.schema));
    }

    @Bean
    public TrackLikeEtlJob trackLikeEtlJob(
            TrackLikeEtlJobProperties properties,
            SparkSession sparkSession,
            KafkaProperties kafkaProperties,
            ClickHouseProperties clickHouseProperties) throws IOException {
        var props = new PropertiesPack(properties, sparkSession, kafkaProperties, clickHouseProperties);
        return etlJob(props, p -> new TrackLikeEtlJob(
                properties.getName(),
                sparkSession,
                p.kafkaOptions,
                p.clickhouseOptions,
                p.schema));
    }

    private <T extends EtlJob> T etlJob(PropertiesPack pack, Function<PreparedPropertiesPack, T> supplier) throws IOException {
        // Configure Kafka options
        Map<String, String> kafkaOptions = new HashMap<>();
        kafkaOptions.put("kafka.bootstrap.servers", pack.kafkaProperties.getBootstrapServers());
        kafkaOptions.put("subscribe", pack.properties.getTopic());

        // Configure ClickHouse options
        Map<String, String> clickhouseOptions = new HashMap<>();
        clickhouseOptions.put("driver", "com.clickhouse.jdbc.ClickHouseDriver");
        clickhouseOptions.put("url", pack.clickHouseProperties.getUrl());
        clickhouseOptions.put("user", pack.clickHouseProperties.getUser());
        clickhouseOptions.put("password", pack.clickHouseProperties.getPassword());
        clickhouseOptions.put("dbtable", pack.properties.getTable());

        // Load Avro schema
        ClassLoader cl = EtlJobsConfiguration.class.getClassLoader();
        InputStream stream = cl.getResourceAsStream(pack.properties.getSchemaFile());
        Schema avroSchema = new Schema.Parser().parse(stream);

        var preparedPropertiesPack = new PreparedPropertiesPack(
                pack.sparkSession,
                kafkaOptions,
                clickhouseOptions,
                avroSchema);
        return supplier.apply(preparedPropertiesPack);
    }

    private record PropertiesPack(
            EtlJobProperties properties,
            SparkSession sparkSession,
            KafkaProperties kafkaProperties,
            ClickHouseProperties clickHouseProperties) {}

    private record PreparedPropertiesPack(
            SparkSession sparkSession,
            Map<String, String> kafkaOptions,
            Map<String, String> clickhouseOptions,
            Schema schema) {}
}
