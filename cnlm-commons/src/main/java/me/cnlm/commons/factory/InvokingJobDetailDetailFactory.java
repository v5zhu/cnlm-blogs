package me.cnlm.commons.factory;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.lang.reflect.Method;

/**
 * @author :   zhuxl@paxsz.com
 * @time :   2017/7/5
 * @description:
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class InvokingJobDetailDetailFactory extends QuartzJobBean {
    // 计划任务所在类
    private String targetObject;

    // 具体需要执行的计划任务
    private String targetMethod;

    private ApplicationContext ctx;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            Object otargetObject = ctx.getBean(targetObject);
            Method m = null;
            try {
                m = otargetObject.getClass().getMethod(targetMethod);
                m.invoke(otargetObject);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.ctx = applicationContext;
    }

    public void setTargetObject(String targetObject) {
        this.targetObject = targetObject;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }
}

