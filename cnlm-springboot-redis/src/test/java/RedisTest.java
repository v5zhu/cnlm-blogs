import me.cnlm.springboot.redis.Application;
import me.cnlm.springboot.redis.config.RedisConfig;
import me.cnlm.springboot.redis.redis.JedisFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import redis.clients.jedis.Jedis;

/**
 * Created by cnlm.me@qq.com on 2017/7/21.
 */
@RunWith(SpringJUnit4ClassRunner.class) // SpringJUnit支持，由此引入Spring-Test框架支持！
@SpringBootTest(classes = Application.class) // 指定我们SpringBoot工程的Application启动类,1.5.4摒弃了SpringApplicationConfiguration注解
@WebAppConfiguration // 由于是Web项目，Junit需要模拟ServletContext，因此我们需要给我们的测试类加上@WebAppConfiguration。
public class RedisTest {

    @Test
    public void testRedis(){
        Jedis jedis= JedisFactory.getInstance().getJedis(RedisConfig.HOST,RedisConfig.PORT);
        String isok=jedis.set("phone","1388***8929");
        System.out.println("存储结果:"+isok);
    }
}
