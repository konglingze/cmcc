package cmcc_visual_36.src.main.java.cn.sheep.cmcc.controller;

import cn.sheep.cmcc.beans.MinutesKpiVo;
import cn.sheep.cmcc.service.IMinuteService;
import cn.sheep.cmcc.service.MinuteKpiService;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(name = "minuteKpiServlet",urlPatterns = {"/minutesKpi.cmcc"})
public class MinuteKpiServlet extends HttpServlet {
   IMinuteService service =  new MinuteKpiService();
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        req.setCharacterEncoding("utf-8");
        //接收参数
        String day = req.getParameter("day");
        //取得当前时间
        Date date = new Date();
        //取到小时和分钟，注意小时时24小时制，分钟也是两位
       SimpleDateFormat format = new SimpleDateFormat("HHmm");
        String time = format.format(date);
        MinutesKpiVo vo = service.findBy(day, time);
        //将对象转换成json并输出
        resp.getWriter().write(JSONObject.toJSONString(vo));
    }
}
