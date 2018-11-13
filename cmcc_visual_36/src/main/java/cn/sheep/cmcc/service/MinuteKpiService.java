package cmcc_visual_36.src.main.java.cn.sheep.cmcc.service;

import cn.sheep.cmcc.beans.MinutesKpiVo;
import cn.sheep.cmcc.utils.Constants;
import cn.sheep.cmcc.utils.Jpools;
import redis.clients.jedis.Jedis;

public class MinuteKpiService implements IMinuteService {
    @Override
    public MinutesKpiVo findBy(String day, String hourMinutes) {
        MinutesKpiVo vo = new MinutesKpiVo();
        //获取数据
        Jedis jedis = Jpools.getJedis();
        //获取最近一分钟的充值金额
       String money =  jedis.hget(Constants.MINUTES_PREFIX+day,Constants.MINUTES_FIELD_M_PREFIX+hourMinutes);
        //获取最近一分钟的充值笔数
       String count = jedis.hget(Constants.MINUTES_PREFIX+day,Constants.MINUTES_FIELD_C_PREFIX+hourMinutes);
        jedis.close();
        vo.setCounts(count);
        vo.setMoney(money);
        return vo;
    }
}
