package cmcc_visual_36.src.main.java.cn.sheep.cmcc.beans;

/**
 * 充值量分布成功业务实体类
 */
public class MapVo {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public MapVo(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public MapVo() {
    }

    //省份
    private String name;
    //订单成功数量
    private int value;


}
