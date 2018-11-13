package cmcc_visual_36.src.main.java.cn.sheep.cmcc.service;

import cn.sheep.cmcc.beans.MapVo;
import cn.sheep.cmcc.utils.Constants;
import cn.sheep.cmcc.utils.Jpools;
import redis.clients.jedis.Jedis;
import sun.awt.im.InputMethodAdapter;

import java.util.*;

public class MapIndexImpl implements IMapIndexService {
    @Override
    public List<MapVo> findAllBy(String day) {
        List<MapVo> list = new ArrayList<>();
        //从redis数据库获取数据
        Jedis jedis = Jpools.getJedis();
        final Map<String, String> all = jedis.hgetAll(Constants.MAP_PRFIX + day);
        //数据封装
        for (Map.Entry<String,String> entry:all.entrySet()){
            MapVo map = new MapVo();
            map.setName(entry.getKey());
            map.setValue(Integer.parseInt(entry.getValue()));
            list.add(map);
        }

        return list;
    }
}
