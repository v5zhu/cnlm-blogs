package me.cnlm.springboot.quartz.restful.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.RequestContextFilter;

/**
 * Created by cnlm.me@qq.com on 2017/7/23.
 */
@Configuration
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(RequestContextFilter.class);
        //配置restful package.
        packages("me.cnlm.springboot.quartz.restful.rest.facade");
    }
}
