package cmcc_visual_36.src.main.java.cn.sheep.cmcc.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis数据库连接池访问类
 */
public class Jpools {
    private static Config load = ConfigFactory.load();
    private static GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    private static JedisPool jedisPool = null;
    static {
        poolConfig.setMaxIdle(load.getInt("redis.pool.maxActive"));
        poolConfig.setMaxIdle(load.getInt("redis.pool.maxIdle"));
       jedisPool =  new JedisPool(poolConfig,load.getString("redis.ip"),load.getInt("redis.port"));
    }
    //提供redis访问连接
    public static Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        jedis.select(load.getInt("redis.db.index"));
        return jedis;
    }
}
