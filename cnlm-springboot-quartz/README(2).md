# Springboot集成quartz之定时向用户发送邮件（第二期）

##本期使用邮件发送的功能来验证springboot集成定时任务

##1. 创建表t_b_email_message
```sql
CREATE TABLE `t_b_email_message` (
  `user_id` bigint(20) NOT NULL COMMENT '用户主键id',
  `status` tinyint(1) NOT NULL COMMENT '0：不发送，1：需要发送，2：发送成功',
  `sender` varchar(32) NOT NULL COMMENT '邮件发送者',
  `receiver` varchar(32) NOT NULL COMMENT '邮件接收者',
  `cc` varchar(255) DEFAULT NULL COMMENT '邮件抄送人，可多个，逗号隔开',
  `bcc` varchar(255) DEFAULT NULL COMMENT '邮件密送人，可多个，逗号隔开',
  `title` varchar(255) NOT NULL COMMENT '邮件标题',
  `content` varchar(1024) NOT NULL COMMENT '邮件内容',
  `send_time` datetime NOT NULL COMMENT '邮件发送时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```
##2. 创建实体类EmailMessage
```java
package me.cnlm.springboot.quartz.entity;

import java.util.Date;

/**
 * Created by cnlm.me@qq.com on 2017/7/22.
 */
public class EmailMessage {
    private Long userId;
    private Integer status;
    private String sender;
    private String receiver;
    private String cc;
    private String bcc;
    private String title;
    private String content;
    private Date sendTime;

    ···getter setter···
}
```
##3. 创建mybatis xml并提供扫描需要发送邮件的查询

本期已结束，至此项目已经可以定时为用户发送邮件通知了，下期：
- **Springboot集成quartz之集群（第三期）**

##**欢迎加入技术交流QQ群566654343**`（菜鸟联盟 ）`