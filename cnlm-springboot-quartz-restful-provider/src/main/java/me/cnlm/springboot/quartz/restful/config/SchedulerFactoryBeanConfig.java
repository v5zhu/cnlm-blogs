package me.cnlm.springboot.quartz.restful.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;

@Configuration
public class SchedulerFactoryBeanConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        return new SchedulerFactoryBean();
    }
}