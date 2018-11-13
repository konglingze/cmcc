package cmcc_monitor.src.main.scala.com.sheep.cmcc.utils

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.{Jedis, JedisPool}

object Jpools {
  private val poolConfig = new GenericObjectPoolConfig()
  //连接池中最大的空闲连接数，默认是8
  poolConfig.setMaxIdle(5)
  //只支持最大的连接数，连接池中最大的连接数，默认是8
  poolConfig.setMaxTotal(2000)
  private lazy val jedisPool: JedisPool = new JedisPool(poolConfig,AppParams.redisHost)

  def getJedis = {
    val jedis: Jedis = jedisPool.getResource
    jedis.select(AppParams.redisDbIndex)
    jedis
  }
}
