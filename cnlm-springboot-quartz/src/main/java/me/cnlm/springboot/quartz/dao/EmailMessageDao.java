package me.cnlm.springboot.quartz.dao;

import me.cnlm.springboot.quartz.entity.EmailMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by cnlm.me@qq.com on 2017/7/22.
 */
@Mapper
public interface EmailMessageDao {
    List<EmailMessage> scanForSending();

    int sendSuccessfully(Long id);
}
