#Springboot Redis分布式集群（1）- 搭建工程引入redis

@(Markdown博客)

##1. 创建模块`cnlm-springboot-redis`
- 创建`cnlm-blog`工程的子模块`cnlm-springboot-redis`
- 添加jedis依赖,版本依赖parent的`spring-boot-parent`预定义的版本2.9.0
```xml
<dependencies>
    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
    </dependency>
</dependencies>
```

##2. 设置redis连接参数
- 在resources/application.properties文件中添加如下redis连接参数
```xml
spring.redis.host=120.77.172.143
spring.redis.port=6379
spring.redis.retry.num=10000
spring.redis.pool.max.active=200
spring.redis.pool.max.wait=10000
spring.redis.pool.max.idle=100
spring.redis.pool.min.idle=10
spring.redis.pool.timeout=10000
```
- 添加RedisConfig类以便在其他地方可以访问上述参数
```java
package me.cnlm.springboot.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by cnlm.me@qq.com on 2017/7/21.
 */
@Configuration
public class RedisConfig {
    public static String HOST;
    public static int PORT;
    public static int RETRY_NUM;
    public static int POOL_MAX_ACTIVE;
    public static int POOL_MAX_WAIT;
    public static int POOL_MAX_IDLE;
    public static int POOL_MIN_IDLE;
    public static int POOL_TIMEOUT;

    @Value("${spring.redis.host}")
    public void setHOST(String host) {
        HOST = host;
    }

    @Value("${spring.redis.port}")
    public void setPORT(int port) {
        PORT = port;
    }

    @Value("${spring.redis.retry.num}")
    public void setRETRY_NUM(int retryNum) {
        RETRY_NUM = retryNum;
    }

    @Value("${spring.redis.pool.max.active}")
    public void setPoolMaxActive(int poolMaxActive) {
        POOL_MAX_ACTIVE = poolMaxActive;
    }

    @Value("${spring.redis.pool.max.wait}")
    public void setPoolMaxWait(int poolMaxWait) {
        POOL_MAX_WAIT = poolMaxWait;
    }

    @Value("${spring.redis.pool.max.idle}")
    public void setPoolMaxIdle(int poolMaxIdle) {
        POOL_MAX_IDLE = poolMaxIdle;
    }

    @Value("${spring.redis.pool.min.idle}")
    public void setPoolMinIdle(int poolMinIdle) {
        POOL_MIN_IDLE = poolMinIdle;
    }

    @Value("${spring.redis.pool.timeout}")
    public void setPoolTimeout(int poolTimeout) {
        POOL_TIMEOUT = poolTimeout;
    }
}

```
- 创建JedisFactory类用来获取Jedis实例进行redis存取操作
```java
package me.cnlm.springboot.redis.redis;

import me.cnlm.springboot.redis.config.RedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuxl@paxsz.com on 2017/7/21.
 */
public class JedisFactory {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private JedisFactory(){}

    private static class RedisUtilHolder{
        private static final JedisFactory instance = new JedisFactory();
    }

    public static JedisFactory getInstance(){
        return RedisUtilHolder.instance;
    }

    private static Map<String,JedisPool> maps = new HashMap<String,JedisPool>();

    private static JedisPool getPool(String ip, int port){
        String key = ip+":"+port;
        JedisPool pool = null;
        if(!maps.containsKey(key))
        {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(RedisConfig.POOL_MAX_ACTIVE);
            config.setMaxIdle(RedisConfig.POOL_MAX_IDLE);
            config.setMaxWaitMillis(RedisConfig.POOL_MAX_WAIT);
            config.setTestOnBorrow(true);
            config.setTestOnReturn(true);

            pool = new JedisPool(config,ip,port,RedisConfig.POOL_TIMEOUT);
            maps.put(key, pool);
        }
        else
        {
            pool = maps.get(key);
        }
        return pool;
    }

    public Jedis getJedis(String ip, int port)
    {
        Jedis jedis = null;
        int count = 0;
        do
        {
            try
            {
                jedis = getPool(ip,port).getResource();
            }
            catch (Exception e)
            {
                logger.error("get redis master1 failed!",e);
                getPool(ip,port).returnBrokenResource(jedis);
            }
        }
        while(jedis == null && count<RedisConfig.RETRY_NUM);
        return jedis;
    }

    public void closeJedis(Jedis jedis, String ip, int port){
        if(jedis != null)
        {
            getPool(ip,port).returnResource(jedis);
        }
    }
}
```

##3. 创建单元测试测试存储
- 测试存储数据：{"phone":"1388***8929"}
  -  先测试redis服务器是否存有该json数据
    ```java
    127.0.0.1:6379> get phone
    (nil)
    127.0.0.1:6379> 
    ```
  根据检验结果，redis服务器暂时不存在此phone为1388***8929的key value
  
  -  然后编写单元测试，向redis服务器存储{"phone":"1388***8929"}
    ```java
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
    ``````
        @Test
        public void testRedis(){
            Jedis jedis= JedisFactory.getInstance().getJedis(RedisConfig.HOST,RedisConfig.PORT);
            String isok=jedis.set("phone","1388***8929");
            System.out.println("存储结果:"+isok);
        }
    }

    ```
    运行输出结果:
    ```bash
    ....
    2017-07-21 23:28:03.193  INFO 10324 --- [           main] RedisTest                                : Started RedisTest in 2.549 seconds (JVM running for 3.099)
    存储结果:OK
    Disconnected from the target VM, address: '127.0.0.1:38713', transport: 'socket'
    ....
    ```
    远程redis服务器查询结果：
    ```bash
    127.0.0.1:6379> get phone
    "1388***8929"
    127.0.0.1:6379>
    ```
    到此，说明在此单元测试中jedis已经将值成功存储在redis服务器中，达到预期的目的
    
------------------------------------------
    
- 项目地址:[https://github.com/v5zhu/cnlm-blog](https://github.com/v5zhu/cnlm-blog)
- 欢迎加入新QQ群：566654343
- 本人QQ：2810010108