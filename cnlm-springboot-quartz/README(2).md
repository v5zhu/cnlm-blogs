# Springboot集成quartz之定时向用户发送邮件（第二期）

##本期使用邮件发送的功能来验证springboot集成定时任务

##1. 创建表t_b_email_message
```sql
CREATE TABLE `t_b_email_message` (
  `user_id` bigint(20) NOT NULL COMMENT '用户主键id',
  `status` tinyint(1) NOT NULL COMMENT '0：不发送，1：需要发送，2：发送成功',
  `sender` varchar(32) NOT NULL COMMENT '邮件发送者',
  `receiver` varchar(32) NOT NULL COMMENT '邮件接收者',
  `cc` varchar(255) DEFAULT NULL COMMENT '邮件抄送人，可多个，逗号隔开',
  `bcc` varchar(255) DEFAULT NULL COMMENT '邮件密送人，可多个，逗号隔开',
  `title` varchar(255) NOT NULL COMMENT '邮件标题',
  `content` varchar(1024) NOT NULL COMMENT '邮件内容',
  `send_time` datetime NOT NULL COMMENT '邮件发送时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```
##2. 创建实体类EmailMessage
```java
package me.cnlm.springboot.quartz.entity;

import java.util.Date;

/**
 * Created by cnlm.me@qq.com on 2017/7/22.
 */
public class EmailMessage {
    private Long userId;
    private Integer status;
    private String sender;
    private String receiver;
    private String cc;
    private String bcc;
    private String title;
    private String content;
    private Date sendTime;

    ···getter setter···
}
```
##3. 创建mybatis xml并提供扫描需要发送邮件的查询
- 两个sql功能分别是扫描需要发送邮件的记录以及发送成功后修改标志位
```xml
<select id="scanForSending"  resultType="EmailMessage">
    select em.*
    from t_b_email_message em
    where em.status=1
</select>
<update id="sendSuccessfully" parameterType="long">
    update t_b_email_message
    <set>
        status=2
    </set>
    where id=#{arg0}
</update>
```

##4. 创建EmailJob类用于处理邮件发送服务

- 此service扫描出来的数据有可能很多，若用for循环来串行处理，即便是分成n个小事务，数据量大的时候串行一样会可能导致锁表，这在工作当中曾遇到过，因此采用线程池来处理邮件的发送，效率高
```java
package me.cnlm.springboot.quartz.job.service;

import me.cnlm.mail.service.EmailSender;
import me.cnlm.springboot.quartz.dao.EmailMessageDao;
import me.cnlm.springboot.quartz.entity.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by cnlm.me@qq.com on 2017/7/22.
 */
@SuppressWarnings("ALL")
@Service
public class EmailJob {
    private static final Logger logger = LoggerFactory.getLogger(EmailJob.class);

    @Autowired
    private EmailMessageDao emailMessageDao;
    @Autowired
    private TaskExecutor taskExecutor;

    public void sendEmail() {
        List<EmailMessage> emailMessageList = emailMessageDao.scanForSending();
        logger.info("查询到{}条数据需要发送邮件", emailMessageList.size());

        for (int i = 0; i < emailMessageList.size(); i++) {
            EmailMessage emailMessage = emailMessageList.get(i);
            try {
                logger.info("开始发送第{}封邮件", i+1);
                taskExecutor.execute(new SingleEmailRunnable(emailMessage));
                logger.info("结束发送第{}封邮件", i+1);
            } catch (Exception e) {
                logger.info("发送第{}封邮件出现异常", i+1);
            }
        }
    }
}

```

##5. 创建EmailJob内部类SingleEmailRunnable并实现Runnable接口，用于实现多线程发送邮件
```java

    private class SingleEmailRunnable implements Runnable {
        private EmailMessage emailMessage;

        public SingleEmailRunnable(EmailMessage emailMessage) {
            this.emailMessage = emailMessage;
        }

        @Override
        @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
        public void run() {
            EmailSender.sendMimeMessageMail(
                    emailMessage.getSender(),
                    emailMessage.getReceiver(),
                    emailMessage.getCc(),
                    emailMessage.getBcc(),
                    emailMessage.getTitle(),
                    emailMessage.getContent()
            );
            //发送成功
            int updated = emailMessageDao.sendSuccessfully(emailMessage.getId());
            logger.info("发送成功，修改标志位结果:{}", updated);
        }
    }
```
##6. 配置quartz并注册定时任务
- 定义任务的触发器
```java
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

```
- 注册触发器到调度器工厂
```java
package me.cnlm.springboot.quartz.config;

import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.util.Properties;

@Configuration
public class SchedulerFactoryBeanConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(@Qualifier("sendEmailTrigger") Trigger sendEmailTrigger) throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setStartupDelay(10);
        factory.setAutoStartup(true);
        factory.setApplicationContextSchedulerContextKey("applicationContext");
        //注册触发器
        factory.setTriggers(
                sendEmailTrigger
        );
        return factory;
    }
}
```

##7. 运行并验证定时任务
```
  Scheduler class: 'org.quartz.core.QuartzScheduler' - running locally.
  NOT STARTED.
  Currently in standby mode.
  Number of jobs executed: 0
  Using thread pool 'org.quartz.simpl.SimpleThreadPool' - with 10 threads.
  Using job-store 'org.quartz.simpl.RAMJobStore' - which does not support persistence. and is not clustered.
```
- 可以看到，此时定时任务不支持持久化也不支持集群
``` 
2017-07-22 21:45:10.605  INFO 12732 --- [lerFactoryBean]] o.s.s.quartz.SchedulerFactoryBean        : Starting Quartz Scheduler now, after delay of 10 seconds
2017-07-22 21:45:10.606  INFO 12732 --- [lerFactoryBean]] org.quartz.core.QuartzScheduler          : Scheduler schedulerFactoryBean_$_NON_CLUSTERED started.
2017-07-22 21:45:10.806  INFO 12732 --- [ryBean_Worker-1] com.alibaba.druid.pool.DruidDataSource   : {dataSource-1} inited
2017-07-22 21:45:11.353  INFO 12732 --- [ryBean_Worker-1] m.c.s.quartz.job.service.EmailJob        : 查询到1条数据需要发送邮件
2017-07-22 21:45:11.354  INFO 12732 --- [ryBean_Worker-1] m.c.s.quartz.job.service.EmailJob        : 开始发送第1封邮件
2017-07-22 21:45:11.355  INFO 12732 --- [ryBean_Worker-1] m.c.s.quartz.job.service.EmailJob        : 结束发送第1封邮件
2017-07-22 21:45:12.541  INFO 12732 --- [ taskExecutor-1] m.c.s.quartz.job.service.EmailJob        : 发送成功，修改标志位结果:1
2017-07-22 21:45:20.040  INFO 12732 --- [ryBean_Worker-2] m.c.s.quartz.job.service.EmailJob        : 查询到0条数据需要发送邮件
2017-07-22 21:45:40.037  INFO 12732 --- [ryBean_Worker-3] m.c.s.quartz.job.service.EmailJob        : 查询到0条数据需要发送邮件
2017-07-22 21:46:00.037  INFO 12732 --- [ryBean_Worker-4] m.c.s.quartz.job.service.EmailJob        : 查询到0条数据需要发送邮件
2017-07-22 21:46:20.037  INFO 12732 --- [ryBean_Worker-5] m.c.s.quartz.job.service.EmailJob        : 查询到0条数据需要发送邮件
```
- 这里可以看出，定时任务已经成功运行，并按照既定的cron `quartz.cron.email.message.send=0/20 * * * * ?`表达式每20秒钟运行一次
- 温馨提示：正式使用时建议表达式设置至少2分钟执行一次，因很多邮件服务器会因为发送频率过高而发送失败

##本期已结束，至此项目已经可以定时为用户发送邮件通知了，下期：
- **Springboot集成quartz之集群（第三期）**

##**欢迎加入技术交流QQ群566654343**`（菜鸟联盟 ）`