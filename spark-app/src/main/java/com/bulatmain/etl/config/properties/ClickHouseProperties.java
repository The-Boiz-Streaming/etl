package com.bulatmain.etl.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "clickhouse")
public class ClickHouseProperties {
    private String url;
    private String user;
    private String password;
}
