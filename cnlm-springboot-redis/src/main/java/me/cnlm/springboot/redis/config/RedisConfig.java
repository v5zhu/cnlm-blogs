package me.cnlm.springboot.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhuxl@paxsz.com on 2017/7/21.
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
