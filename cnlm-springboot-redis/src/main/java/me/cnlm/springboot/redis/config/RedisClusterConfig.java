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
