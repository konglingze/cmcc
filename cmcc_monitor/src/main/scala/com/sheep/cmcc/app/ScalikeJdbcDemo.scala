package cmcc_monitor.src.main.scala.com.sheep.cmcc.app

import scalikejdbc.config._
import scalikejdbc._

/***
  * scalike 访问mysql测试
  */
object ScalikeJdbcDemo {
  def main(args: Array[String]): Unit = {
    //读取mysql的配置 application.conf -> application.json -> application.properties
    DBs.setup()
    //查询数据（只读）
    DB.readOnly(
      implicit session =>{
           SQL("select * from wordcount").map(rs=>{
             (rs.string("words"),
             rs.int(2))
           }).list().apply()
      }.foreach(println)
    )
    //删除数据
   DB.autoCommit(
     implicit session => {
       SQL("delete from wordcount where words='shabi'").update().apply()
     }
   )
    //事务
    DB.localTx(implicit session =>{
      SQL("insert into wordcount values(?,?)").bind("hadoop",10).update().apply()
      var r = 1 / 0
      SQL("insert into wordcount values(?,?)").bind("php",20).update().apply()
    })
  }
}
