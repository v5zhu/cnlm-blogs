package me.cnlm.springmvc.quartz.console.service;

import me.cnlm.springboot.quartz.restful.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by cnlm.me@qq.com on 2017/7/29.
 */
@Service
public class ConsumerTaskService {
    @Autowired
    private TaskService taskService;

    public void test(){
        System.out.println(111111111111111111L);
        System.out.println(taskService.getAllJob().size());
    }
}
