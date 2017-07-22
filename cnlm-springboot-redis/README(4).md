#Springboot Redis分布式集群（4）- JedisCluster连接测试集群

@(Markdown博客)

##1. 在resources/application.properties中添加集群连接参数
```xml
spring.redis.cluster.nodes=120.77.172.111:7000,120.77.172.111:7001,120.77.172.111:7002,120.77.172.111:7003,120.77.172.111:7004,120.77.172.111:7005
```
##2. 创建RedisClusterConfig类用来读取集群配置信息
```java
package me.cnlm.springboot.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;

import java.util.*;

/**
 * Created by cnlm.me@qq.com on 2017/7/21.
 */
@Configuration
public class RedisClusterConfig {
    public static Set<HostAndPort> NODES;

    @Value("${spring.redis.cluster.nodes}")
    public void setNODES(String nodes) {
        try {
            Set<HostAndPort> hostAndPortSet = new LinkedHashSet();
            String[] hostAndPorts = nodes.split(",");
            for (String hap : hostAndPorts) {
                String ip = hap.split(":")[0];
                int port = Integer.parseInt(hap.split(":")[1]);

                HostAndPort hostAndPort = new HostAndPort(ip, port);

                hostAndPortSet.add(hostAndPort);
            }
            NODES = hostAndPortSet;
        } catch (Exception e) {
            System.out.println("集群节点配置有误");
            throw e;
        }
    }


}
```
##3. 创建JedisClusterFactory类用来获取远程redis集群连接实例
```java
package me.cnlm.springboot.redis.redis;

import me.cnlm.springboot.redis.config.RedisClusterConfig;
import me.cnlm.springboot.redis.config.RedisConfig;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by cnlm.me@qq.com on 2017/7/22.
 */
public class JedisClusterFactory {
    private static class RedisUtilHolder {
        private static final JedisClusterFactory instance = new JedisClusterFactory();
    }

    public static JedisClusterFactory getInstance() {
        return RedisUtilHolder.instance;
    }

    public JedisCluster getJedisCluster() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(RedisConfig.POOL_MAX_ACTIVE);
        config.setMaxIdle(RedisConfig.POOL_MAX_IDLE);
        config.setMaxWaitMillis(RedisConfig.POOL_MAX_WAIT);
        config.setMinIdle(RedisConfig.POOL_MIN_IDLE);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        JedisCluster cluster = new JedisCluster(RedisClusterConfig.NODES, config);
//        JedisCluster cluster = new JedisCluster(new HostAndPort("120.77.172.143",7000), config);
        return cluster;
    }
}

```
##4.编写单元测试JedisClusterTest类测试集群功能
```java
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
        cluster.set("name","cnlm.me");
        cluster.set("email","cnlm.me@qq.com");
        cluster.set("phone","13880**8929");

        Map<String,String> map=new HashMap();
        map.put("year","2017");
        map.put("month","07");
        map.put("day","22");
        cluster.hmset("map",map);

        cluster.set("company","pax");
        cluster.set("sex","male");
        cluster.set("love","2010");
/*
        System.out.println(cluster.get("name"));
        System.out.println(cluster.get("email"));
        System.out.println(cluster.get("phone"));
        System.out.println(cluster.hgetAll("map"));
        System.out.println(cluster.get("company"));
        System.out.println(cluster.get("sex"));
        System.out.println(cluster.get("love"));*/
    }
}


```
这里在执行单元测试之前，在远程服务器使用monitor命令观察7000，7001，7002三个master节点的数据存储监控
初始化的监控状态：
- 7000节点
```
[root@cnlm redis]# redis-cli -c -h 127.0.0.1 -p 7000
127.0.0.1:7000> clear
127.0.0.1:7000> monitor
OK
```
- 7001节点
```
[root@cnlm redis]# clear
[root@cnlm redis]# redis-cli -c -h 127.0.0.1 -p 7001
127.0.0.1:7001> monitor
OK
```
- 7002节点
```
[root@cnlm redis]# clear
[root@cnlm redis]# redis-cli -c -h 127.0.0.1 -p 7002
127.0.0.1:7002> monitor
OK
```
第一次执行单元测试存储数据，结果：
- 7001节点监控
```
127.0.0.1:7001> monitor
OK
1500700354.296269 [0 125.82.190.120:20605] "PING"
1500700354.327038 [0 125.82.190.120:20605] "SET" "email" "cnlm.me@qq.com"
1500700354.357832 [0 125.82.190.120:20605] "PING"
```
- 7002节点监控
```
127.0.0.1:7002> monitor
OK
1500700159.006154 [0 125.82.190.120:20447] "PING"
1500700354.192247 [0 125.82.190.120:20604] "SET" "name" "cnlm.me"
1500700354.228369 [0 125.82.190.120:20604] "PING
```
这里只存入了两个键值对，name和email，其他的在存储的时候客户端报了连接超时，此问题猜测可能是由于刚刚测试的时候请求次数过多却缺少关闭cluster连接造成超过最大连接数，后面的连接就一直等待超时报异常，添加如下代码暂时未出现超时连接的问题了。
```java
	try{
       cluster.close();
    } catch (IOException e) {
      e.printStackTrace();
	}
```

##5.疑问？
- 单元测试时Jedis实例不能操作远端redis集群，只能使用JedisCluster实例才能操作？
- JedisCluster实例操作redis集群时出现连接被拒绝或超时现象，最开始创建集群为：
```
	./redis-trib.rb  create --replicas 1 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005
```
- 然后连接时报错，连接被拒或超时
```bash
./redis-trib.rb  create --replicas 1 172.18.153.216:7000 172.18.153.216:7001 172.18.153.216:7002 172.18.153.216:7003 172.18.153.216:7004 172.18.153.216:7005
```
- 然而还是被拒或超时，最后IP换成服务器的外网ip才可以在本地开发连接远程redis集群，
- 疑问：**这里的集群节点ip是否需要客户端能访问的ip？**

  

------------------------------------------
    
- 项目地址:[https://github.com/v5zhu/cnlm-blog](https://github.com/v5zhu/cnlm-blog)
- 欢迎加入新QQ群：566654343
- 本人QQ：2810010108