package me.cnlm.springboot.quartz.restful.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import me.cnlm.exception.CoreException;
import me.cnlm.springboot.quartz.restful.QuartzJobFactory;
import me.cnlm.springboot.quartz.restful.QuartzJobFactoryDisallowConcurrentExecution;
import me.cnlm.springboot.quartz.restful.dao.ScheduleJobDao;
import me.cnlm.springboot.quartz.restful.entity.ScheduleJob;
import me.cnlm.springboot.quartz.restful.service.TaskService;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springside.modules.mapper.BeanMapper;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * @author wei9.li@changhong.com
 * @Description: 计划任务管理
 * @date 2015年4月20日 下午2:43:54
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Service(version = "1.0")
@Component
public class TaskServiceImpl implements TaskService {
    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private ScheduleJobDao scheduleJobDao;

    /**
     * 从数据库中取 区别于getAllJob
     *
     * @return
     */
    @Override
    public PageInfo<ScheduleJob> getAllTask(int page, int pageSize) {
        //获取第1页，10条内容，默认查询总数count
        PageHelper.startPage(page, pageSize,true);
        List<ScheduleJob> jobs = scheduleJobDao.getAll();
        //分页实现
        //或者使用PageInfo类（下面的例子有介绍）
        PageInfo<ScheduleJob> pageInfo = new PageInfo(jobs);

        return pageInfo;
    }

    @Override
    public PageInfo<ScheduleJob> getTasks(String jobName, int page, int pageSize) throws CoreException {
        //获取第1页，10条内容，默认查询总数count
        PageHelper.startPage(page, pageSize);
        List<ScheduleJob> jobs = scheduleJobDao.getTaskByContent(jobName);
        //分页实现
        //或者使用PageInfo类（下面的例子有介绍）
        PageInfo<ScheduleJob> pageInfo = new PageInfo(jobs);

        return pageInfo;

    }

    /**
     * 添加到数据库中 区别于addJob
     */
    @Override
    public void addTask(ScheduleJob jobDto) {
        ScheduleJob job = BeanMapper.map(jobDto, ScheduleJob.class);
        job.setCreateTime(new Date());
        job.setJobStatus("0");
        scheduleJobDao.insertSelective(job);
    }

    @Override
    public void editTask(ScheduleJob jobDto) throws CoreException {
        ScheduleJob job = scheduleJobDao.selectByPrimaryKey(jobDto.getJobId());
        Date date = job.getCreateTime();
        String jobStatus = job.getJobStatus();

        BeanMapper.copy(jobDto, job);
        job.setCreateTime(date);
        job.setJobStatus(jobStatus);
        job.setUpdateTime(new Date());
        scheduleJobDao.updateByPrimaryKey(job);
    }

    /**
     * 从数据库中查询job
     */
    @Override
    public ScheduleJob getTaskById(Long jobId) {
        return BeanMapper.map(scheduleJobDao.selectByPrimaryKey(jobId), ScheduleJob.class);
    }

    @Override
    public void delTaskById(Long jobId) throws CoreException {
        scheduleJobDao.deleteByPrimaryKey(jobId);
    }

    /**
     * 更改任务状态
     *
     * @throws SchedulerException
     */
    public void changeStatus(Long jobId, String cmd) {
        ScheduleJob job = scheduleJobDao.selectByPrimaryKey(jobId);
        if (job == null) {
            return;
        }
        if ("stop".equals(cmd)) {
            deleteJob(job);
            job.setJobStatus(ScheduleJob.STATUS_NOT_RUNNING);
        } else if ("start".equals(cmd)) {
            job.setJobStatus(ScheduleJob.STATUS_RUNNING);
            addJob(job);
        }
        scheduleJobDao.updateByPrimaryKeySelective(job);
    }

    /**
     * 更改任务 cron表达式
     *
     * @throws SchedulerException
     */
    @Override
    public void updateCron(Long jobId) {
        ScheduleJob job = scheduleJobDao.selectByPrimaryKey(jobId);
        if (job == null) {
            return;
        }
//        job.setCronExpression(cron);
        if (ScheduleJob.STATUS_RUNNING.equals(job.getJobStatus())) {
            updateJobCron(job);
        }
//        scheduleJobDao.updateByPrimaryKeySelective(job);
    }

    /**
     * 添加任务
     *
     * @param job
     * @throws SchedulerException
     */
    @Override
    public void addJob(ScheduleJob job) {
        if (job == null || !ScheduleJob.STATUS_RUNNING.equals(job.getJobStatus())) {
            return;
        }
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        logger.debug(scheduler + ".......................................................................................add");
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
        try {
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

            // 不存在，创建一个
            if (null == trigger) {
                Class clazz = ScheduleJob.CONCURRENT_IS.equals(job.getIsConcurrent()) ? QuartzJobFactory.class : QuartzJobFactoryDisallowConcurrentExecution.class;

                JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(job.getJobName(), job.getJobGroup()).build();

                jobDetail.getJobDataMap().put("scheduleJob", job);

                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

                trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup()).withSchedule(scheduleBuilder).build();

                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                // Trigger已存在，那么更新相应的定时设置
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

                // 按新的cronExpression表达式重新构建trigger
                trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

                // 按新的trigger重新设置job执行
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    @PostConstruct
    public void init() throws Exception {

        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        // 这里获取任务信息数据
        List<ScheduleJob> jobList = scheduleJobDao.getAll();

        for (ScheduleJob job : jobList) {
            addJob(job);
        }
    }

    /**
     * 获取所有计划中的任务列表
     *
     * @return
     * @throws SchedulerException
     */
    @Override
    public List<ScheduleJob> getAllJob() {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
            List<ScheduleJob> jobList = new ArrayList();
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    ScheduleJob job = new ScheduleJob();
                    job.setJobName(jobKey.getName());
                    job.setJobGroup(jobKey.getGroup());
                    job.setDescription("触发器:" + trigger.getKey());
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    job.setJobStatus(triggerState.name());
                    if (trigger instanceof CronTrigger) {
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        String cronExpression = cronTrigger.getCronExpression();
                        job.setCronExpression(cronExpression);
                    }
                    jobList.add(job);
                }
            }
            return BeanMapper.mapList(jobList, ScheduleJob.class);
        } catch (SchedulerException e) {
            e.printStackTrace();

        }
        return null;
    }

    /**
     * 所有正在运行的job
     *
     * @return
     * @throws SchedulerException
     */
    public List<ScheduleJob> getRunningJob() {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
            List<ScheduleJob> jobList = new ArrayList<ScheduleJob>(executingJobs.size());
            for (JobExecutionContext executingJob : executingJobs) {
                ScheduleJob job = new ScheduleJob();
                JobDetail jobDetail = executingJob.getJobDetail();
                JobKey jobKey = jobDetail.getKey();
                Trigger trigger = executingJob.getTrigger();
                job.setJobName(jobKey.getName());
                job.setJobGroup(jobKey.getGroup());
                job.setDescription("触发器:" + trigger.getKey());
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                job.setJobStatus(triggerState.name());
                if (trigger instanceof CronTrigger) {
                    CronTrigger cronTrigger = (CronTrigger) trigger;
                    String cronExpression = cronTrigger.getCronExpression();
                    job.setCronExpression(cronExpression);
                }
                jobList.add(job);
            }
            return BeanMapper.mapList(jobList, ScheduleJob.class);

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 暂停一个job
     *
     * @param jobGroup
     * @param jobName
     * @throws SchedulerException
     */
    @Override
    public void pauseJob(String jobGroup, String jobName) throws CoreException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobGroup, jobName);
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
            String[] infos = {jobGroup, jobName, e.getMessage()};
            logger.error("停止任务:group [{}],name [{}] 失败,异常信息[{}]", infos);
            throw new CoreException("暂停任务失败");
        }
    }

    /**
     * 暂停一个job
     *
     * @param scheduleJob
     * @throws CoreException
     */
    private void pauseJob(ScheduleJob scheduleJob) throws CoreException {
        pauseJob(scheduleJob.getJobName(), scheduleJob.getJobGroup());
    }

    ;

    /**
     * 恢复一个job
     *
     * @param jobGroup
     * @param jobName
     * @throws SchedulerException
     */
    public void resumeJob(String jobGroup, String jobName) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            //todo throw coreException
            e.printStackTrace();
        }
    }

    /**
     * 删除一个job
     *
     * @param job
     * @throws SchedulerException
     */
    public void deleteJob(ScheduleJob job) {
        deleteJob(job.getJobGroup(), job.getJobName());
    }

    /**
     * 删除一个job
     *
     * @param jobGroup
     * @param jobName
     * @throws SchedulerException
     */
    @Override
    public void deleteJob(String jobGroup, String jobName) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        try {
            scheduler.deleteJob(jobKey);
            logger.info("任务分组[{}],任务名称 = [{}]------------------已停止", jobGroup, jobName);
        } catch (SchedulerException e) {
            //todo throw coreException
            e.printStackTrace();
        }

    }

    /**
     * 立即执行job
     *
     * @param jobGroup
     * @param jobName
     * @throws SchedulerException
     */
    @Override
    public void runAJobNow(String jobGroup, String jobName) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        try {
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            //todo throw coreException
            e.printStackTrace();
        }
    }

    /**
     * 更新job时间表达式
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void updateJobCron(ScheduleJob scheduleJob) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());

        CronTrigger trigger = null;
        try {
            trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新job时间表达式
     *
     * @param jobGroup
     * @param jobName
     * @param cronExpression
     * @throws SchedulerException
     */
    @Override
    public void updateJobCron(String jobGroup, String jobName, String cronExpression) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);

        CronTrigger trigger = null;
        try {
            trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }


    }

    @Override
    public Boolean verifyCronExpression(String cronExpression) {
        try {
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
        } catch (Exception e) {
            logger.error("cron表达式有误，不能被解析！");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("xxxxx");
    }
}
