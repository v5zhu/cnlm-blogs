package me.cnlm.springboot.quartz.config.triggers;

import me.cnlm.commons.factory.InvokingJobDetailDetailFactory;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cnlm.me@qq.com on 2017/7/8.
 */
@Configuration
public class EmailJobTrigger {

    @Value("${quartz.cron.email.message.send}")
    public String quartz_cron_email_message_send;

    @Bean
    public JobDetailFactoryBean sendEmailJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(InvokingJobDetailDetailFactory.class);
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        factoryBean.setGroup("CNLM-EMAIL");
        factoryBean.setDescription("邮件监控");
        Map<String, String> map = new HashMap<>();
        map.put("targetObject","emailJob");
        map.put("targetMethod", "sendEmail");
        factoryBean.setJobDataAsMap(map);
        return factoryBean;
    }
    @Bean
    public CronTriggerFactoryBean sendEmailTrigger(@Qualifier("sendEmailJobDetail") JobDetail jobDetail) {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setCronExpression(quartz_cron_email_message_send);
        return factoryBean;
    }
}
