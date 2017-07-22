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
