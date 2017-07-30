package me.cnlm.springmvc.quartz.console.controller;

import com.github.pagehelper.PageInfo;
import me.cnlm.commons.response.Res;
import me.cnlm.exception.CoreException;
import me.cnlm.springboot.quartz.restful.entity.ScheduleJob;
import me.cnlm.springboot.quartz.restful.service.TaskService;
import me.cnlm.springboot.quartz.restful.utils.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@SuppressWarnings("ALL")
@Controller
@RequestMapping("")
public class JobTaskController {
    // 日志记录器
    public final Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private TaskService taskService;

    @RequestMapping("tasks")
    public String taskList(HttpServletRequest request, @RequestParam(value = "name", defaultValue = "") String name,
                           @RequestParam(value = "page", defaultValue = "1") int page) {
        PageInfo<ScheduleJob> PageInfo = null;
        int pageSize = 10;
        try {
            if (StringUtils.isBlank(name)) {
                PageInfo = taskService.getAllTask(page, pageSize);
            } else {
                //搜索任务
                PageInfo = taskService.getTasks(name, page, pageSize);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        request.setAttribute("taskPage", PageInfo);
        return "task/taskList";
    }

    @RequestMapping(value = "task", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> taskList(@RequestBody ScheduleJob scheduleJob) {
        Boolean verified = null;
        try {
            verified = taskService.verifyCronExpression(scheduleJob.getCronExpression());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        if (!verified) {
            return new ResponseEntity(Res.failure("cron表达式有误，不能被解析！"), HttpStatus.BAD_REQUEST);
        }

        Object obj = null;
        try {
            if (StringUtils.isNotBlank(scheduleJob.getSpringId())) {
                obj = SpringUtils.getBean(scheduleJob.getSpringId());
            } else {
                Class clazz = Class.forName(scheduleJob.getBeanClass());
                obj = clazz.newInstance();
            }

            if (obj == null) {
                return new ResponseEntity(Res.failure("未找到目标类！"), HttpStatus.BAD_REQUEST);
            } else {
                Class clazz = obj.getClass();
                Method method = null;

                method = clazz.getMethod(scheduleJob.getMethodName(), null);

                if (method == null) {
                    return new ResponseEntity(Res.failure("未找到目标方法！"), HttpStatus.BAD_REQUEST);
                }
                taskService.addTask(scheduleJob);
            }
        } catch (NoSuchBeanDefinitionException e) {
            return new ResponseEntity(Res.failure(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(Res.failure("保存失败，检查 name group组合是否有重复！"), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity(Res.success(), HttpStatus.CREATED);
    }

    //编辑任务
    @RequestMapping(value = "task/edit", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> taskEdit(@RequestBody ScheduleJob scheduleJob) {
        Boolean verified = null;
        if (scheduleJob.getJobId() == null) {
            return new ResponseEntity<Object>(Res.failure("任务ID为空，请重新请求"), HttpStatus.BAD_REQUEST);
        }
        try {
            verified = taskService.verifyCronExpression(scheduleJob.getCronExpression());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        if (!verified) {
            return new ResponseEntity(Res.failure("cron表达式有误，不能被解析！"), HttpStatus.BAD_REQUEST);
        }

        Object obj = null;
        try {
            if (StringUtils.isNotBlank(scheduleJob.getSpringId())) {
                obj = SpringUtils.getBean(scheduleJob.getSpringId());
            } else {
                Class clazz = Class.forName(scheduleJob.getBeanClass());
                obj = clazz.newInstance();
            }

            if (obj == null) {
                return new ResponseEntity(Res.failure("未找到目标类！"), HttpStatus.FORBIDDEN);
            } else {
                Class clazz = obj.getClass();
                Method method = null;

                method = clazz.getMethod(scheduleJob.getMethodName(), null);

                if (method == null) {
                    return new ResponseEntity(Res.failure("未找到目标方法！"), HttpStatus.FORBIDDEN);
                }
                taskService.editTask(scheduleJob);
            }
        } catch (NoSuchBeanDefinitionException e) {
            return new ResponseEntity(Res.failure(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(Res.failure("保存失败，检查 name group 组合是否有重复！"), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<Object>(Res.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "task/{jobId}/status", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Object> changeJobStatus(@PathVariable Long jobId, @RequestBody String[] cmd) throws CoreException {
        if (cmd.length < 1) {
            return new ResponseEntity<Object>(Res.failure("任务状态改变失败！"), HttpStatus.FORBIDDEN);
        } else {
            taskService.changeStatus(jobId, cmd[0]);
        }
        return new ResponseEntity<Object>(Res.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "task/{jobId}/cron", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Object> updateCron(@PathVariable Long jobId) throws CoreException {
        taskService.updateCron(jobId);
        return new ResponseEntity<Object>(Res.success(), HttpStatus.OK);
    }

    @RequestMapping(value = "task/{jobId}/deletion", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Object> deleteJob(@PathVariable Long jobId) throws CoreException {

        ScheduleJob task = taskService.getTaskById(jobId);
        //如果任务处于运行状态，需要先停止任务
        if (task.getJobStatus() != null &&
                task.getJobStatus().equals(ScheduleJob.STATUS_RUNNING)) {
            taskService.changeStatus(jobId, "stop");
        }

        taskService.delTaskById(jobId);
        return new ResponseEntity<Object>(Res.success(), HttpStatus.OK);
    }
}
