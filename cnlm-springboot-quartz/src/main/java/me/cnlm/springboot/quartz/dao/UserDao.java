package me.cnlm.springboot.quartz.dao;

import me.cnlm.springboot.quartz.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author :   cnlm.me@qq.com
 * @time :   2017/6/20
 * @description:
 */
@Mapper
public interface UserDao {
    User findByPhone(String phone);

    User findByQQ(String qq);

}
