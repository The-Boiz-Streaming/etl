package com.bulatmain.etl.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "etl.jobs.track-like")
public class TrackLikeEtlJobProperties extends EtlJobProperties {
    @Override
    public String getName() {
        return "track-like";
    }
}
