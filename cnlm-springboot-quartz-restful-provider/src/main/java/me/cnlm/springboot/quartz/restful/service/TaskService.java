package me.cnlm.springboot.quartz.restful.service;


import com.github.pagehelper.PageInfo;
import me.cnlm.exception.CoreException;
import me.cnlm.springboot.quartz.restful.entity.ScheduleJob;

import java.util.List;


public interface TaskService {

    /**
     * 从数据库中取 区别于getAllJob
     *
     * @return
     * @throws CoreException
     */
    PageInfo<ScheduleJob> getAllTask(int page, int pageSize) throws CoreException;

    /**
     * 根据搜索内容 从数据库中取任务
     *
     * @param content  搜索内容
     * @param page
     * @param pageSize
     * @return
     * @throws CoreException
     */
    PageInfo<ScheduleJob> getTasks(String content, int page, int pageSize) throws CoreException;

    /**
     * 添加到数据库中 区别于addJob
     *
     * @param job
     */
    void addTask(ScheduleJob job) throws CoreException;

    /**
     * 修改任务并保持到数据库
     *
     * @param job
     * @throws CoreException
     */
    void editTask(ScheduleJob job) throws CoreException;

    /**
     * 从数据库中查询job
     *
     * @param jobId
     * @throws CoreException
     */
    ScheduleJob getTaskById(Long jobId) throws CoreException;

    /**
     * 根据ID删除定时任务
     *
     * @param jobId
     * @throws CoreException
     */
    void delTaskById(Long jobId) throws CoreException;

    /**
     * 更改任务状态
     *
     * @throws CoreException
     */
    void changeStatus(Long jobId, String cmd) throws CoreException;

    /**
     * 更改任务 cron表达式
     *
     * @param jobId
     * @param
     * @throws CoreException
     */
    void updateCron(Long jobId) throws CoreException;

    /**
     * 添加任务
     *
     * @param job
     * @throws CoreException
     */
    void addJob(ScheduleJob job) throws CoreException;

    void init() throws Exception;

    /**
     * 获取所有计划中的任务列表
     *
     * @return
     * @throws CoreException
     */
    List<ScheduleJob> getAllJob() throws CoreException;

    /**
     * 所有正在运行的job
     *
     * @return
     * @throws CoreException
     */
    List<ScheduleJob> getRunningJob() throws CoreException;


    /**
     * 暂停一个job
     *
     * @param jobGroup
     * @param jobName
     * @throws CoreException
     */
    void pauseJob(String jobGroup, String jobName) throws CoreException;

    /**
     * 恢复一个job
     *
     * @param jobGroup
     * @param jobName
     * @throws CoreException
     */
    void resumeJob(String jobGroup, String jobName) throws CoreException;

    /**
     * 删除一个job
     *
     * @param jobGroup
     * @param jobName
     * @throws CoreException
     */
    void deleteJob(String jobGroup, String jobName) throws CoreException;


    /**
     * 立即执行job
     *
     * @param jobGroup
     * @param jobName
     * @throws CoreException
     */
    void runAJobNow(String jobGroup, String jobName) throws CoreException;

    /**
     * 更新job时间表达式
     *
     * @param jobGroup
     * @param jobName
     * @param cronExpression
     * @throws CoreException
     */
    void updateJobCron(String jobGroup, String jobName, String cronExpression)
            throws CoreException;

    /**
     * 检查表达式
     *
     * @return
     * @throws CoreException
     */

    Boolean verifyCronExpression(String cronExpression) throws CoreException;
}
