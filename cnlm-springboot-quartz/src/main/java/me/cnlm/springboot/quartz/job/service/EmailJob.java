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
}
