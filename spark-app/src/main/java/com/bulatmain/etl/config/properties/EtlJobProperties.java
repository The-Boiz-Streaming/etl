package com.bulatmain.etl.config.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class EtlJobProperties {
    private String topic;
    private String schemaFile;
    private String table;
    private long periodMs;

    public abstract String getName();
}
