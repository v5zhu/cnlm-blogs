package me.cnlm.springboot.redis.utils;

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
public class JedisUtils {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private JedisUtils(){}

    private static class RedisUtilHolder{
        private static final JedisUtils instance = new JedisUtils();
    }

    public static JedisUtils getInstance(){
        return RedisUtilHolder.instance;
    }

    private static Map<String,JedisPool> maps = new HashMap<String,JedisPool>();

    private static JedisPool getPool(String ip, int port){
        String key = ip+":"+port;
        JedisPool pool = null;
        if(!maps.containsKey(key))
        {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxActive(RedisConfig.MAX_ACTIVE);
            config.setMaxIdle(RedisConfig.MAX_IDLE);
            config.setMaxWait(RedisConfig.MAX_WAIT);
            config.setTestOnBorrow(true);
            config.setTestOnReturn(true);

            pool = new JedisPool(config,ip,port,RedisConfig.TIMEOUT);
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
