package cmcc_monitor.src.main.scala.com.sheep.cmcc.app

import java.text.SimpleDateFormat

import cmcc_monitor.src.main.scala.com.sheep.cmcc.utils.{AppParams, Jpools}
import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object BootStrapApp {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)
    val conf: SparkConf = new SparkConf()
      .setAppName("中国移动运营实时监控平台-Monitor")
      //如果是在集群上运行的话需要去掉setMaster
      .setMaster("local[*]")
    //SparkStreaming传输的是离散流，离散流是由RDD组成的
    //数据传输的时候可以对RDD进行压缩，压缩的目的是减少内存的占用
    //默认采用org.apache.spark.serializer.JavaSerializer
    //这是最基本的优化
    conf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
    //rdd压缩
    conf.set("spark.rdd.compress","true")
    //设置每次拉取的数量，为了防止一下子拉取的数据过多，系统处理不过来
    //这里并不是拉取100条，是有公式的。
    //batchSize = partitionNum * 分区数量 * 采样时间
    conf.set("spark.streaming.kafka.maxRatePerPartition","100")
    //设置优雅的结束，这样可以避免数据的丢失
    conf.set("spark.streaming.stopGracefullyOnShutdown","true")
    val ssc: StreamingContext = new StreamingContext(conf,Seconds(2))
    //获取kafka的数据
    /**
      *   指定kafka数据源
      *   ssc：StreamingContext的实例
      *   LocationStrategies：位置策略，如果kafka的broker节点跟Executor在同一台机器上给一种策略，不在一台机器上给另外一种策略
      *       设定策略后会以最优的策略进行获取数据
      *       一般在企业中kafka节点跟Executor不会放到一台机器的，原因是kakfa是消息存储的，Executor用来做消息的计算，
      *       因此计算与存储分开，存储对磁盘要求高，计算对内存、CPU要求高
      *       如果Executor节点跟Broker节点在一起的话使用PreferBrokers策略，如果不在一起的话使用PreferConsistent策略
      *       使用PreferConsistent策略的话，将来在kafka中拉取了数据以后尽量将数据分散到所有的Executor上
      *   ConsumerStrategies：消费者策略（指定如何消费）
      *
      */
    val directStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String,String](AppParams.topic,AppParams.kafkaParams)
    )
    //serviceName为reChargeNotifyReq的才被认为是充值通知
    directStream.foreachRDD(
      rdd =>{
        //rdd.map(_.value()).foreach(println)
        //取得所有充值通知日志
        val baseData: RDD[JSONObject] = rdd.map(cr =>JSON.parseObject(cr.value()))
         .filter(obj => obj.getString("serviceName").equalsIgnoreCase("reChargeNotifyReq")).cache()
        //bussinessRst是业务结果，如果是0000则为成功，其他返回错误编码
        val totalSucc = baseData.map(obj => {
          val reqId = obj.getString("requestId")
          val day = reqId.substring(0, 8)
          //取出该条充值是否成功的标志
          val result = obj.getString("bussinessRst")
          val flag = if (result.equals("0000")) 1 else 0
          (day, flag)
        }).reduceByKey(_+_)
       //获取充值成功的订单金额
        val totalMoney = baseData.map(obj => {
          val reqId = obj.getString("requestId")
          val day = reqId.substring(0, 8)
          //取出该条充值是否成功的标志
          val result = obj.getString("bussinessRst")
          val fee = if (result.equals("0000")) obj.getString("chargefee").toDouble else 0
          (day, fee)
        }).reduceByKey(_+_)
        //总订单量
        val total = baseData.count()

        /**
          * 获取充值成功的充值时长
          */
        val totalTime: RDD[(String, Long)] = baseData.map(obj => {
          val reqId = obj.getString("requestId")
          //获取日期
          val day = reqId.substring(0, 8)
          //取出该条充值是否成功的标志
          val result = obj.getString("bussinessRst")
          //时间格式为：yyyyMMddHHmmssSSS(年月日时分秒毫秒)
          val endTime = obj.getString("receiveNotifyTime")
          val startTime: String = reqId.substring(0, 17)
          val format: SimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS")
          val cost = if (result.equals("0000")) format.parse(endTime).getTime - format.parse(startTime).getTime else 0
          (day, cost)
        }).reduceByKey(_ + _)

       //将充值成功的订单数写入redis
        totalSucc.foreachPartition(it => {
          val jedis: jedis = Jpools.getJedis
          it.foreach(
            tp => {
              jedis.incrBy("CMCC-"+tp._1,tp._2)
            })
          jedis.close()
        })
      }
    )
    ssc.start()
    ssc.awaitTermination()
  }
}
