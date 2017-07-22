import me.cnlm.springboot.redis.Application;
import me.cnlm.springboot.redis.config.RedisClusterConfig;
import me.cnlm.springboot.redis.config.RedisConfig;
import me.cnlm.springboot.redis.redis.JedisClusterFactory;
import me.cnlm.springboot.redis.redis.JedisFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cnlm.me@qq.com on 2017/7/21.
 */
@RunWith(SpringJUnit4ClassRunner.class) // SpringJUnit支持，由此引入Spring-Test框架支持！
@SpringBootTest(classes = Application.class) // 指定我们SpringBoot工程的Application启动类,1.5.4摒弃了SpringApplicationConfiguration注解
@WebAppConfiguration // 由于是Web项目，Junit需要模拟ServletContext，因此我们需要给我们的测试类加上@WebAppConfiguration。
public class RedisClusterTest {

    @Test
    public void testRedis(){
        JedisCluster cluster= JedisClusterFactory.getInstance().getJedisCluster();
        cluster.set("name1","cnlm.me");
        cluster.set("email","cnlm.me@qq.com");
        cluster.set("phone","13880**8929");

        Map<String,String> map=new HashMap();
        map.put("year","2017");
        map.put("month","07");
        map.put("day","22");
        cluster.hmset("map",map);

        cluster.set("company","pax");
        System.out.println(cluster.get("name"));
        System.out.println(cluster.get("email"));
        System.out.println(cluster.get("phone"));
        System.out.println(cluster.hgetAll("map"));
        System.out.println(cluster.get("company"));

        try {
            cluster.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
