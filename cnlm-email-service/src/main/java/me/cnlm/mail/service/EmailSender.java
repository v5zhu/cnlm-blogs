package me.cnlm.mail.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.Authenticator;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Date;
import java.util.Properties;

/**
 * Created by cnlm.me@qq.com on 2016/08/19.
 */
public class EmailSender extends Authenticator {
    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);
    private static final String logger_prefix_email = "邮件定时通知:";


    /**
     * 发送带HTML等复杂内容的邮件
     *
     * @param to
     * @param subject
     * @param htmlContent
     */
    public static void sendMimeMessageMail(String from, String to, String cc, String bcc, String subject, String htmlContent) {
        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setFrom(from);
            helper.setTo(to);
            if (StringUtils.isNotBlank(cc)) {
                helper.setCc(cc.split(","));//抄送
            }
            if (StringUtils.isNotBlank(bcc)) {
                helper.setBcc(bcc.split(","));//密送
            }
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setSentDate(new Date());

            sender.setHost("smtp.163.com");
            sender.setUsername("13880298929@163.com");
            sender.setPassword("v5zhu0624");
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", true);  //  将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
            prop.put("mail.smtp.port", 25);
            prop.put("mail.smtp.timeout", "25000");
            sender.setJavaMailProperties(prop);
            sender.send(message);
        } catch (Exception e) {
            logger.error(logger_prefix_email + "发送简单邮件时发生异常！", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 发送带附件的邮件
     *
     * @param to
     * @param subject
     * @param htmlContent
     */
    public static void sendAttachedMail(String from, String to, String subject, String htmlContent) {
        try {

            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            FileSystemResource file1 = new FileSystemResource(new File("D:/test.png"));
            FileSystemResource file2 = new FileSystemResource(new File("D:/log.out"));
            //这里的方法调用和插入图片是不同的。
            helper.addAttachment("test.png", file1);
            helper.addAttachment("log.out", file2);

            sender.setHost("mail.paxsz.com");
            sender.setUsername("mer_paxpay");
            sender.setPassword("Pax67904081");
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", true);  //  将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
            prop.put("mail.smtp.port", 587);  //  将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
            prop.put("mail.smtp.timeout", "25000");
            sender.setJavaMailProperties(prop);
            sender.send(message);
            logger.info("带附件的邮件已经发送");
        } catch (Exception e) {
            logger.error("发送简单邮件时发生异常！", e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
