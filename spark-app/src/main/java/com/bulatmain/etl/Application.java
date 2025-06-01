package com.bulatmain.etl;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.bulatmain.etl")
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .run(args);
    }
}
