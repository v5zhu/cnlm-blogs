# Springboot集成quartz之集群模式（第三期）

##本期将提供quartz集群能力
- 集群案例分析：
 上一期的邮件发送功能，若在服务需要部署多节点，但定时任务不支持集群，因此，多节点定时任务势必会同时运行，
 若向用户发送邮件通知，这种情况下会向用户发送两次一模一样的邮件，N个节点会发送N次邮件，严重不符合业务场景，
 若提供集群能力，则多节点间应分担邮件发送的工作而不是各节点做重复的工作，因此在部署多节点的时候定时任务也需要提供集群能力。
- 个人见解：
  1. quartz集群分为水平集群和垂直集群，水平集群即将定时任务节点部署在不同的服务器，水平集群最大的问题就是时钟同步问题，
  quartz集群强烈要求时钟同步，若时钟不能同步，则会导致集群中各个节点状态紊乱，造成不可预知的后果，请自行搜索`服务器时钟同步`,
  若能保证时钟同步，水平集群能保证服务的可靠性，其中一个节点挂掉或其中一个服务器宕机，其他节点依然正常服务；垂直集群则是集群各节点部署在同一台服务器，
  时钟同步自然不是问题，但存在单点故障问题，服务器宕机会严重影响服务的可用性。因此，要结合实际情况来考虑集群方案
  2. 由于集群中强烈要求时钟同步，因此不管是垂直集群还是水平集群，本地开发决不能连接线上环境（本地也是集群模式），这样的话势必会破坏集群，但本地若是非集群模式，
  则可以依情况来连接线上环境。
  3. quartz集群和redis这样的集群实现方式不一样，redis集群需要节点之间通信，各节点需要知道其他节点的状况，而quartz集群的实现
  方式在于11张表，集群节点相互之间不通信，而是通过定时任务持久化加锁的方式来实现集群。
  4. 破坏集群后果一般是死锁或者状态紊乱每个节点都不可用或其中某些节点能用部分或全部的定时任务

##1. 创建集群需要的11张表
```sql
t_b_qrtz_blob_triggers
t_b_qrtz_calendars
t_b_qrtz_cron_triggers
t_b_qrtz_fired_triggers
t_b_qrtz_job_details
t_b_qrtz_locks
t_b_qrtz_paused_trigger_grps
t_b_qrtz_scheduler_state
t_b_qrtz_simple_triggers
t_b_qrtz_simprop_triggers
t_b_qrtz_triggers
```
##2. 集群建表sql
```sql
drop table if exists t_b_qrtz_fired_triggers;
drop table if exists t_b_qrtz_paused_trigger_grps;
drop table if exists t_b_qrtz_scheduler_state;
drop table if exists t_b_qrtz_locks;
drop table if exists t_b_qrtz_simple_triggers;
drop table if exists t_b_qrtz_simprop_triggers;
drop table if exists t_b_qrtz_cron_triggers;
drop table if exists t_b_qrtz_blob_triggers;
drop table if exists t_b_qrtz_triggers;
drop table if exists t_b_qrtz_job_details;
drop table if exists t_b_qrtz_calendars;

create table t_b_qrtz_job_details(
  sched_name varchar(120) not null,
  job_name varchar(200) not null,
  job_group varchar(200) not null,
  description varchar(250) null,
  job_class_name varchar(250) not null,
  is_durable varchar(1) not null,
  is_nonconcurrent varchar(1) not null,
  is_update_data varchar(1) not null,
  requests_recovery varchar(1) not null,
  job_data blob null,
  primary key (sched_name,job_name,job_group))
  engine=innodb;

create table t_b_qrtz_triggers (
  sched_name varchar(120) not null,
  trigger_name varchar(200) not null,
  trigger_group varchar(200) not null,
  job_name varchar(200) not null,
  job_group varchar(200) not null,
  description varchar(250) null,
  next_fire_time bigint(13) null,
  prev_fire_time bigint(13) null,
  priority integer null,
  trigger_state varchar(16) not null,
  trigger_type varchar(8) not null,
  start_time bigint(13) not null,
  end_time bigint(13) null,
  calendar_name varchar(200) null,
  misfire_instr smallint(2) null,
  job_data blob null,
  primary key (sched_name,trigger_name,trigger_group),
  foreign key (sched_name,job_name,job_group)
  references t_b_qrtz_job_details(sched_name,job_name,job_group))
  engine=innodb;

create table t_b_qrtz_simple_triggers (
  sched_name varchar(120) not null,
  trigger_name varchar(200) not null,
  trigger_group varchar(200) not null,
  repeat_count bigint(7) not null,
  repeat_interval bigint(12) not null,
  times_triggered bigint(10) not null,
  primary key (sched_name,trigger_name,trigger_group),
  foreign key (sched_name,trigger_name,trigger_group)
  references t_b_qrtz_triggers(sched_name,trigger_name,trigger_group))
  engine=innodb;

create table t_b_qrtz_cron_triggers (
  sched_name varchar(120) not null,
  trigger_name varchar(200) not null,
  trigger_group varchar(200) not null,
  cron_expression varchar(120) not null,
  time_zone_id varchar(80),
  primary key (sched_name,trigger_name,trigger_group),
  foreign key (sched_name,trigger_name,trigger_group)
  references t_b_qrtz_triggers(sched_name,trigger_name,trigger_group))
  engine=innodb;

create table t_b_qrtz_simprop_triggers
(
  sched_name varchar(120) not null,
  trigger_name varchar(200) not null,
  trigger_group varchar(200) not null,
  str_prop_1 varchar(512) null,
  str_prop_2 varchar(512) null,
  str_prop_3 varchar(512) null,
  int_prop_1 int null,
  int_prop_2 int null,
  long_prop_1 bigint null,
  long_prop_2 bigint null,
  dec_prop_1 numeric(13,4) null,
  dec_prop_2 numeric(13,4) null,
  bool_prop_1 varchar(1) null,
  bool_prop_2 varchar(1) null,
  primary key (sched_name,trigger_name,trigger_group),
  foreign key (sched_name,trigger_name,trigger_group)
  references t_b_qrtz_triggers(sched_name,trigger_name,trigger_group))
  engine=innodb;

create table t_b_qrtz_blob_triggers (
  sched_name varchar(120) not null,
  trigger_name varchar(200) not null,
  trigger_group varchar(200) not null,
  blob_data blob null,
  primary key (sched_name,trigger_name,trigger_group),
  index (sched_name,trigger_name, trigger_group),
  foreign key (sched_name,trigger_name,trigger_group)
  references t_b_qrtz_triggers(sched_name,trigger_name,trigger_group))
  engine=innodb;

create table t_b_qrtz_calendars (
  sched_name varchar(120) not null,
  calendar_name varchar(200) not null,
  calendar blob not null,
  primary key (sched_name,calendar_name))
  engine=innodb;

create table t_b_qrtz_paused_trigger_grps (
  sched_name varchar(120) not null,
  trigger_group varchar(200) not null,
  primary key (sched_name,trigger_group))
  engine=innodb;

create table t_b_qrtz_fired_triggers (
  sched_name varchar(120) not null,
  entry_id varchar(95) not null,
  trigger_name varchar(200) not null,
  trigger_group varchar(200) not null,
  instance_name varchar(200) not null,
  fired_time bigint(13) not null,
  sched_time bigint(13) not null,
  priority integer not null,
  state varchar(16) not null,
  job_name varchar(200) null,
  job_group varchar(200) null,
  is_nonconcurrent varchar(1) null,
  requests_recovery varchar(1) null,
  primary key (sched_name,entry_id))
  engine=innodb;

create table t_b_qrtz_scheduler_state (
  sched_name varchar(120) not null,
  instance_name varchar(200) not null,
  last_checkin_time bigint(13) not null,
  checkin_interval bigint(13) not null,
  primary key (sched_name,instance_name))
  engine=innodb;

create table t_b_qrtz_locks (
  sched_name varchar(120) not null,
  lock_name varchar(40) not null,
  primary key (sched_name,lock_name))
  engine=innodb;

create index idx_qrtz_j_req_recovery on t_b_qrtz_job_details(sched_name,requests_recovery);
create index idx_qrtz_j_grp on t_b_qrtz_job_details(sched_name,job_group);

create index idx_qrtz_t_j on t_b_qrtz_triggers(sched_name,job_name,job_group);
create index idx_qrtz_t_jg on t_b_qrtz_triggers(sched_name,job_group);
create index idx_qrtz_t_c on t_b_qrtz_triggers(sched_name,calendar_name);
create index idx_qrtz_t_g on t_b_qrtz_triggers(sched_name,trigger_group);
create index idx_qrtz_t_state on t_b_qrtz_triggers(sched_name,trigger_state);
create index idx_qrtz_t_n_state on t_b_qrtz_triggers(sched_name,trigger_name,trigger_group,trigger_state);
create index idx_qrtz_t_n_g_state on t_b_qrtz_triggers(sched_name,trigger_group,trigger_state);
create index idx_qrtz_t_next_fire_time on t_b_qrtz_triggers(sched_name,next_fire_time);
create index idx_qrtz_t_nft_st on t_b_qrtz_triggers(sched_name,trigger_state,next_fire_time);
create index idx_qrtz_t_nft_misfire on t_b_qrtz_triggers(sched_name,misfire_instr,next_fire_time);
create index idx_qrtz_t_nft_st_misfire on t_b_qrtz_triggers(sched_name,misfire_instr,next_fire_time,trigger_state);
create index idx_qrtz_t_nft_st_misfire_grp on t_b_qrtz_triggers(sched_name,misfire_instr,next_fire_time,trigger_group,trigger_state);

create index idx_qrtz_ft_trig_inst_name on t_b_qrtz_fired_triggers(sched_name,instance_name);
create index idx_qrtz_ft_inst_job_req_rcvry on t_b_qrtz_fired_triggers(sched_name,instance_name,requests_recovery);
create index idx_qrtz_ft_j_g on t_b_qrtz_fired_triggers(sched_name,job_name,job_group);
create index idx_qrtz_ft_jg on t_b_qrtz_fired_triggers(sched_name,job_group);
create index idx_qrtz_ft_t_g on t_b_qrtz_fired_triggers(sched_name,trigger_name,trigger_group);
create index idx_qrtz_ft_tg on t_b_qrtz_fired_triggers(sched_name,trigger_group);

commit;
```
##3. 工程中application.properties添加集群配置
```xml
quartz.scheduler.instanceName=CnlmScheduler
org.quartz.dataSource.myDS.driver=com.mysql.cj.jdbc.Driver
org.quartz.dataSource.myDS.URL=jdbc:mysql://120.77.172.111:3306/touch6?useUnicode=true&characterEncoding=utf8
org.quartz.dataSource.myDS.user=cnlm.me
org.quartz.dataSource.myDS.password=123456
org.quartz.dataSource.myDS.maxConnections=10
```
##4. 调度工厂配置集群参数支持集群能力，下面是支持集群能力的调度工厂类
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

    @Value("${quartz.scheduler.instanceName}")
    private String quartzInstanceName;

    @Value("${org.quartz.dataSource.myDS.driver}")
    private String myDSDriver;

    @Value("${org.quartz.dataSource.myDS.URL}")
    private String myDSURL;

    @Value("${org.quartz.dataSource.myDS.user}")
    private String myDSUser;

    @Value("${org.quartz.dataSource.myDS.password}")
    private String myDSPassword;

    @Value("${org.quartz.dataSource.myDS.maxConnections}")
    private String myDSMaxConnections;


    /**
     * 定时任务集群配置
     * 设置属性
     *
     * @return
     * @throws IOException
     */
    private Properties quartzProperties() throws IOException {
        Properties prop = new Properties();
        prop.put("quartz.scheduler.instanceName", quartzInstanceName);
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        prop.put("org.quartz.scheduler.skipUpdateCheck", "true");
        prop.put("org.quartz.scheduler.jmx.export", "true");

        prop.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        prop.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        prop.put("org.quartz.jobStore.dataSource", "quartzDataSource");
        prop.put("org.quartz.jobStore.tablePrefix", "T_B_QRTZ_");
        prop.put("org.quartz.jobStore.isClustered", "true");

        prop.put("org.quartz.jobStore.clusterCheckinInterval", "20000");
        prop.put("org.quartz.jobStore.dataSource", "myDS");
        prop.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "1");
        prop.put("org.quartz.jobStore.misfireThreshold", "120000");
        prop.put("org.quartz.jobStore.txIsolationLevelSerializable", "true");
        prop.put("org.quartz.jobStore.selectWithLockSQL", "SELECT * FROM {0}LOCKS WHERE LOCK_NAME = ? FOR UPDATE");

        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", "10");
        prop.put("org.quartz.threadPool.threadPriority", "5");
        prop.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");

        prop.put("org.quartz.dataSource.myDS.driver", myDSDriver);
        prop.put("org.quartz.dataSource.myDS.URL", myDSURL);
        prop.put("org.quartz.dataSource.myDS.user", myDSUser);
        prop.put("org.quartz.dataSource.myDS.password", myDSPassword);
        prop.put("org.quartz.dataSource.myDS.maxConnections", myDSMaxConnections);

        prop.put("org.quartz.plugin.triggHistory.class", "org.quartz.plugins.history.LoggingJobHistoryPlugin");
        prop.put("org.quartz.plugin.shutdownhook.class", "org.quartz.plugins.management.ShutdownHookPlugin");
        prop.put("org.quartz.plugin.shutdownhook.cleanShutdown", "true");
        return prop;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(@Qualifier("sendEmailTrigger") Trigger sendEmailTrigger) throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        //用于quartz集群,加载quartz数据源
        //factory.setDataSource(dataSource);
        factory.setStartupDelay(10);
//        factory.setQuartzProperties(quartzProperties());
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

##5. 测试验证，略（分别运行两个项目节点，可以发现上述11张表中添加了2个节点的信息和定时任务运行状态，此处可自行验证）

##本期已结束，至此项目支持定时任务分布式集群模式




##**欢迎加入技术交流QQ群566654343**`（菜鸟联盟 ）`