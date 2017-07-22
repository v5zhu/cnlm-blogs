package me.cnlm;

import com.alibaba.fastjson.JSONObject;
import me.cnlm.springboot.quartz.Application;
import me.cnlm.springboot.quartz.dao.UserDao;
import me.cnlm.springboot.quartz.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@SuppressWarnings("ALL")
@RunWith(SpringJUnit4ClassRunner.class) // SpringJUnit支持，由此引入Spring-Test框架支持！
@SpringBootTest(classes = Application.class) // 指定我们SpringBoot工程的Application启动类,1.5.4摒弃了SpringApplicationConfiguration注解
@WebAppConfiguration // 由于是Web项目，Junit需要模拟ServletContext，因此我们需要给我们的测试类加上@WebAppConfiguration。
public class UserTests {
	@Autowired
	private UserDao userDao;

	@Test
	public void findByPhone() {
		String qq="2810010108";
		User user=userDao.findByQQ(qq);
		System.out.println(JSONObject.toJSONString(user));
	}

}
