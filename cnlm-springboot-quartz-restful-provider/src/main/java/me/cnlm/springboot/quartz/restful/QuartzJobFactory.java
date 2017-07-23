package me.cnlm.springboot.quartz.restful;

import me.cnlm.springboot.quartz.restful.entity.ScheduleJob;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * 
 * @Description: 计划任务执行处 无状态
 * @author wei9.li@changhong.com
 * @date 2014年4月24日 下午5:05:47
 */
public class QuartzJobFactory implements Job {
	public final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		ScheduleJob scheduleJob = (ScheduleJob) context.getMergedJobDataMap().get("scheduleJob");
		TaskUtils.invokMethod(scheduleJob);
	}
}