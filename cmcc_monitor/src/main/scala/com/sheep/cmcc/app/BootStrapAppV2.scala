package cmcc_monitor.src.main.scala.com.sheep.cmcc.app

import java.lang

import cmcc_monitor.src.main.scala.com.sheep.cmcc.utils.{AppParams, KpiTools, OffsetManager}
import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010._

/**
  * 中国移动监控平台优化版
  */
object BootStrapAppV2 {
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
    conf.set("spark.streaming.kafka.maxRatePerPartition","10000")
    //设置优雅的结束，这样可以避免数据的丢失
    conf.set("spark.streaming.stopGracefullyOnShutdown","true")
    val ssc: StreamingContext = new StreamingContext(conf,Seconds(2))
    /**
      * 提取数据库中的偏移量
      */
    val currOffset = OffsetManager.getMydbCurrentOffset
    /**
      * 广播省份映射关系
      */
    val pcode2PName: Broadcast[Map[String, AnyRef]] = ssc.sparkContext.broadcast(AppParams.pcode2PName)
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
      ConsumerStrategies.Subscribe[String,String](AppParams.topic,AppParams.kafkaParams, currOffset)
    )
    //serviceName为reChargeNotifyReq的才被认为是充值通知
    directStream.foreachRDD(rdd =>{
      val offsetRanges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      //取得所有充值通知日志
      val baseData= KpiTools.baseDataRDD(rdd)

      /**
        * 计算业务概况
        */
      KpiTools.kpi_general(baseData)

      /**
        * 业务概述-每小时的充值情况
        */
      KpiTools.kpi_general_hour(baseData)

      /**
        *业务质量
        */
      KpiTools.kpi_general_quality(baseData,pcode2PName)

      /**
        * 实时充值情况分析
        */
      KpiTools.kpi_realtime_minute(baseData)

      /**
        * 存储偏移量
        */
       OffsetManager.saveCuerrentOffset(offsetRanges)
    })
      ssc.start()
    ssc.awaitTermination()

  }



}
