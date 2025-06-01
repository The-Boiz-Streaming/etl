package com.bulatmain.etl.job;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.*;
import static org.apache.spark.sql.avro.functions.*;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class BaseEtlJob implements EtlJob {
    @Getter
    private final String id;
    private final SparkSession sparkSession;
    private final Map<String, String> readOptions;
    private final Map<String, String> writeOptions;
    private final Schema avroSchema;

    @Override
    public Dataset<Row> extract() {
        log.info("Extracting data from topic {}", readOptions.get("subscribe"));
        return sparkSession.read()
                .format("kafka")
                .options(readOptions)
                .load()
                .select(from_avro(col("value"), avroSchema.toString()))
                .as("data")
                .select("data.*");
    }

    @Override
    public Dataset<Row> transform(Dataset<Row> data) {
        log.info("Transforming data");
        return data.dropDuplicates();
    }

    @Override
    public void load(Dataset<Row> data) {
        log.info("Loading data to {}", writeOptions.get("dbtable"));
        data.write()
                .format("jdbc")
                .options(writeOptions)
                .mode("append")
                .save();
    }
}
