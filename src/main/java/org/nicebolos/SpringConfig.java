package org.nicebolos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"org.nicebolos","org.nicebolos.businesslayer"})
@EnableAutoConfiguration
public class SpringConfig {
    public static void main() {
        SpringApplication application = new SpringApplication(SpringConfig.class);
        application.run();
    }
}
