package com.bulatmain.etl.job;

import org.apache.avro.Schema;
import org.apache.spark.sql.SparkSession;

import java.util.Map;

public class TrackAddToPlaylistEtlJob extends BaseEtlJob {
    public TrackAddToPlaylistEtlJob(String id, SparkSession sparkSession, Map<String, String> readOptions, Map<String, String> writeOptions, Schema avroSchema) {
        super(id, sparkSession, readOptions, writeOptions, avroSchema);
    }
}
