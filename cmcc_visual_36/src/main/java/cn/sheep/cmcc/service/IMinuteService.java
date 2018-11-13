package cmcc_visual_36.src.main.java.cn.sheep.cmcc.service;

import cn.sheep.cmcc.beans.MinutesKpiVo;

public interface IMinuteService {
    /**
     * 根据日期和时间获取数据
     */
    MinutesKpiVo findBy(String date, String hourMinutes);
}
