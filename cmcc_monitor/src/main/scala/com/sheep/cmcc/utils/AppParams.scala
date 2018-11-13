package cmcc_monitor.src.main.scala.com.sheep.cmcc.utils

import com.typesafe.config.{Config, ConfigFactory, ConfigObject}
import org.apache.kafka.common.serialization.StringDeserializer

object AppParams {
  /**
    * 解析application.conf的配置文件
    * 加载resource下面的配置，默认规则application.conf -> application.json -> application.properties
    */
    private lazy val config: Config = ConfigFactory.load()
  /**
    * 返回订阅的主题,这里用，分割是因为可能有多个主题
    */
  val topic = config.getString("kafka.topic").split(",")
  /**
    * kafka集群所在的主机和端口
    */
  val brokers = config.getString("kafka.broker.list")
  /**
    * 消费者的id
    */
  val groupId = config.getString("kafka.group.id")
  /**
    * kafka的相关参数
    */
  val kafkaParams = Map[String,Object](
    "bootstrap.servers" -> brokers,
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> groupId,
    //这个代表，任务启动之前产生的数据也要读
    "auto.offset.reset" -> "earliest",
    "enable.auto.commit" -> (false:java.lang.Boolean)
  )

  /**
    * redis服务器地址
    */
  val redisHost = config.getString("redis.host")

  /**
    * 将数据写入到哪个库
    */
  val redisDbIndex = config.getString("redis.db.index").toInt
  /**
    * 省份code和省份名称的映射关系
    *
    */
    import scala.collection.JavaConversions._
   val  pcode2PName = config.getObject("pcode2pname").unwrapped().toMap
  //  def main(args: Array[String]): Unit = {
//    val configObject: ConfigObject = config.getObject("pcode2pname")
//    import scala.collection.JavaConversions._
//    val map: Map[String, AnyRef] = configObject.unwrapped().toMap
//    map.foreach(println)
//  }

}
