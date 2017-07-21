package me.cnlm.springboot.redis.dao;

import me.cnlm.springboot.redis.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author :   zhuxl@paxsz.com
 * @time :   2017/6/20
 * @description:
 */
@Mapper
public interface StudentDao {
    void addStudent(Student student);

}
