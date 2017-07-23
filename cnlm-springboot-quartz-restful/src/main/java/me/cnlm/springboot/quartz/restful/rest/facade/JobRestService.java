package me.cnlm.springboot.quartz.restful.rest.facade;

import com.github.pagehelper.PageInfo;
import me.cnlm.commons.response.Res;
import me.cnlm.exception.CoreException;
import me.cnlm.springboot.quartz.restful.entity.ScheduleJob;
import me.cnlm.springboot.quartz.restful.service.TaskService;
import me.cnlm.springboot.quartz.restful.utils.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;

/**
 * Created by cnlm.me@qq.com on 2017/7/22.
 */
@SuppressWarnings("ALL")
@Path("/test")
@Service
public class JobRestService {

    @Autowired
    private TaskService taskService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tasks")
    public Response taskList(@QueryParam(value = "name") String name,
                             @QueryParam(value = "page") int page) {
        PageInfo<ScheduleJob> pageInfo = null;
        //@RequestParam int pageNumbe,
        int pageSize = 10;
        try {
            if (StringUtils.isBlank(name)) {
                pageInfo = taskService.getAllTask(page, pageSize);
            } else {
                //搜索任务
                pageInfo = taskService.getTasks(name, page, pageSize);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return Response.ok().entity(pageInfo).build();
    }


    @POST
    @Path("task")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response taskList(ScheduleJob scheduleJob) {
        Boolean verified = null;
        try {
            verified = taskService.verifyCronExpression(scheduleJob.getCronExpression());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        if (!verified) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure("cron表达式有误，不能被解析！")).build();
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
                return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure("未找到目标类！")).build();
            } else {
                Class clazz = obj.getClass();
                Method method = null;

                method = clazz.getMethod(scheduleJob.getMethodName(), null);

                if (method == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure("未找到目标方法！")).build();

                }
                taskService.addTask(scheduleJob);
            }
        } catch (NoSuchBeanDefinitionException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure(e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure("保存失败，检查 name group 组合是否有重复！")).build();

        }
        return Response.status(Response.Status.CREATED).entity(Res.success()).build();
    }

    //编辑任务
    @POST
    @Path("task/edit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response taskEdit(ScheduleJob scheduleJob) {
        Boolean verified = null;
        if (scheduleJob.getJobId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure("任务ID为空，请重新请求")).build();
        }
        try {
            verified = taskService.verifyCronExpression(scheduleJob.getCronExpression());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        if (!verified) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure("cron表达式有误，不能被解析！")).build();
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
                return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure("未找到目标类！")).build();
            } else {
                Class clazz = obj.getClass();
                Method method = null;

                method = clazz.getMethod(scheduleJob.getMethodName(), null);

                if (method == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure("未找到目标方法！")).build();
                }
                taskService.editTask(scheduleJob);
            }
        } catch (NoSuchBeanDefinitionException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure(e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure("保存失败，检查 name group 组合是否有重复！")).build();

        }
        return Response.status(Response.Status.CREATED).entity(Res.success()).build();
    }

    @Path("task/{jobId}/status")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeJobStatus(@PathParam("jobId") Long jobId, String[] cmd) throws CoreException {
        if (cmd.length < 1) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Res.failure("任务状态改变失败！")).build();
        } else {
            taskService.changeStatus(jobId, cmd[0]);
        }
        return Response.status(Response.Status.OK).entity(Res.success()).build();
    }

    @Path("task/{jobId}/cron")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCron(@PathParam("jobId") Long jobId) throws CoreException {
//        Boolean verified = taskService.verifyCronExpression(cron[0]);
//        if (!verified) {
//            retObj.setMsg("cron表达式有误，不能被解析！");
//            return retObj;
//        }
        taskService.updateCron(jobId);
        return Response.status(Response.Status.OK).entity(Res.success()).build();
    }

    @DELETE
    @Path("task/{jobId}/deletion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteJob(@PathParam("jobId") Long jobId) throws CoreException {
        ScheduleJob task = taskService.getTaskById(jobId);
        //如果任务处于运行状态，需要先停止任务
        if (task.getJobStatus() != null &&
                task.getJobStatus().equals(ScheduleJob.STATUS_RUNNING)) {
            taskService.changeStatus(jobId, "stop");
        }

        taskService.delTaskById(jobId);
        return Response.status(Response.Status.OK).entity(Res.success()).build();
    }
}
