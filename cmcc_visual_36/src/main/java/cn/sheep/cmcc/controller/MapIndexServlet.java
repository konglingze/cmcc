package cmcc_visual_36.src.main.java.cn.sheep.cmcc.controller;

import cn.sheep.cmcc.beans.MapVo;
import cn.sheep.cmcc.service.IMapIndexService;
import cn.sheep.cmcc.service.MapIndexImpl;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 充值成功业务分布Servelet访问接口
 */
@WebServlet(name = "mapIndexServlet",urlPatterns = {"/mapIndex.cmcc"})
public class MapIndexServlet extends HttpServlet {
    //实例化service对象
    IMapIndexService service = new MapIndexImpl();
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     resp.setCharacterEncoding("utf-8");
     resp.setContentType("application/json");
     //接收前端传递过来的参数
        String day = req.getParameter("day");

        List<MapVo> voList = service.findAllBy(day);
        System.out.println(voList.toString());
        //将数据返回给前端
        String jsonStr = JSONObject.toJSONString(voList);
        System.out.println(jsonStr);
        //将json字符串写到前端
        resp.getWriter().write(jsonStr);
    }
    //    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//    }
//
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//    }
}
