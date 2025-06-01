package com.bulatmain.etl.job;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.UUID;

public interface EtlJob {
    String getId();
    Dataset<Row> extract();
    Dataset<Row> transform(Dataset<Row> data);
    void load(Dataset<Row> data);

    default void run() {
        var data = extract();
        var transformed = transform(data);
        load(data);
    }
}
