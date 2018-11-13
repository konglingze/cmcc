package cmcc_monitor.src.main.scala.com.sheep.cmcc.utils

import java.text.SimpleDateFormat

import org.apache.commons.lang3.time.FastDateFormat



object CaculateTools {
  //非线程安全的
  //private val format = new SimpleDateFormat("yyyyMMddHHmmssSSS")
  private val format: FastDateFormat = FastDateFormat.getInstance("yyyyMMddHHmmssSSS")
     def caculateTime(startTime:String,endTime:String):Long = {
       val start = startTime.substring(0,17)
        format.parse(endTime).getTime - format.parse(start).getTime
     }
}
