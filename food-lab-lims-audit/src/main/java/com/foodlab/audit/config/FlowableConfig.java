package com.foodlab.audit.config;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

@Configuration
public class FlowableConfig {

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processEngineConfigurer() {
        return engineConfiguration -> {
            engineConfiguration.setHistoryLevel("audit");
            engineConfiguration.setAsyncExecutorActivate(true);
            engineConfiguration.setAsyncHistoryEnabled(true);
            try {
                ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources("classpath:/processes/*.bpmn20.xml");
                engineConfiguration.setDeploymentResources(resources);
            } catch (IOException e) {
                engineConfiguration.setDeploymentResources(new Resource[0]);
            }
        };
    }
}
