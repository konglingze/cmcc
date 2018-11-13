package cmcc_visual_36.src.main.java.cn.sheep.cmcc.service;

import cn.sheep.cmcc.beans.MapVo;

import java.util.List;

public interface  IMapIndexService {
    /**
     * 通过日期
     * @param day
     * @return
     */
    List<MapVo> findAllBy(String day);
}
